/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.model.path.impl;

/**
 * *
 * @param <P> The parent type of this path, i.e. the type from which the value is retrieved.
 * @param <T> The value type of this path, i.e. the type of values at the end of this path.
 */
public abstract class PojoModelPathValueNode<P, T> extends PojoModelPath {

	PojoModelPathValueNode() {
		// Package-protected constructor
	}

	/**
	 * @return The model path to the type holding the value represented by this path.
	 * For a property, this will be the type on which the property is accessed.
	 * For a container element, this will be the type on which the container property is accessed.
	 */
	public abstract PojoModelPathTypeNode<P> parentType();

	/**
	 * @return The model path to the type of this path.
	 * For a property, this will be a child path representing the type of the property.
	 * For a container element, this will be a child path representing the type of the container element.
	 */
	public abstract PojoModelPathTypeNode<T> type();
}
