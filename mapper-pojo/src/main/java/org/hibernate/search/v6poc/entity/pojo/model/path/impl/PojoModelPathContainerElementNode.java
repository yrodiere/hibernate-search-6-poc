/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.model.path.impl;

import org.hibernate.search.v6poc.entity.pojo.extractor.ContainerValueExtractor;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoTypeModel;

public class PojoModelPathContainerElementNode<P, C, T> extends PojoModelPathValueNode<P, T> {

	private final PojoModelPathPropertyNode<P, ? extends C> parent;
	private final ContainerValueExtractor<C, T> extractor;
	private final PojoTypeModel<T> elementTypeModel;
	private PojoModelPathTypeNode<T> elementTypePathNode;

	PojoModelPathContainerElementNode(PojoModelPathPropertyNode<P, ? extends C> parent,
			ContainerValueExtractor<C, T> extractor, PojoTypeModel<T> elementTypeModel) {
		this.parent = parent;
		this.extractor = extractor;
		this.elementTypeModel = elementTypeModel;
	}

	@Override
	public PojoModelPathPropertyNode<P, ? extends C> parent() {
		return parent;
	}

	@Override
	public PojoModelPathTypeNode<P> parentType() {
		return parent.parentType();
	}

	@Override
	public PojoModelPathTypeNode<T> type() {
		if ( elementTypePathNode == null ) {
			elementTypePathNode = new PojoModelPathTypeNode<>( this, elementTypeModel );
		}
		return elementTypePathNode;
	}

	public ContainerValueExtractor<C, T> getExtractor() {
		return extractor;
	}

	@Override
	void appendSelfPath(StringBuilder builder) {
		builder.append( "[" ).append( extractor ).append( "]" );
	}
}
