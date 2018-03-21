/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.model.tree.impl;

import java.util.Collection;
import java.util.Optional;

import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoRawTypeModel;

/**
 * Represents a node in a POJO tree.
 * <p>
 * Used to model the way Hibernate Search accesses a POJO during indexing, in particular.
 * <p>
 * The tree root is always a {@link PojoModelTreeTypeNode}.
 * A type node has {@link PojoModelTreePropertyNode}s as children, one per accessed property.
 * A property node has {@link PojoModelTreeValueNode} as children, one per list of extractors used to extract values.
 * A value node has zero or one child, always a {@link PojoModelTreeTypeNode} representing the type of that value.
 */
public abstract class PojoModelTreeNode {

	public static <T> PojoModelTreeTypeNode<T> root(PojoRawTypeModel<T> typeModel) {
		return new PojoModelTreeTypeNode<>( null, typeModel );
	}

	PojoModelTreeNode() {
		// Package-protected constructor
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder( getClass().getSimpleName() )
				.append( "[" );
		appendPath( builder );
		builder.append( "]" );
		return builder.toString();
	}

	public abstract Optional<? extends PojoModelTreeNode> getParentOptional();

	public abstract Collection<? extends PojoModelTreeNode> getChildren();

	abstract void appendSelfPath(StringBuilder builder);

	private void appendPath(StringBuilder builder) {
		Optional<? extends PojoModelTreeNode> parentOptional = getParentOptional();
		if ( parentOptional.isPresent() ) {
			PojoModelTreeNode parent = parentOptional.get();
			parent.appendPath( builder );
			builder.append( " => " );
			appendSelfPath( builder );
		}
		else {
			appendSelfPath( builder );
		}
	}
}
