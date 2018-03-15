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
import org.hibernate.search.v6poc.entity.mapping.building.spi.TypeMetadataContributorProvider;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoIndexModelBinderImpl;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoMappingCollectorValueNode;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoTypeMetadataContributor;
import org.hibernate.search.v6poc.entity.pojo.model.path.impl.PojoModelPathContainerElementNode;
import org.hibernate.search.v6poc.entity.pojo.model.path.impl.PojoModelPathPropertyNode;
import org.hibernate.search.v6poc.entity.pojo.processing.impl.PojoIndexingProcessor;
import org.hibernate.search.v6poc.entity.pojo.processing.impl.PojoIndexingProcessorContainerElementNode;

public class PojoIndexingProcessorContainerElementNodeBuilder<C, T> extends AbstractPojoProcessorNodeBuilder<C> {

	private final PojoModelPathContainerElementNode<?, C, T> modelPath;

	private final PojoIndexingProcessorValueNodeBuilderDelegate<T> valueNodeProcessorCollectionBuilder;

	PojoIndexingProcessorContainerElementNodeBuilder(PojoModelPathContainerElementNode<?, C, T> modelPath,
			TypeMetadataContributorProvider<PojoTypeMetadataContributor> contributorProvider,
			PojoIndexModelBinderImpl indexModelBinder, IndexModelBindingContext bindingContext) {
		super( contributorProvider, indexModelBinder, bindingContext );
		this.modelPath = modelPath;

		PojoModelPathPropertyNode<?, ? extends C> propertyPath = modelPath.parent();

		valueNodeProcessorCollectionBuilder = new PojoIndexingProcessorValueNodeBuilderDelegate<>(
				modelPath, propertyPath.getPropertyHandle().getName(),
				contributorProvider, indexModelBinder, bindingContext
		);
	}

	public PojoMappingCollectorValueNode value() {
		return valueNodeProcessorCollectionBuilder;
	}

	@Override
	PojoModelPathContainerElementNode<?, C, T> getModelPath() {
		return modelPath;
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
					modelPath.getExtractor(), immutableNestedProcessors
			) );
		}
	}
}
