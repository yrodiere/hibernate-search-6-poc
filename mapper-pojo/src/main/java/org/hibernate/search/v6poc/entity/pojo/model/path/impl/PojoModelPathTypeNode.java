/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.model.path.impl;

import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoTypeModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PropertyHandle;

public class PojoModelPathTypeNode<T> extends PojoModelPath {

	private final PojoModelPathValueNode<?, T> parent;
	private final PojoTypeModel<T> typeModel;

	PojoModelPathTypeNode(PojoModelPathValueNode<?, T> parent, PojoTypeModel<T> typeModel) {
		this.parent = parent;
		this.typeModel = typeModel;
	}

	@Override
	public PojoModelPath parent() {
		return parent;
	}

	public PojoModelPathPropertyNode<T, ?> property(PropertyHandle propertyHandle) {
		return new PojoModelPathPropertyNode<>(
				this, propertyHandle, typeModel.getProperty( propertyHandle.getName() )
		);
	}

	public PojoTypeModel<T> getTypeModel() {
		return typeModel;
	}

	@Override
	void appendSelfPath(StringBuilder builder) {
		builder.append( "type " ).append( typeModel );
	}
}
