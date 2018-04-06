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
import org.hibernate.search.v6poc.entity.pojo.extractor.impl.ContainerValueExtractorBinder;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.spi.PojoMappingCollectorTypeNode;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.spi.PojoTypeMetadataContributor;
import org.hibernate.search.v6poc.entity.pojo.mapping.impl.PojoMappingDelegateImpl;
import org.hibernate.search.v6poc.entity.pojo.mapping.impl.PojoIndexedTypeManagerContainer;
import org.hibernate.search.v6poc.entity.pojo.mapping.spi.PojoMappingDelegate;
import org.hibernate.search.v6poc.entity.pojo.model.augmented.building.impl.PojoAugmentedTypeModelProvider;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoRawTypeModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoBootstrapIntrospector;
import org.hibernate.search.v6poc.entity.pojo.mapping.impl.ProvidedStringIdentifierMapping;
import org.hibernate.search.v6poc.util.AssertionFailure;

class PojoMapper<M> implements Mapper<M> {

	private final ConfigurationPropertySource propertySource;
	private final TypeMetadataContributorProvider<PojoTypeMetadataContributor> contributorProvider;
	private final boolean implicitProvidedId;
	private final BiFunction<ConfigurationPropertySource, PojoMappingDelegate, MappingImplementor<M>> wrapperFactory;
	private final PojoMappingHelper mappingHelper;

	private final List<PojoIndexedTypeManagerBuilder<?, ?>> indexedTypeManagerBuilders = new ArrayList<>();

	PojoMapper(BuildContext buildContext, ConfigurationPropertySource propertySource,
			TypeMetadataContributorProvider<PojoTypeMetadataContributor> contributorProvider,
			PojoBootstrapIntrospector introspector,
			boolean implicitProvidedId,
			BiFunction<ConfigurationPropertySource, PojoMappingDelegate, MappingImplementor<M>> wrapperFactory) {
		this.propertySource = propertySource;
		this.contributorProvider = contributorProvider;
		this.implicitProvidedId = implicitProvidedId;
		this.wrapperFactory = wrapperFactory;

		PojoAugmentedTypeModelProvider augmentedTypeModelProvider =
				new PojoAugmentedTypeModelProvider( contributorProvider );

		ContainerValueExtractorBinder extractorBinder = new ContainerValueExtractorBinder( buildContext );
		BridgeResolver bridgeResolver = new BridgeResolver();
		PojoIndexModelBinder indexModelBinder = new PojoIndexModelBinderImpl(
				buildContext, introspector, extractorBinder, bridgeResolver
		);

		mappingHelper = new PojoMappingHelper(
				contributorProvider, indexModelBinder, augmentedTypeModelProvider
		);
	}

	@Override
	public void addIndexed(MappableTypeModel typeModel, IndexManagerBuildingState<?> indexManagerBuildingState) {
		if ( !( typeModel instanceof PojoRawTypeModel ) ) {
			throw new AssertionFailure(
					"Expected the indexed type model to be an instance of " + PojoRawTypeModel.class
					+ ", got " + typeModel + " instead. There is probably a bug in the mapper implementation"
			);
		}

		PojoRawTypeModel<?> entityTypeModel = (PojoRawTypeModel<?>) typeModel;
		PojoIndexedTypeManagerBuilder<?, ?> builder = new PojoIndexedTypeManagerBuilder<>(
				entityTypeModel, mappingHelper, indexManagerBuildingState,
				implicitProvidedId ? ProvidedStringIdentifierMapping.get() : null );
		PojoMappingCollectorTypeNode collector = builder.asCollector();
		contributorProvider.forEach(
				entityTypeModel,
				c -> c.contributeMapping( collector )
		);
		indexedTypeManagerBuilders.add( builder );
	}

	@Override
	public MappingImplementor<M> build() {
		PojoIndexedTypeManagerContainer.Builder indexedTypeManagerContainerBuilder =
				PojoIndexedTypeManagerContainer.builder();
		indexedTypeManagerBuilders.forEach( b -> b.addTo( indexedTypeManagerContainerBuilder ) );
		PojoMappingDelegate mappingImplementor = new PojoMappingDelegateImpl(
				indexedTypeManagerContainerBuilder.build()
		);
		return wrapperFactory.apply( propertySource, mappingImplementor );
	}

}
