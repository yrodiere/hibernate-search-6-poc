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

import org.hibernate.search.v6poc.backend.document.DocumentElement;
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
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoGenericTypeModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoRawTypeModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoIntrospector;
import org.hibernate.search.v6poc.entity.pojo.processing.impl.ProvidedStringIdentifierMapping;
import org.hibernate.search.v6poc.util.AssertionFailure;


/**
 * @author Yoann Rodiere
 */
public class PojoMapper<M extends MappingImplementor> implements Mapper<PojoTypeNodeMetadataContributor, M> {

	private final PojoIndexModelBinder indexModelBinder;
	private final ConfigurationPropertySource propertySource;
	private final PojoIntrospector introspector;
	private final boolean implicitProvidedId;
	private final BiFunction<ConfigurationPropertySource, PojoMappingDelegate, M> wrapperFactory;

	private final List<PojoTypeManagerBuilder<?, ?>> typeManagerBuilders = new ArrayList<>();

	public PojoMapper(BuildContext buildContext, ConfigurationPropertySource propertySource,
			PojoIntrospector introspector,
			boolean implicitProvidedId,
			BiFunction<ConfigurationPropertySource, PojoMappingDelegate, M> wrapperFactory) {
		ContainerValueExtractorResolver extractorResolver = new ContainerValueExtractorResolver( buildContext );
		BridgeResolver bridgeResolver = new BridgeResolver();
		this.indexModelBinder = new PojoIndexModelBinderImpl(
				buildContext, introspector, extractorResolver, bridgeResolver
		);

		this.propertySource = propertySource;
		this.introspector = introspector;
		this.implicitProvidedId = implicitProvidedId;
		this.wrapperFactory = wrapperFactory;
	}

	@Override
	public void addIndexed(MappableTypeModel typeModel,
			IndexManagerBuildingState<?> indexManagerBuildingState,
			TypeMetadataContributorProvider<PojoTypeNodeMetadataContributor> contributorProvider) {
		if ( !( typeModel instanceof PojoRawTypeModel ) ) {
			throw new AssertionFailure(
					"Expected the indexed type model to be an instance of " + PojoRawTypeModel.class
							+ ", got " + typeModel + " instead. There is probably a bug in the mapper implementation"
			);
		}
		PojoRawTypeModel<?> rawTypeModel = (PojoRawTypeModel<?>) typeModel;
		PojoTypeManagerBuilder<?, ?> builder = createBuilder( rawTypeModel, indexManagerBuildingState, contributorProvider );
		PojoTypeNodeMappingCollector collector = builder.asCollector();
		contributorProvider.forEach(
				rawTypeModel,
				c -> c.contributeMapping( collector )
		);
		typeManagerBuilders.add( builder );
	}

	@Override
	public M build() {
		PojoTypeManagerContainer.Builder typeManagersBuilder = PojoTypeManagerContainer.builder();
		typeManagerBuilders.forEach( b -> b.addTo( typeManagersBuilder ) );
		PojoMappingDelegate mappingImplementor = new PojoMappingDelegateImpl( typeManagersBuilder.build(), introspector );
		return wrapperFactory.apply( propertySource, mappingImplementor );
	}

	private <T, D extends DocumentElement> PojoTypeManagerBuilder<T, D> createBuilder(PojoRawTypeModel<T> rawTypeModel,
			IndexManagerBuildingState<D> indexManagerBuildingState,
			TypeMetadataContributorProvider<PojoTypeNodeMetadataContributor> contributorProvider) {
		PojoGenericTypeModel<T> genericTypeModel = introspector.getGenericTypeModel( rawTypeModel.getJavaClass() );
		return new PojoTypeManagerBuilder<>(
				rawTypeModel, genericTypeModel,
				contributorProvider, indexModelBinder, indexManagerBuildingState,
				implicitProvidedId ? ProvidedStringIdentifierMapping.get() : null );
	}

}
