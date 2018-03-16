/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.mapping.building.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import org.hibernate.search.v6poc.cfg.ConfigurationPropertySource;
import org.hibernate.search.v6poc.engine.spi.BuildContext;
import org.hibernate.search.v6poc.entity.mapping.building.spi.IndexManagerBuildingState;
import org.hibernate.search.v6poc.entity.mapping.building.spi.Mapper;
import org.hibernate.search.v6poc.entity.mapping.building.spi.TypeMetadataContributorProvider;
import org.hibernate.search.v6poc.entity.mapping.spi.MappingImplementor;
import org.hibernate.search.v6poc.entity.model.spi.MappableTypeModel;
import org.hibernate.search.v6poc.entity.pojo.bridge.impl.BridgeResolver;
import org.hibernate.search.v6poc.entity.pojo.extractor.impl.ContainerValueExtractorResolver;
import org.hibernate.search.v6poc.entity.pojo.mapping.impl.PojoMappingDelegateImpl;
import org.hibernate.search.v6poc.entity.pojo.mapping.impl.PojoTypeManagerContainer;
import org.hibernate.search.v6poc.entity.pojo.mapping.spi.PojoMappingDelegate;
import org.hibernate.search.v6poc.entity.pojo.model.augmented.building.impl.PojoAugmentedTypeModelProvider;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoRawTypeModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoBootstrapIntrospector;
import org.hibernate.search.v6poc.entity.pojo.mapping.impl.ProvidedStringIdentifierMapping;
import org.hibernate.search.v6poc.util.AssertionFailure;


/**
 * @author Yoann Rodiere
 */
public class PojoMapper<M extends MappingImplementor> implements Mapper<PojoTypeMetadataContributor, M> {

	private final PojoIndexModelBinder indexModelBinder;
	private final ConfigurationPropertySource propertySource;
	private final boolean implicitProvidedId;
	private final BiFunction<ConfigurationPropertySource, PojoMappingDelegate, M> wrapperFactory;

	private final List<PojoTypeManagerBuilder<?, ?>> typeManagerBuilders = new ArrayList<>();

	public PojoMapper(BuildContext buildContext, ConfigurationPropertySource propertySource,
			PojoBootstrapIntrospector introspector,
			boolean implicitProvidedId,
			BiFunction<ConfigurationPropertySource, PojoMappingDelegate, M> wrapperFactory) {
		ContainerValueExtractorResolver extractorResolver = new ContainerValueExtractorResolver( buildContext );
		BridgeResolver bridgeResolver = new BridgeResolver();
		this.indexModelBinder = new PojoIndexModelBinder(
				buildContext, introspector, extractorResolver, bridgeResolver
		);

		this.propertySource = propertySource;
		this.implicitProvidedId = implicitProvidedId;
		this.wrapperFactory = wrapperFactory;
	}

	@Override
	public void addIndexed(MappableTypeModel typeModel,
			IndexManagerBuildingState<?> indexManagerBuildingState,
			TypeMetadataContributorProvider<PojoTypeMetadataContributor> contributorProvider) {
		if ( !( typeModel instanceof PojoRawTypeModel ) ) {
			throw new AssertionFailure(
					"Expected the indexed type model to be an instance of " + PojoRawTypeModel.class
					+ ", got " + typeModel + " instead. There is probably a bug in the mapper implementation"
			);
		}

		PojoAugmentedTypeModelProvider augmentedTypeModelProvider =
				new PojoAugmentedTypeModelProvider( contributorProvider );
		PojoMappingHelper mappingHelper = new PojoMappingHelper(
				contributorProvider, indexModelBinder, augmentedTypeModelProvider
		);

		PojoRawTypeModel<?> entityTypeModel = (PojoRawTypeModel<?>) typeModel;
		PojoTypeManagerBuilder<?, ?> builder = new PojoTypeManagerBuilder<>(
				entityTypeModel, mappingHelper, indexManagerBuildingState,
				implicitProvidedId ? ProvidedStringIdentifierMapping.get() : null );
		PojoMappingCollectorTypeNode collector = builder.asCollector();
		contributorProvider.forEach(
				entityTypeModel,
				c -> c.contributeMapping( collector )
		);
		typeManagerBuilders.add( builder );
	}

	@Override
	public M build() {
		PojoTypeManagerContainer.Builder typeManagersBuilder = PojoTypeManagerContainer.builder();
		typeManagerBuilders.forEach( b -> b.addTo( typeManagersBuilder ) );
		PojoMappingDelegate mappingImplementor = new PojoMappingDelegateImpl( typeManagersBuilder.build() );
		return wrapperFactory.apply( propertySource, mappingImplementor );
	}

}
