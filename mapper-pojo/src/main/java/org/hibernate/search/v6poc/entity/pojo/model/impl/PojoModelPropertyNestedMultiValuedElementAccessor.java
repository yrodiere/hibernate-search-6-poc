/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.model.impl;

import java.util.stream.Stream;

import org.hibernate.search.v6poc.entity.pojo.model.PojoElement;
import org.hibernate.search.v6poc.entity.pojo.model.PojoModelElementAccessor;
import org.hibernate.search.v6poc.entity.pojo.model.PojoModelMultiValuedElementAccessor;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PropertyHandle;

class PojoModelPropertyNestedMultiValuedElementAccessor<T> implements PojoModelMultiValuedElementAccessor<T> {

	private final PojoModelMultiValuedElementAccessor<?> parent;
	private final PropertyHandle handle;

	PojoModelPropertyNestedMultiValuedElementAccessor(PojoModelMultiValuedElementAccessor<?> parent, PropertyHandle handle) {
		this.parent = parent;
		this.handle = handle;
	}

	@Override
	public Stream<T> read(PojoElement bridgedElement) {
		return parent.read( bridgedElement )
				.map( parentValue -> {
					if ( parentValue != null ) {
						return (T) handle.get( parentValue );
					}
					else {
						return null;
					}
				} );
	}

}