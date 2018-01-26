/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.model.impl;

import org.hibernate.search.v6poc.entity.pojo.model.PojoElement;
import org.hibernate.search.v6poc.entity.pojo.model.PojoModelElementAccessor;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PropertyHandle;

class PojoPropertyAccessor<T> implements PojoModelElementAccessor<T> {

	private final PojoModelElementAccessor<?> parent;
	private final PropertyHandle handle;

	PojoPropertyAccessor(PojoModelElementAccessor<?> parent, PropertyHandle handle) {
		this.parent = parent;
		this.handle = handle;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<T> getType() {
		return (Class<T>) handle.getJavaType();
	}

	@Override
	public T read(PojoElement bridgedElement) {
		return (T) handle.get( parent.read( bridgedElement ) );
	}

}