/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.model.path.impl;

import org.hibernate.search.v6poc.entity.pojo.extractor.ContainerValueExtractor;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoPropertyModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoTypeModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PropertyHandle;

public class PojoModelPathPropertyNode<P, T> extends PojoModelPathValueNode<P, T> {

	private final PojoModelPathTypeNode<P> parent;
	private final PropertyHandle propertyHandle;
	private final PojoPropertyModel<T> propertyModel;
	private PojoModelPathTypeNode<T> propertyTypePathNode;

	PojoModelPathPropertyNode(PojoModelPathTypeNode<P> parent, PropertyHandle propertyHandle,
			PojoPropertyModel<T> propertyModel) {
		this.parent = parent;
		this.propertyHandle = propertyHandle;
		this.propertyModel = propertyModel;
	}

	@Override
	public PojoModelPathTypeNode<P> parent() {
		return parent;
	}

	@Override
	public PojoModelPathTypeNode<P> parentType() {
		return parent;
	}

	@Override
	public PojoModelPathTypeNode<T> type() {
		if ( propertyTypePathNode == null ) {
			propertyTypePathNode = new PojoModelPathTypeNode<>( this, propertyModel.getTypeModel() );
		}
		return propertyTypePathNode;
	}

	public <U> PojoModelPathContainerElementNode<P, ? super T, U> containerElement(
			ContainerValueExtractor<? super T, U> extractor, PojoTypeModel<U> elementTypeModel) {
		return new PojoModelPathContainerElementNode<>( this, extractor, elementTypeModel );
	}

	public PropertyHandle getPropertyHandle() {
		return propertyHandle;
	}

	public PojoPropertyModel<T> getPropertyModel() {
		return propertyModel;
	}

	@Override
	void appendSelfPath(StringBuilder builder) {
		builder.append( "." ).append( propertyHandle.getName() );
	}
}
