/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.model.impl;

import java.util.stream.Stream;

import org.hibernate.search.v6poc.entity.pojo.extractor.ContainerValueExtractor;
import org.hibernate.search.v6poc.entity.pojo.model.PojoElement;
import org.hibernate.search.v6poc.entity.pojo.model.PojoModelMultiValuedElementAccessor;

class PojoModelMultiContainerNestedElementAccessor<C, T> implements PojoModelMultiValuedElementAccessor<T> {

	private final PojoModelMultiValuedElementAccessor<C> parent;
	private final ContainerValueExtractor<? super C, T> extractor;

	PojoModelMultiContainerNestedElementAccessor(PojoModelMultiValuedElementAccessor<C> parent,
			ContainerValueExtractor<? super C, T> extractor) {
		this.parent = parent;
		this.extractor = extractor;
	}

	@Override
	public Stream<T> read(PojoElement bridgedElement) {
		return parent.read( bridgedElement ).flatMap( extractor::extract );
	}

}