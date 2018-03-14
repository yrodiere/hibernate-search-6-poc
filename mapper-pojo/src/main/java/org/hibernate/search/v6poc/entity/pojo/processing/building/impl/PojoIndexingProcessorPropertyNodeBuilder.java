/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.processing.building.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.hibernate.search.v6poc.entity.mapping.building.spi.IndexModelBindingContext;
import org.hibernate.search.v6poc.entity.mapping.building.spi.TypeMetadataContributorProvider;
import org.hibernate.search.v6poc.entity.pojo.bridge.IdentifierBridge;
import org.hibernate.search.v6poc.entity.pojo.bridge.PropertyBridge;
import org.hibernate.search.v6poc.entity.pojo.bridge.mapping.BridgeBuilder;
import org.hibernate.search.v6poc.entity.pojo.extractor.ContainerValueExtractor;
import org.hibernate.search.v6poc.entity.pojo.extractor.impl.BoundContainerValueExtractor;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoIdentityMappingCollector;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoIndexModelBinder;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoMappingCollectorPropertyNode;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoMappingCollectorValueNode;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoTypeMetadataContributor;
import org.hibernate.search.v6poc.entity.pojo.model.impl.PojoModelPropertyRootElement;
import org.hibernate.search.v6poc.entity.pojo.model.path.impl.PojoModelPath;
import org.hibernate.search.v6poc.entity.pojo.model.path.impl.PojoModelPathContainerElementNode;
import org.hibernate.search.v6poc.entity.pojo.model.path.impl.PojoModelPathPropertyNode;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoGenericTypeModel;
import org.hibernate.search.v6poc.entity.pojo.processing.impl.PojoIndexingProcessor;
import org.hibernate.search.v6poc.entity.pojo.processing.impl.PojoIndexingProcessorPropertyNode;

public class PojoIndexingProcessorPropertyNodeBuilder<P, T> extends AbstractPojoProcessorNodeBuilder<P>
		implements PojoMappingCollectorPropertyNode {

	private final PojoModelPathPropertyNode<P, T> modelPath;
	private final PojoModelPropertyRootElement pojoModelRootElement;

	private final PojoIdentityMappingCollector identityMappingCollector;

	private final Collection<PropertyBridge> propertyBridges = new ArrayList<>();
	private final PojoIndexingProcessorValueNodeBuilderDelegate<T> valueWithoutExtractorBuilderDelegate;
	private Map<List<? extends Class<? extends ContainerValueExtractor>>,
			PojoIndexingProcessorContainerElementNodeBuilder<? super T, ?>> containerElementNodeBuilders = new HashMap<>();

	PojoIndexingProcessorPropertyNodeBuilder(
			PojoModelPathPropertyNode<P, T> modelPath,
			TypeMetadataContributorProvider<PojoTypeMetadataContributor> contributorProvider,
			PojoIndexModelBinder indexModelBinder, IndexModelBindingContext bindingContext,
			PojoIdentityMappingCollector identityMappingCollector) {
		super( contributorProvider, indexModelBinder, bindingContext );

		this.modelPath = modelPath;

		// FIXME do something more with the pojoModelRootElement, to be able to use it in containedIn processing in particular
		this.pojoModelRootElement = new PojoModelPropertyRootElement( modelPath.getPropertyModel(), contributorProvider );

		this.identityMappingCollector = identityMappingCollector;

		this.valueWithoutExtractorBuilderDelegate = new PojoIndexingProcessorValueNodeBuilderDelegate<>(
				modelPath, modelPath.getPropertyHandle().getName(),
				contributorProvider, indexModelBinder, bindingContext
		);
	}

	@Override
	public void bridge(BridgeBuilder<? extends PropertyBridge> builder) {
		indexModelBinder.addPropertyBridge( bindingContext, pojoModelRootElement, builder )
				.ifPresent( propertyBridges::add );
	}

	@Override
	@SuppressWarnings( {"rawtypes", "unchecked"} )
	public void identifierBridge(BridgeBuilder<? extends IdentifierBridge<?>> builder) {
		PojoGenericTypeModel<T> propertyTypeModel = modelPath.getPropertyModel().getTypeModel();
		IdentifierBridge<T> bridge = indexModelBinder.createIdentifierBridge(
				pojoModelRootElement, propertyTypeModel, builder
		);
		identityMappingCollector.identifierBridge( propertyTypeModel, modelPath.getPropertyHandle(), bridge );
	}

	@Override
	public void containedIn() {
		// FIXME implement ContainedIn
		// FIXME also contribute containedIns to indexedEmbeddeds using the parent's metadata here, if possible?
		throw new UnsupportedOperationException( "Not implemented yet" );
	}

	@Override
	public PojoMappingCollectorValueNode valueWithoutExtractors() {
		return valueWithoutExtractorBuilderDelegate;
	}

	@Override
	public PojoMappingCollectorValueNode valueWithDefaultExtractors() {
		PojoIndexingProcessorContainerElementNodeBuilder<? super T, ?> containerElementNodeBuilder =
				containerElementNodeBuilders.get( null );
		if ( containerElementNodeBuilder == null && !containerElementNodeBuilders.containsKey( null ) ) {
			Optional<BoundContainerValueExtractor<? super T, ?>> boundExtractorOptional =
					indexModelBinder.createDefaultExtractors( modelPath.getPropertyModel().getTypeModel() );
			if ( boundExtractorOptional.isPresent() ) {
				containerElementNodeBuilder = createContainerElementNodeBuilder( boundExtractorOptional.get() );
			}
			containerElementNodeBuilders.put( null, containerElementNodeBuilder );
		}
		if ( containerElementNodeBuilder != null ) {
			return containerElementNodeBuilder.value();
		}
		else {
			return valueWithoutExtractors();
		}
	}

	@Override
	public PojoMappingCollectorValueNode valueWithExtractors(
			List<? extends Class<? extends ContainerValueExtractor>> extractorClasses) {
		PojoIndexingProcessorContainerElementNodeBuilder<? super T, ?> containerElementNodeBuilder =
				containerElementNodeBuilders.get( extractorClasses );
		if ( containerElementNodeBuilder == null ) {
			BoundContainerValueExtractor<? super T, ?> boundExtractor = indexModelBinder.<T>createExplicitExtractors(
					modelPath.getPropertyModel().getTypeModel(), extractorClasses
			);
			containerElementNodeBuilder = createContainerElementNodeBuilder( boundExtractor );
			containerElementNodeBuilders.put( extractorClasses, containerElementNodeBuilder );
		}
		return containerElementNodeBuilder.value();
	}

	/*
	 * This generic method is necessary to make it clear to the compiler
	 * that the extracted type and extractor have compatible generic arguments.
	 */
	private <V> PojoIndexingProcessorContainerElementNodeBuilder<? super T, V> createContainerElementNodeBuilder(
			BoundContainerValueExtractor<? super T, V> boundExtractor) {
		PojoModelPathContainerElementNode<P, ? super T, V> containerElementPath = modelPath.containerElement(
				boundExtractor.getExtractor(), boundExtractor.getExtractedType()
		);
		return new PojoIndexingProcessorContainerElementNodeBuilder<>(
				containerElementPath, contributorProvider, indexModelBinder, bindingContext
		);
	}

	@Override
	PojoModelPath getModelPath() {
		return modelPath;
	}

	@Override
	Optional<PojoIndexingProcessorPropertyNode<P, T>> build() {
		Collection<PropertyBridge> immutableBridges = propertyBridges.isEmpty() ? Collections.emptyList() : new ArrayList<>( propertyBridges );
		Collection<PojoIndexingProcessor<? super T>> valueWithoutExtractorNodes =
				valueWithoutExtractorBuilderDelegate.build();
		Collection<PojoIndexingProcessor<? super T>> immutableNestedNodes =
				valueWithoutExtractorNodes.isEmpty() && containerElementNodeBuilders.isEmpty()
				? Collections.emptyList()
				: new ArrayList<>( valueWithoutExtractorNodes.size() + containerElementNodeBuilders.size() );
		if ( !valueWithoutExtractorNodes.isEmpty() ) {
			immutableNestedNodes.addAll( valueWithoutExtractorNodes );
		}
		containerElementNodeBuilders.values().stream()
				.filter( Objects::nonNull )
				.map( AbstractPojoProcessorNodeBuilder::build )
				.filter( Optional::isPresent )
				.map( Optional::get )
				.forEach( immutableNestedNodes::add );

		if ( immutableBridges.isEmpty() && immutableNestedNodes.isEmpty() ) {
			/*
			 * If this node doesn't have any bridge, nor any nested node,
			 * it is useless and we don't need to build it.
			 */
			return Optional.empty();
		}
		else {
			return Optional.of( new PojoIndexingProcessorPropertyNode<>(
					modelPath.getPropertyHandle(), immutableBridges, immutableNestedNodes
			) );
		}
	}
}
