/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.dirtiness.impl;

import java.util.Collection;

import org.hibernate.search.v6poc.util.impl.common.ToStringTreeBuilder;

public class PojoImplicitReindexingResolverTypeNode<T> extends PojoImplicitReindexingResolver<T> {

	private final Collection<PojoImplicitReindexingResolverPropertyNode<? super T, ?>> propertyNodes;

	public PojoImplicitReindexingResolverTypeNode(
			Collection<PojoImplicitReindexingResolverPropertyNode<? super T, ?>> propertyNodes) {
		this.propertyNodes = propertyNodes;
	}

	@Override
	public void appendTo(ToStringTreeBuilder builder) {
		builder.attribute( "class", getClass().getSimpleName() );
		builder.startList( "propertyNodes" );
		for ( PojoImplicitReindexingResolverPropertyNode<?, ?> propertyNode : propertyNodes ) {
			builder.value( propertyNode );
		}
		builder.endList();
	}

	@Override
	public void resolveEntitiesToReindex(PojoReindexingCollector collector, T dirty) {
		for ( PojoImplicitReindexingResolverPropertyNode<? super T, ?> propertyNode : propertyNodes ) {
			propertyNode.resolveEntitiesToReindex( collector, dirty );
		}
	}
}
