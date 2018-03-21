/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.processing.building.impl;

import java.util.Collection;
import java.util.Optional;

import org.hibernate.search.v6poc.entity.mapping.building.spi.IndexModelBindingContext;
import org.hibernate.search.v6poc.entity.pojo.extractor.ContainerValueExtractor;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoMappingCollectorValueNode;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoMappingHelper;
import org.hibernate.search.v6poc.entity.pojo.model.tree.impl.PojoModelTreeValueNode;
import org.hibernate.search.v6poc.entity.pojo.processing.impl.PojoIndexingProcessor;
import org.hibernate.search.v6poc.entity.pojo.processing.impl.PojoIndexingProcessorContainerElementNode;

public class PojoIndexingProcessorContainerElementNodeBuilder<C, T> extends AbstractPojoProcessorNodeBuilder<C> {

	private final PojoModelTreeValueNode<?, ? extends C, T> treeNode;
	private final ContainerValueExtractor<C, T> extractor;

	private final PojoIndexingProcessorValueNodeBuilderDelegate<T> valueNodeProcessorCollectionBuilder;

	PojoIndexingProcessorContainerElementNodeBuilder(PojoModelTreeValueNode<?, ? extends C, T> treeNode,
			ContainerValueExtractor<C, T> extractor,
			PojoMappingHelper mappingHelper, IndexModelBindingContext bindingContext) {
		super( mappingHelper, bindingContext );
		this.treeNode = treeNode;
		this.extractor = extractor;

		valueNodeProcessorCollectionBuilder = new PojoIndexingProcessorValueNodeBuilderDelegate<>(
				treeNode, mappingHelper, bindingContext
		);
	}

	public PojoMappingCollectorValueNode value() {
		return valueNodeProcessorCollectionBuilder;
	}

	@Override
	PojoModelTreeValueNode<?, ? extends C, T> getModelTreeNode() {
		return treeNode;
	}

	@Override
	Optional<PojoIndexingProcessorContainerElementNode<C, T>> build() {
		Collection<PojoIndexingProcessor<? super T>> immutableNestedProcessors =
				valueNodeProcessorCollectionBuilder.build();

		if ( immutableNestedProcessors.isEmpty() ) {
			/*
			 * If this processor doesn't have any bridge, nor any nested processor,
			 * it is useless and we don't need to build it
			 */
			return Optional.empty();
		}
		else {
			return Optional.of( new PojoIndexingProcessorContainerElementNode<>(
					extractor, immutableNestedProcessors
			) );
		}
	}
}
