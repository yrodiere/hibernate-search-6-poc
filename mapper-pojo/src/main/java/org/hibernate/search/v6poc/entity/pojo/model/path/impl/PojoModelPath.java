/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.model.path.impl;

import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoRawTypeModel;

/**
 * Represents an arbitrarily long access path when walking the POJO model. For instance the path could be:
 * <code>
 * Type A =&gt; property "propertyOfA" =&gt; extractor "MapValueExtractor" =&gt; Type B =&gt; property "propertyOfB"
 * </code>
 */
public abstract class PojoModelPath {

	public static <T> PojoModelPathTypeNode<T> root(PojoRawTypeModel<T> typeModel) {
		return new PojoModelPathTypeNode<>( null, typeModel );
	}

	PojoModelPath() {
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

	public abstract PojoModelPath parent();

	abstract void appendSelfPath(StringBuilder builder);

	private void appendPath(StringBuilder builder) {
		PojoModelPath parent = parent();
		if ( parent == null ) {
			appendSelfPath( builder );
		}
		else {
			parent.appendPath( builder );
			builder.append( " => " );
			appendSelfPath( builder );
		}
	}
}
