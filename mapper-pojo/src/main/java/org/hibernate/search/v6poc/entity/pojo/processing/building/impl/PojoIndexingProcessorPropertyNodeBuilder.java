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
import org.hibernate.search.v6poc.entity.pojo.extractor.ContainerValueExtractor;
import org.hibernate.search.v6poc.entity.pojo.extractor.impl.BoundContainerValueExtractorPath;
import org.hibernate.search.v6poc.entity.pojo.extractor.impl.ContainerValueExtractorPath;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoIdentityMappingCollector;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoMappingCollectorPropertyNode;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoMappingCollectorValueNode;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoMappingHelper;
import org.hibernate.search.v6poc.entity.pojo.model.impl.PojoModelPropertyRootElement;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoGenericTypeModel;
import org.hibernate.search.v6poc.entity.pojo.model.tree.impl.PojoModelTreePropertyNode;
import org.hibernate.search.v6poc.entity.pojo.model.tree.impl.PojoModelTreeValueNode;
import org.hibernate.search.v6poc.entity.pojo.processing.impl.PojoIndexingProcessor;
import org.hibernate.search.v6poc.entity.pojo.processing.impl.PojoIndexingProcessorPropertyNode;

public class PojoIndexingProcessorPropertyNodeBuilder<P, T> extends AbstractPojoProcessorNodeBuilder<P>
		implements PojoMappingCollectorPropertyNode {

	private final PojoModelTreePropertyNode<P, T> treeNode;
	private final PojoModelPropertyRootElement pojoModelRootElement;

	private final PojoIdentityMappingCollector identityMappingCollector;

	private final Collection<PropertyBridge> propertyBridges = new ArrayList<>();
	private final PojoIndexingProcessorValueNodeBuilderDelegate<T> valueWithoutExtractorBuilderDelegate;
	private Map<ContainerValueExtractorPath, PojoIndexingProcessorContainerElementNodeBuilder<? super T, ?>>
			containerElementNodeBuilders = new HashMap<>();

	PojoIndexingProcessorPropertyNodeBuilder(
			PojoModelTreePropertyNode<P, T> treeNode,
			PojoMappingHelper mappingHelper, IndexModelBindingContext bindingContext,
			PojoIdentityMappingCollector identityMappingCollector) {
		super( mappingHelper, bindingContext );

		this.treeNode = treeNode;

		// FIXME do something more with the pojoModelRootElement, to be able to use it in containedIn processing in particular
		this.pojoModelRootElement = new PojoModelPropertyRootElement(
				treeNode.getPropertyModel(), mappingHelper.getAugmentedTypeModelProvider()
		);

		this.identityMappingCollector = identityMappingCollector;

		PojoGenericTypeModel<T> propertyTypeModel = treeNode.getPropertyModel().getTypeModel();
		PojoModelTreeValueNode<P, T, T> valueWithoutExtractorTreeNode = treeNode.getOrCreateValue(
				BoundContainerValueExtractorPath.noExtractors( propertyTypeModel )
		);
		this.valueWithoutExtractorBuilderDelegate = new PojoIndexingProcessorValueNodeBuilderDelegate<>(
				valueWithoutExtractorTreeNode, mappingHelper, bindingContext
		);
	}

	@Override
	public void bridge(BridgeBuilder<? extends PropertyBridge> builder) {
		mappingHelper.getIndexModelBinder().addPropertyBridge( bindingContext, pojoModelRootElement, builder )
				.ifPresent( propertyBridges::add );
	}

	@Override
	@SuppressWarnings( {"rawtypes", "unchecked"} )
	public void identifierBridge(BridgeBuilder<? extends IdentifierBridge<?>> builder) {
		PojoGenericTypeModel<T> propertyTypeModel = treeNode.getPropertyModel().getTypeModel();
		IdentifierBridge<T> bridge = mappingHelper.getIndexModelBinder().createIdentifierBridge(
				pojoModelRootElement, propertyTypeModel, builder
		);
		identityMappingCollector.identifierBridge( propertyTypeModel, treeNode.getPropertyHandle(), bridge );
	}

	@Override
	public PojoMappingCollectorValueNode value(ContainerValueExtractorPath extractorPath) {
		if ( !extractorPath.isEmpty() ) {
			PojoIndexingProcessorContainerElementNodeBuilder<? super T, ?> containerElementNodeBuilder =
					containerElementNodeBuilders.get( extractorPath );
			if ( containerElementNodeBuilder == null && !containerElementNodeBuilders.containsKey( extractorPath ) ) {
				BoundContainerValueExtractorPath<T, ?> boundExtractorPath =
						mappingHelper.getIndexModelBinder().bindExtractorPath(
								treeNode.getPropertyModel().getTypeModel(),
								ContainerValueExtractorPath.defaultExtractors()
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
	private <V> PojoIndexingProcessorContainerElementNodeBuilder<? super T, V> createContainerElementNodeBuilder(
			BoundContainerValueExtractorPath<T, V> boundExtractorPath) {
		ContainerValueExtractor<? super T, V> extractor = mappingHelper.getIndexModelBinder()
				.createExtractors( boundExtractorPath );
		PojoModelTreeValueNode<P, T, V> extractedValueNode =
				treeNode.getOrCreateValue( boundExtractorPath );
		return new PojoIndexingProcessorContainerElementNodeBuilder<>(
				extractedValueNode, extractor, mappingHelper, bindingContext
		);
	}

	@Override
	PojoModelTreePropertyNode<P, T> getModelTreeNode() {
		return treeNode;
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
					treeNode.getPropertyHandle(), immutableBridges, immutableNestedNodes
			) );
		}
	}
}
