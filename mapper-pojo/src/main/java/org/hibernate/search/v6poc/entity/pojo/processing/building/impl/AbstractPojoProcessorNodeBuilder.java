/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.processing.building.impl;

import java.util.Optional;

import org.hibernate.search.v6poc.entity.mapping.building.spi.IndexModelBindingContext;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoMappingHelper;
import org.hibernate.search.v6poc.entity.pojo.model.tree.impl.PojoModelTreeNode;
import org.hibernate.search.v6poc.entity.pojo.processing.impl.PojoIndexingProcessor;

abstract class AbstractPojoProcessorNodeBuilder<T> {

	final PojoMappingHelper mappingHelper;
	final IndexModelBindingContext bindingContext;

	AbstractPojoProcessorNodeBuilder(
			PojoMappingHelper mappingHelper, IndexModelBindingContext bindingContext) {
		this.mappingHelper = mappingHelper;
		this.bindingContext = bindingContext;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + getModelTreeNode() + "]";
	}

	abstract PojoModelTreeNode getModelTreeNode();

	abstract Optional<? extends PojoIndexingProcessor<T>> build();
}
