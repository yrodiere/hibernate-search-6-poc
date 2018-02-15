/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.engine.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import org.hibernate.search.v6poc.cfg.ConfigurationPropertySource;
import org.hibernate.search.v6poc.engine.SearchMappingRepository;
import org.hibernate.search.v6poc.engine.SearchMappingRepositoryBuilder;
import org.hibernate.search.v6poc.engine.spi.BeanResolver;
import org.hibernate.search.v6poc.engine.spi.BuildContext;
import org.hibernate.search.v6poc.engine.spi.ReflectionBeanResolver;
import org.hibernate.search.v6poc.engine.spi.ServiceManager;
import org.hibernate.search.v6poc.entity.mapping.spi.MappingKey;
import org.hibernate.search.v6poc.entity.mapping.building.spi.Mapper;
import org.hibernate.search.v6poc.entity.mapping.building.spi.MapperFactory;
import org.hibernate.search.v6poc.entity.mapping.building.spi.MetadataContributor;
import org.hibernate.search.v6poc.entity.mapping.building.spi.TypeMetadataCollector;
import org.hibernate.search.v6poc.entity.mapping.spi.MappingImplementor;
import org.hibernate.search.v6poc.entity.model.spi.TypeModel;
import org.hibernate.search.v6poc.util.SearchException;


/**
 * @author Yoann Rodiere
 */
public class SearchMappingRepositoryBuilderImpl implements SearchMappingRepositoryBuilder {

	private final ConfigurationPropertySource mainPropertySource;
	private final Properties overriddenProperties = new Properties();
	private final Collection<MetadataContributor> contributors = new ArrayList<>();

	private BeanResolver beanResolver = new ReflectionBeanResolver();
	private SearchMappingRepository builtResult;

	public SearchMappingRepositoryBuilderImpl(ConfigurationPropertySource mainPropertySource) {
		this.mainPropertySource = mainPropertySource;
	}

	@Override
	public SearchMappingRepositoryBuilder setBeanResolver(BeanResolver beanResolver) {
		this.beanResolver = beanResolver;
		return this;
	}

	@Override
	public SearchMappingRepositoryBuilder setProperty(String name, String value) {
		this.overriddenProperties.setProperty( name, value );
		return this;
	}

	@Override
	public SearchMappingRepositoryBuilder setProperties(Properties properties) {
		this.overriddenProperties.putAll( properties );
		return this;
	}

	@Override
	public SearchMappingRepositoryBuilder addMapping(MetadataContributor mappingContributor) {
		contributors.add( mappingContributor );
		return this;
	}

	@Override
	public SearchMappingRepository build() {
		ServiceManager serviceManager = new ServiceManagerImpl( beanResolver );
		BuildContext buildContext = new BuildContextImpl( serviceManager );

		ConfigurationPropertySource propertySource;
		if ( !overriddenProperties.isEmpty() ) {
			propertySource = ConfigurationPropertySource.fromProperties( overriddenProperties )
					.withFallback( mainPropertySource );
		}
		else {
			propertySource = mainPropertySource;
		}

		IndexManagerBuildingStateHolder indexManagerBuildingStateProvider =
				new IndexManagerBuildingStateHolder( buildContext, propertySource );
		// TODO close the holder (which will close the backends) if anything fails after this

		TypeMetadataCollectorImpl metadataCollector = new TypeMetadataCollectorImpl();
		contributors.forEach( c -> c.contribute( buildContext, metadataCollector ) );

		Map<MappingKey<?>, Mapper<?, ?>> mappers =
				metadataCollector.createMappers( buildContext, propertySource, indexManagerBuildingStateProvider );

		Map<MappingKey<?>, MappingImplementor> mappings = new HashMap<>();
		// TODO close the mappings created so far if anything fails after this
		mappers.forEach( (mappingKey, mapper) -> {
			MappingImplementor mapping = mapper.build();
			mappings.put( mappingKey, mapping );
		} );

		builtResult = new SearchMappingRepositoryImpl(
				mappings, indexManagerBuildingStateProvider.getBackendsByName()
		);
		return builtResult;
	}

	@Override
	public SearchMappingRepository getBuiltResult() {
		return builtResult;
	}

	private static class TypeMetadataCollectorImpl implements TypeMetadataCollector {
		private final Map<MappingKey<?>, MapperContribution<?, ?>> contributionByMappingKey = new HashMap<>();

		@Override
		public <C> void collect(MapperFactory<C, ?> mapperFactory, TypeModel typeModel,
				String indexName, C contributor) {
			@SuppressWarnings("unchecked")
			MapperContribution<C, ?> contribution = (MapperContribution<C, ?>)
					contributionByMappingKey.computeIfAbsent( mapperFactory, ignored -> new MapperContribution<>( mapperFactory ));
			contribution.update( typeModel, indexName, contributor );
		}

		Map<MappingKey<?>, Mapper<?, ?>> createMappers(
				BuildContext buildContext, ConfigurationPropertySource propertySource,
				IndexManagerBuildingStateHolder indexManagerBuildingStateProvider) {
			Map<MappingKey<?>, Mapper<?, ?>> mappers = new HashMap<>();
			contributionByMappingKey.forEach( (mappingKey, contribution) -> {
				Mapper<?, ?> mapper = contribution.preBuild( buildContext, propertySource, indexManagerBuildingStateProvider );
				mappers.put( mappingKey, mapper );
			} );
			return mappers;
		}
	}

	private static class MapperContribution<C, M extends MappingImplementor> {

		private final MapperFactory<C, M> mapperFactory;
		private final Map<TypeModel, TypeMappingContribution<C>> contributionByType = new HashMap<>();

		MapperContribution(MapperFactory<C, M> mapperFactory) {
			this.mapperFactory = mapperFactory;
		}

		public void update(TypeModel typeModel, String indexName, C contributor) {
			contributionByType.computeIfAbsent( typeModel, TypeMappingContribution::new )
					.update( indexName, contributor );
		}

		public Mapper<C, M> preBuild(BuildContext buildContext, ConfigurationPropertySource propertySource,
				IndexManagerBuildingStateHolder indexManagerBuildingStateHolder) {
			Mapper<C, M> mapper = mapperFactory.createMapper( buildContext, propertySource );
			for ( TypeModel typeModel : contributionByType.keySet() ) {
				Optional<String> indexNameOptional = typeModel.getAscendingSuperTypes()
						.map( contributionByType::get )
						.filter( Objects::nonNull )
						.map( TypeMappingContribution::getIndexName )
						.filter( Objects::nonNull )
						.findFirst();
				if ( indexNameOptional.isPresent() ) {
					String indexName = indexNameOptional.get();
					mapper.addIndexed(
							typeModel,
							indexManagerBuildingStateHolder.startBuilding( indexName ),
							this::getContributors
					);
				}
			}
			return mapper;
		}

		private Stream<C> getContributors(TypeModel typeModel) {
			return typeModel.getDescendingSuperTypes()
					.map( contributionByType::get )
					.filter( Objects::nonNull )
					.flatMap( TypeMappingContribution::getContributors );
		}
	}

	private static class TypeMappingContribution<C> {
		private final TypeModel typeModel;
		private String indexName;
		private final List<C> contributors = new ArrayList<>();

		public TypeMappingContribution(TypeModel typeModel) {
			this.typeModel = typeModel;
		}

		public String getIndexName() {
			return indexName;
		}

		public void update(String indexName, C contributor) {
			if ( indexName != null && !indexName.isEmpty() ) {
				if ( this.indexName != null ) {
					throw new SearchException( "Type '" + typeModel + "' mapped to multiple indexes: '"
							+ this.indexName + "', '" + indexName + "'." );
				}
				this.indexName = indexName;
			}
			this.contributors.add( contributor );
		}

		public Stream<C> getContributors() {
			return contributors.stream();
		}
	}
}
