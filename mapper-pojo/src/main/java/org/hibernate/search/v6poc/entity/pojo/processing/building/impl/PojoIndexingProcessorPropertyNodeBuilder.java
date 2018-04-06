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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.hibernate.search.v6poc.entity.mapping.building.spi.IndexModelBindingContext;
import org.hibernate.search.v6poc.entity.pojo.bridge.IdentifierBridge;
import org.hibernate.search.v6poc.entity.pojo.bridge.PropertyBridge;
import org.hibernate.search.v6poc.entity.pojo.bridge.mapping.BridgeBuilder;
import org.hibernate.search.v6poc.entity.pojo.dirtiness.building.impl.PojoIndexingDependencyCollectorPropertyNode;
import org.hibernate.search.v6poc.entity.pojo.dirtiness.building.impl.PojoIndexingDependencyCollectorTypeNode;
import org.hibernate.search.v6poc.entity.pojo.extractor.ContainerValueExtractor;
import org.hibernate.search.v6poc.entity.pojo.extractor.ContainerValueExtractorPath;
import org.hibernate.search.v6poc.entity.pojo.extractor.impl.BoundContainerValueExtractorPath;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.BoundPropertyBridge;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoIdentityMappingCollector;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoMappingHelper;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.spi.PojoMappingCollectorPropertyNode;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.spi.PojoMappingCollectorValueNode;
import org.hibernate.search.v6poc.entity.pojo.model.path.impl.BoundPojoModelPath;
import org.hibernate.search.v6poc.entity.pojo.model.path.impl.BoundPojoModelPathPropertyNode;
import org.hibernate.search.v6poc.entity.pojo.model.path.impl.BoundPojoModelPathValueNode;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoGenericTypeModel;
import org.hibernate.search.v6poc.entity.pojo.processing.impl.PojoIndexingProcessor;
import org.hibernate.search.v6poc.entity.pojo.processing.impl.PojoIndexingProcessorPropertyNode;

public class PojoIndexingProcessorPropertyNodeBuilder<T, P> extends AbstractPojoProcessorNodeBuilder<T>
		implements PojoMappingCollectorPropertyNode {

	private final BoundPojoModelPathPropertyNode<T, P> modelPath;

	private final PojoIdentityMappingCollector identityMappingCollector;

	private final Collection<BoundPropertyBridge<P>> boundPropertyBridges = new ArrayList<>();
	private final PojoIndexingProcessorValueNodeBuilderDelegate<P, P> valueWithoutExtractorBuilderDelegate;
	private Map<ContainerValueExtractorPath, PojoIndexingProcessorContainerElementNodeBuilder<P, ? super P, ?>>
			containerElementNodeBuilders = new HashMap<>();

	PojoIndexingProcessorPropertyNodeBuilder(
			BoundPojoModelPathPropertyNode<T, P> modelPath,
			PojoMappingHelper mappingHelper, IndexModelBindingContext bindingContext,
			PojoIdentityMappingCollector identityMappingCollector) {
		super( mappingHelper, bindingContext );

		this.modelPath = modelPath;

		this.identityMappingCollector = identityMappingCollector;

		this.valueWithoutExtractorBuilderDelegate = new PojoIndexingProcessorValueNodeBuilderDelegate<>(
				modelPath.valueWithoutExtractors(),
				mappingHelper, bindingContext
		);
	}

	@Override
	public void bridge(BridgeBuilder<? extends PropertyBridge> builder) {
		mappingHelper.getIndexModelBinder().addPropertyBridge( bindingContext, modelPath, builder )
				.ifPresent( boundPropertyBridges::add );
	}

	@Override
	@SuppressWarnings( {"rawtypes", "unchecked"} )
	public void identifierBridge(BridgeBuilder<? extends IdentifierBridge<?>> builder) {
		PojoGenericTypeModel<P> propertyTypeModel = modelPath.getPropertyModel().getTypeModel();
		IdentifierBridge<P> bridge = mappingHelper.getIndexModelBinder().createIdentifierBridge( modelPath, builder );
		identityMappingCollector.identifierBridge( propertyTypeModel, modelPath.getPropertyHandle(), bridge );
	}

	@Override
	public PojoMappingCollectorValueNode value(ContainerValueExtractorPath extractorPath) {
		if ( !extractorPath.isEmpty() ) {
			PojoIndexingProcessorContainerElementNodeBuilder<P, ? super P, ?> containerElementNodeBuilder =
					containerElementNodeBuilders.get( extractorPath );
			if ( containerElementNodeBuilder == null && !containerElementNodeBuilders.containsKey( extractorPath ) ) {
				BoundContainerValueExtractorPath<P, ?> boundExtractorPath =
						mappingHelper.getIndexModelBinder().bindExtractorPath(
								modelPath.getPropertyModel().getTypeModel(),
								extractorPath
						);
				ContainerValueExtractorPath explicitExtractorPath = boundExtractorPath.getExtractorPath();
				if ( !explicitExtractorPath.isEmpty() ) {
					// Check whether the path was already encountered as an explicit path
					containerElementNodeBuilder = containerElementNodeBuilders.get( explicitExtractorPath );
					if ( containerElementNodeBuilder == null ) {
						containerElementNodeBuilder = createContainerElementNodeBuilder( boundExtractorPath );
					}
				}
				containerElementNodeBuilders.put( explicitExtractorPath, containerElementNodeBuilder );
				containerElementNodeBuilders.put( extractorPath, containerElementNodeBuilder );
			}
			if ( containerElementNodeBuilder != null ) {
				return containerElementNodeBuilder.value();
			}
		}
		return valueWithoutExtractorBuilderDelegate;
	}

	/*
	 * This generic method is necessary to make it clear to the compiler
	 * that the extracted type and extractor have compatible generic arguments.
	 */
	private <V> PojoIndexingProcessorContainerElementNodeBuilder<P, ? super P, V> createContainerElementNodeBuilder(
			BoundContainerValueExtractorPath<P, V> boundExtractorPath) {
		ContainerValueExtractor<? super P, V> extractor =
				mappingHelper.getIndexModelBinder().createExtractors( boundExtractorPath );
		BoundPojoModelPathValueNode<T, P, V> containerElementPath = modelPath.value( boundExtractorPath );
		return new PojoIndexingProcessorContainerElementNodeBuilder<>(
				containerElementPath, extractor,
				mappingHelper, bindingContext
		);
	}

	@Override
	BoundPojoModelPath getModelPath() {
		return modelPath;
	}

	Optional<PojoIndexingProcessorPropertyNode<T, P>> build(
			PojoIndexingDependencyCollectorTypeNode<T> parentDependencyCollector) {
		@SuppressWarnings("unchecked") // We know from the property model that this handle returns a result of type P
		PojoIndexingDependencyCollectorPropertyNode<T, P> propertyDependencyCollector =
				(PojoIndexingDependencyCollectorPropertyNode<T, P>)
						parentDependencyCollector.property( modelPath.getPropertyHandle() );

		Collection<PropertyBridge> immutableBridges = boundPropertyBridges.isEmpty()
				? Collections.emptyList() : new ArrayList<>();
		for ( BoundPropertyBridge<P> boundBridge : boundPropertyBridges ) {
			immutableBridges.add( boundBridge.getBridge() );
			boundBridge.getPojoModelRootElement().contributeDependencies( propertyDependencyCollector );
		}
		Collection<PojoIndexingProcessor<? super P>> valueWithoutExtractorNodes =
				valueWithoutExtractorBuilderDelegate.build( propertyDependencyCollector );
		Collection<PojoIndexingProcessor<? super P>> immutableNestedNodes =
				valueWithoutExtractorNodes.isEmpty() && containerElementNodeBuilders.isEmpty()
				? Collections.emptyList()
				: new ArrayList<>( valueWithoutExtractorNodes.size() + containerElementNodeBuilders.size() );
		if ( !valueWithoutExtractorNodes.isEmpty() ) {
			immutableNestedNodes.addAll( valueWithoutExtractorNodes );
		}
		containerElementNodeBuilders.values().stream()
				.distinct() // Necessary because the default extractor path has two possible keys with the same value
				.filter( Objects::nonNull )
				.map( builder -> builder.build( propertyDependencyCollector ) )
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
