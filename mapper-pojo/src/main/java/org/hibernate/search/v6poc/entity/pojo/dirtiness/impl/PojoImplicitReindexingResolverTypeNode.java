/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.dirtiness.impl;

import java.util.Collection;

public class PojoImplicitReindexingResolverTypeNode<T> implements PojoImplicitReindexingResolver<T> {

	private final Collection<PojoImplicitReindexingResolverPropertyNode<? super T, ?>> propertyNodes;

	public PojoImplicitReindexingResolverTypeNode(
			Collection<PojoImplicitReindexingResolverPropertyNode<? super T, ?>> propertyNodes) {
		this.propertyNodes = propertyNodes;
	}

	@Override
	public void resolveEntitiesToReindex(EntityReindexingCollector collector, T dirty) {
		for ( PojoImplicitReindexingResolverPropertyNode<? super T, ?> propertyNode : propertyNodes ) {
			propertyNode.resolveEntitiesToReindex( collector, dirty );
		}
	}
}
