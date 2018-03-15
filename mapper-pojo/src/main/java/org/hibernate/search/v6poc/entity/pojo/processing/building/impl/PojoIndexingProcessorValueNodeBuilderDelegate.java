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
import java.util.Optional;
import java.util.Set;

import org.hibernate.search.v6poc.backend.document.model.ObjectFieldStorage;
import org.hibernate.search.v6poc.entity.mapping.building.spi.FieldModelContributor;
import org.hibernate.search.v6poc.entity.mapping.building.spi.IndexModelBindingContext;
import org.hibernate.search.v6poc.entity.pojo.bridge.ValueBridge;
import org.hibernate.search.v6poc.entity.pojo.bridge.mapping.BridgeBuilder;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoIdentityMappingCollector;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoMappingCollectorValueNode;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoMappingHelper;
import org.hibernate.search.v6poc.entity.pojo.model.path.impl.PojoModelPathTypeNode;
import org.hibernate.search.v6poc.entity.pojo.model.path.impl.PojoModelPathValueNode;
import org.hibernate.search.v6poc.entity.pojo.processing.impl.PojoIndexingProcessor;
import org.hibernate.search.v6poc.entity.pojo.processing.impl.PojoIndexingProcessorValueBridgeNode;

public class PojoIndexingProcessorValueNodeBuilderDelegate<T> implements PojoMappingCollectorValueNode {

	private final PojoModelPathValueNode<?, T> modelPath;
	private final String defaultName;

	private final PojoMappingHelper mappingHelper;
	private final IndexModelBindingContext bindingContext;

	private final Collection<PojoIndexingProcessorValueBridgeNode<? super T, ?>> bridgeNodes = new ArrayList<>();

	private final Collection<PojoIndexingProcessorTypeNodeBuilder<? super T>> typeNodeBuilders = new ArrayList<>();

	PojoIndexingProcessorValueNodeBuilderDelegate(
			PojoModelPathValueNode<?, T> modelPath, String defaultName,
			PojoMappingHelper mappingHelper, IndexModelBindingContext bindingContext) {
		this.modelPath = modelPath;
		this.defaultName = defaultName;
		this.mappingHelper = mappingHelper;
		this.bindingContext = bindingContext;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + modelPath + "]";
	}

	@Override
	public void valueBridge(BridgeBuilder<? extends ValueBridge<?, ?>> builder, String fieldName,
			FieldModelContributor fieldModelContributor) {
		String defaultedFieldName = fieldName;
		if ( defaultedFieldName == null ) {
			defaultedFieldName = defaultName;
		}

		mappingHelper.getIndexModelBinder().addValueBridge(
				bindingContext, modelPath.type().getTypeModel(), builder, defaultedFieldName,
				fieldModelContributor
		)
				.ifPresent( bridgeNodes::add );
	}

	@Override
	public void indexedEmbedded(String relativePrefix, ObjectFieldStorage storage,
			Integer maxDepth, Set<String> includePaths) {
		String defaultedRelativePrefix = relativePrefix;
		if ( defaultedRelativePrefix == null ) {
			defaultedRelativePrefix = defaultName + ".";
		}

		Optional<IndexModelBindingContext> nestedBindingContextOptional = bindingContext.addIndexedEmbeddedIfIncluded(
				modelPath.parentType().getTypeModel().getRawType(),
				defaultedRelativePrefix, storage, maxDepth, includePaths
		);
		nestedBindingContextOptional.ifPresent( nestedBindingContext -> {
			PojoModelPathTypeNode<T> embeddedTypeModelPath = modelPath.type();
			PojoIndexingProcessorTypeNodeBuilder<T> nestedProcessorBuilder = new PojoIndexingProcessorTypeNodeBuilder<>(
					embeddedTypeModelPath, mappingHelper, nestedBindingContext,
					// Do NOT propagate the identity mapping collector to IndexedEmbeddeds
					PojoIdentityMappingCollector.noOp()
			);
			typeNodeBuilders.add( nestedProcessorBuilder );
			mappingHelper.getContributorProvider().forEach(
					embeddedTypeModelPath.getTypeModel().getRawType(),
					c -> c.contributeMapping( nestedProcessorBuilder )
			);
		} );
	}

	Collection<PojoIndexingProcessor<? super T>> build() {
		Collection<PojoIndexingProcessor<? super T>> immutableNestedNodes =
				bridgeNodes.isEmpty() && typeNodeBuilders.isEmpty()
						? Collections.emptyList()
						: new ArrayList<>( bridgeNodes.size() + typeNodeBuilders.size() );
		immutableNestedNodes.addAll( bridgeNodes );
		typeNodeBuilders.stream()
				.map( AbstractPojoProcessorNodeBuilder::build )
				.filter( Optional::isPresent )
				.map( Optional::get )
				.forEach( immutableNestedNodes::add );

		return immutableNestedNodes;
	}
}
