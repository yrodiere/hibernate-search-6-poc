/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.model.tree.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoTypeModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PropertyHandle;

/**
 * @param <T> The represented type.
 */
public class PojoModelTreeTypeNode<T> extends PojoModelTreeNode {

	private final PojoModelTreeValueNode<?, ?, T> parent;
	private final PojoTypeModel<T> typeModel;

	private final Map<PropertyHandle, PojoModelTreePropertyNode<T, ?>> properties = new LinkedHashMap<>();
	private final Collection<PojoModelTreePropertyNode<T, ?>> children =
			Collections.unmodifiableCollection( properties.values() );

	PojoModelTreeTypeNode(PojoModelTreeValueNode<?, ?, T> parent, PojoTypeModel<T> typeModel) {
		this.parent = parent;
		this.typeModel = typeModel;
	}

	@Override
	public Optional<? extends PojoModelTreeNode> getParentOptional() {
		return Optional.ofNullable( parent );
	}

	@Override
	public Collection<PojoModelTreePropertyNode<T, ?>> getChildren() {
		return children;
	}

	public PojoModelTreeValueNode<?, ?, T> getParent() {
		return parent;
	}

	public PojoModelTreePropertyNode<T, ?> getOrCreateProperty(PropertyHandle propertyHandle) {
		PojoModelTreePropertyNode<T, ?> propertyNode = properties.get( propertyHandle );
		if ( propertyNode == null ) {
			propertyNode = new PojoModelTreePropertyNode<>(
					this, propertyHandle, typeModel.getProperty( propertyHandle.getName() )
			);
			properties.put( propertyHandle, propertyNode );
		}
		return propertyNode;
	}

	public Optional<PojoModelTreePropertyNode<T, ?>> getProperty(PropertyHandle propertyHandle) {
		return Optional.ofNullable( properties.get( propertyHandle ) );
	}

	public PojoTypeModel<T> getTypeModel() {
		return typeModel;
	}

	@Override
	void appendSelfPath(StringBuilder builder) {
		builder.append( "type " ).append( typeModel );
	}

}
