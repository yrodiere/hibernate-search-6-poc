/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.engine.impl;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.hibernate.search.v6poc.cfg.ConfigurationPropertySource;
import org.hibernate.search.v6poc.engine.SearchMappingRepository;
import org.hibernate.search.v6poc.engine.SearchMappingRepositoryBuilder;
import org.hibernate.search.v6poc.engine.spi.BeanProvider;
import org.hibernate.search.v6poc.engine.spi.BeanResolver;
import org.hibernate.search.v6poc.engine.spi.ReflectionBeanResolver;
import org.hibernate.search.v6poc.engine.spi.ServiceManager;
import org.hibernate.search.v6poc.entity.mapping.building.spi.IndexManagerBuildingState;
import org.hibernate.search.v6poc.entity.mapping.building.spi.Mapper;
import org.hibernate.search.v6poc.entity.mapping.building.spi.MappingAbortedException;
import org.hibernate.search.v6poc.entity.mapping.building.spi.MappingConfigurationCollector;
import org.hibernate.search.v6poc.entity.mapping.building.spi.MappingInitiator;
import org.hibernate.search.v6poc.entity.mapping.building.spi.TypeMetadataContributorProvider;
import org.hibernate.search.v6poc.entity.mapping.building.spi.TypeMetadataDiscoverer;
import org.hibernate.search.v6poc.entity.mapping.spi.MappingBuildContext;
import org.hibernate.search.v6poc.entity.mapping.spi.MappingImplementor;
import org.hibernate.search.v6poc.entity.mapping.spi.MappingKey;
import org.hibernate.search.v6poc.entity.model.spi.MappableTypeModel;
import org.hibernate.search.v6poc.logging.impl.Log;
import org.hibernate.search.v6poc.logging.impl.RootFailureCollector;
import org.hibernate.search.v6poc.logging.spi.ContextualFailureCollector;
import org.hibernate.search.v6poc.logging.spi.EventContexts;
import org.hibernate.search.v6poc.util.AssertionFailure;
import org.hibernate.search.v6poc.util.SearchException;
import org.hibernate.search.v6poc.util.impl.common.LoggerFactory;
import org.hibernate.search.v6poc.util.impl.common.SuppressingCloser;

public class SearchMappingRepositoryBuilderImpl implements SearchMappingRepositoryBuilder {

	private static final Log log = LoggerFactory.make( Log.class, MethodHandles.lookup() );

	private static final int FAILURE_LIMIT = 100;

	private final ConfigurationPropertySource mainPropertySource;
	private final Properties overriddenProperties = new Properties();
	private final List<MappingInitiator<?, ?>> mappingInitiators = new ArrayList<>();

	private BeanResolver beanResolver;
	private boolean frozen = false;
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
	public SearchMappingRepositoryBuilder addMappingInitiator(MappingInitiator initiator) {
		if ( frozen ) {
			throw new AssertionFailure(
					"Attempt to add a mapping initiator"
					+ " after Hibernate Search has started to build the mappings."
					+ " There is a bug in the Hibernate Search integration."
			);
		}
		mappingInitiators.add( initiator );
		return this;
	}

	@Override
	public SearchMappingRepository build() {
		IndexManagerBuildingStateHolder indexManagerBuildingStateHolder = null;
		// Use a LinkedHashMap for deterministic iteration
		List<MappingBuildingState<?, ?>> mappingBuildingStates = new ArrayList<>();
		Map<MappingKey<?>, MappingImplementor<?>> mappings = new HashMap<>();
		RootFailureCollector failureCollector = new RootFailureCollector( FAILURE_LIMIT );
		boolean checkingRootFailures = false;

		try {
			frozen = true;

			if ( beanResolver == null ) {
				beanResolver = new ReflectionBeanResolver();
			}

			BeanProvider beanProvider = new BeanProviderImpl( beanResolver );
			ServiceManager serviceManager = new ServiceManagerImpl( beanProvider );
			RootBuildContext rootBuildContext = new RootBuildContext( serviceManager, failureCollector );

			ConfigurationPropertySource propertySource;
			if ( !overriddenProperties.isEmpty() ) {
				propertySource = ConfigurationPropertySource.fromProperties( overriddenProperties )
						.withFallback( mainPropertySource );
			}
			else {
				propertySource = mainPropertySource;
			}

			indexManagerBuildingStateHolder = new IndexManagerBuildingStateHolder( rootBuildContext, propertySource );

			// First phase: collect configuration for all mappings
			for ( MappingInitiator initiator : mappingInitiators ) {
				MappingBuildingState<?, ?> mappingBuildingState =
						new MappingBuildingState<>( rootBuildContext, propertySource, initiator );
				mappingBuildingStates.add( mappingBuildingState );
				mappingBuildingState.collect();
			}
			checkingRootFailures = true;
			failureCollector.checkNoFailure();
			checkingRootFailures = false;

			// Second phase: create mappers and their backing index managers
			for ( MappingBuildingState<?, ?> mappingBuildingState : mappingBuildingStates ) {
				mappingBuildingState.createMapper( indexManagerBuildingStateHolder );
			}
			checkingRootFailures = true;
			failureCollector.checkNoFailure();
			checkingRootFailures = false;

			// Third phase: create mappings
			for ( MappingBuildingState<?, ?> mappingBuildingState : mappingBuildingStates ) {
				mappingBuildingState.createAndAddMapping( mappings );
			}
			checkingRootFailures = true;
			failureCollector.checkNoFailure();
			checkingRootFailures = false;

			builtResult = new SearchMappingRepositoryImpl(
					beanResolver,
					mappings,
					indexManagerBuildingStateHolder.getBackendsByName(),
					indexManagerBuildingStateHolder.getIndexManagersByName()
			);
		}
		catch (RuntimeException e) {
			RuntimeException rethrownException;
			if ( checkingRootFailures ) {
				// The exception was thrown by one of the failure checks above. No need for an additional check.
				rethrownException = e;
			}
			else {
				/*
				 * The exception was thrown by something other than the failure checks above
				 * (a mapper, a backend, ...).
				 * We should check that no failure was collected before.
				 */
				try {
					failureCollector.checkNoFailure();
					// No other failure, just rethrow the exception.
					rethrownException = e;
				}
				catch (SearchException e2) {
					/*
					 * At least one failure was collected, most likely before "e" was even thrown.
					 * Let's throw "e2" (which mentions prior failures), only mentioning "e" as a suppressed exception.
					 */
					rethrownException = e2;
					rethrownException.addSuppressed( e );
				}
			}

			SuppressingCloser closer = new SuppressingCloser( rethrownException );
			// Close the mappers and mappings created so far before aborting
			closer.pushAll( MappingImplementor::close, mappings.values() );
			closer.pushAll( MappingBuildingState::closeOnFailure, mappingBuildingStates );
			// Close the resources contained in the index manager building state before aborting
			closer.pushAll( holder -> holder.closeOnFailure( closer ), indexManagerBuildingStateHolder );
			// Close the bean resolver before aborting
			closer.pushAll( BeanResolver::close, beanResolver );

			throw rethrownException;
		}

		return builtResult;
	}

	@Override
	public SearchMappingRepository getBuiltResult() {
		return builtResult;
	}

	private static class MappingBuildingState<C, M> {
		private final MappingBuildContext buildContext;
		private final ConfigurationPropertySource propertySource;

		private final MappingKey<M> mappingKey;
		private final MappingInitiator<C, M> mappingInitiator;

		// Use a LinkedHashMap for deterministic iteration
		private final Map<MappableTypeModel, TypeMappingContribution<C>> contributionByType = new LinkedHashMap<>();
		private final List<TypeMetadataDiscoverer<C>> metadataDiscoverers = new ArrayList<>();
		private boolean multiTenancyEnabled;

		private final Set<MappableTypeModel> typesSubmittedToDiscoverers = new HashSet<>();

		private Mapper<M> mapper; // Initially null, set in createMapper()

		MappingBuildingState(RootBuildContext rootBuildContext, ConfigurationPropertySource propertySource,
				MappingInitiator<C, M> mappingInitiator) {
			this.mappingKey = mappingInitiator.getMappingKey();
			this.buildContext = new MappingBuildContextImpl( rootBuildContext, mappingKey );
			this.propertySource = propertySource;
			this.mappingInitiator = mappingInitiator;
		}

		void collect() {
			mappingInitiator.configure( buildContext, propertySource, new ConfigurationCollector() );
		}

		void createMapper(IndexManagerBuildingStateHolder indexManagerBuildingStateHolder) {

			ContributorProvider contributorProvider = new ContributorProvider();
			mapper = mappingInitiator.createMapper( buildContext, propertySource, contributorProvider );

			Set<MappableTypeModel> potentiallyMappedToIndexTypes = new LinkedHashSet<>(
					contributionByType.keySet() );
			for ( MappableTypeModel typeModel : potentiallyMappedToIndexTypes ) {
				TypeMappingContribution<C> contribution = contributionByType.get( typeModel );
				String indexName = contribution.getIndexName();
				if ( indexName != null ) {
					IndexManagerBuildingState<?> indexManagerBuildingState;
					try {
						indexManagerBuildingState = indexManagerBuildingStateHolder
								.startBuilding( indexName, multiTenancyEnabled );
					}
					catch (RuntimeException e) {
						buildContext.getFailureCollector()
								.withContext( EventContexts.fromType( typeModel ) )
								.withContext( EventContexts.fromIndexName( indexName ) )
								.add( e );
						continue;
					}
					mapper.addIndexed(
							typeModel,
							indexManagerBuildingState
					);
				}
			}
		}

		void createAndAddMapping(Map<MappingKey<?>, MappingImplementor<?>> mappings) {
			if ( mappings.containsKey( mappingKey ) ) {
				throw new AssertionFailure(
						"Found two mapping initiators using the same key."
						+ " There is a bug in the mapper, please report it."
				);
			}

			try {
				MappingImplementor<M> mapping = mapper.build();
				mappings.put( mappingKey, mapping );
			}
			catch (MappingAbortedException e) {
				ContextualFailureCollector failureCollector = buildContext.getFailureCollector();

				if ( !failureCollector.hasFailure() ) {
					throw new AssertionFailure(
							"Caught " + MappingAbortedException.class.getSimpleName()
									+ ", but the mapper did not collect any failure."
									+ " There is a bug in the mapper, please report it.",
							e
					);
				}

				/*
				 * This generally shouldn't do anything, because we don't expect a cause nor suppressed exceptions
				 * in the MappingAbortedException, but ignoring exceptions can lead to
				 * spending some really annoying hours debugging.
				 * So let's be extra cautious not to lose these.
				 */
				Throwable cause = e.getCause();
				if ( cause != null ) {
					failureCollector.add( cause );
				}
				Throwable[] suppressed = e.getSuppressed();
				for ( Throwable throwable : suppressed ) {
					failureCollector.add( throwable );
				}
			}
		}

		private TypeMappingContribution<C> getOrCreateContribution(MappableTypeModel typeModel) {
			TypeMappingContribution<C> contribution = contributionByType.get( typeModel );
			if ( contribution == null ) {
				contribution = new TypeMappingContribution<>( typeModel );
				contributionByType.put( typeModel, contribution );
			}
			return contribution;
		}

		private TypeMappingContribution<C> getContributionIncludingAutomaticallyDiscovered(
				MappableTypeModel typeModel) {
			if ( !typesSubmittedToDiscoverers.contains( typeModel ) ) {
				// Allow automatic discovery of metadata the first time we encounter each type
				for ( TypeMetadataDiscoverer<C> metadataDiscoverer : metadataDiscoverers ) {
					Optional<C> discoveredContributor = metadataDiscoverer.discover( typeModel );
					if ( discoveredContributor.isPresent() ) {
						getOrCreateContribution( typeModel )
								.collectContributor( discoveredContributor.get() );
					}
				}
				typesSubmittedToDiscoverers.add( typeModel );
			}
			return contributionByType.get( typeModel );
		}

		public void closeOnFailure() {
			if ( mapper != null ) {
				mapper.closeOnFailure();
			}
		}

		private class ConfigurationCollector implements MappingConfigurationCollector<C> {
			@Override
			public void mapToIndex(MappableTypeModel typeModel, String indexName) {
				if ( typeModel.isAbstract() ) {
					throw log.cannotMapAbstractTypeToIndex( typeModel, indexName );
				}
				getOrCreateContribution( typeModel ).mapToIndex( indexName );
			}

			@Override
			public void collectContributor(MappableTypeModel typeModel, C contributor) {
				getOrCreateContribution( typeModel ).collectContributor( contributor );
			}

			@Override
			public void collectDiscoverer(TypeMetadataDiscoverer<C> metadataDiscoverer) {
				metadataDiscoverers.add( metadataDiscoverer );
			}

			@Override
			public void enableMultiTenancy() {
				multiTenancyEnabled = true;
			}
		}

		private class ContributorProvider implements TypeMetadataContributorProvider<C> {
			@Override
			public void forEach(MappableTypeModel typeModel, Consumer<C> contributorConsumer) {
				typeModel.getDescendingSuperTypes()
						.map( MappingBuildingState.this::getContributionIncludingAutomaticallyDiscovered )
						.filter( Objects::nonNull )
						.flatMap( TypeMappingContribution::getContributors )
						.forEach( contributorConsumer );
			}

			@Override
			public Set<? extends MappableTypeModel> getTypesContributedTo() {
				// Use a LinkedHashSet for deterministic iteration
				return Collections.unmodifiableSet( new LinkedHashSet<>( contributionByType.keySet() ) );
			}
		}
	}

	private static class TypeMappingContribution<C> {
		private final MappableTypeModel typeModel;
		private String indexName;
		private final List<C> contributors = new ArrayList<>();

		TypeMappingContribution(MappableTypeModel typeModel) {
			this.typeModel = typeModel;
		}

		public String getIndexName() {
			return indexName;
		}

		public void mapToIndex(String indexName) {
			if ( this.indexName != null ) {
				throw new SearchException( "Type '" + typeModel + "' mapped to multiple indexes: '"
						+ this.indexName + "', '" + indexName + "'." );
			}
			this.indexName = indexName;
		}

		public void collectContributor(C contributor) {
			this.contributors.add( contributor );
		}

		public Stream<C> getContributors() {
			return contributors.stream();
		}
	}
}
