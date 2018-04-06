/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.dirtiness.impl;

import java.util.Collection;

import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoCaster;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoRuntimeIntrospector;
import org.hibernate.search.v6poc.util.impl.common.ToStringTreeBuilder;

public class PojoImplicitReindexingResolverCastedTypeNode<T, U> extends PojoImplicitReindexingResolver<T> {

	private final PojoCaster<? super U> caster;
	private final boolean markForReindexing;
	private final Collection<PojoImplicitReindexingResolverPropertyNode<? super U, ?>> propertyNodes;

	public PojoImplicitReindexingResolverCastedTypeNode(PojoCaster<? super U> caster,
			boolean markForReindexing,
			Collection<PojoImplicitReindexingResolverPropertyNode<? super U, ?>> propertyNodes) {
		this.caster = caster;
		this.markForReindexing = markForReindexing;
		this.propertyNodes = propertyNodes;
	}

	@Override
	public void appendTo(ToStringTreeBuilder builder) {
		builder.attribute( "class", getClass().getSimpleName() );
		builder.attribute( "caster", caster );
		builder.attribute( "markForReindexing", markForReindexing );
		builder.startList( "propertyNodes" );
		for ( PojoImplicitReindexingResolverPropertyNode<?, ?> propertyNode : propertyNodes ) {
			builder.value( propertyNode );
		}
		builder.endList();
	}

	@Override
	@SuppressWarnings( "unchecked" ) // We can only cast to the raw type, if U is generic we need an unchecked cast
	public void resolveEntitiesToReindex(PojoReindexingCollector collector,
			PojoRuntimeIntrospector runtimeIntrospector, T dirty) {
		U cast = (U) caster.castOrNull( runtimeIntrospector.unproxy( dirty ) );
		if ( cast != null ) {
			if ( markForReindexing ) {
				collector.markForReindexing( cast );
			}
			for ( PojoImplicitReindexingResolverPropertyNode<? super U, ?> propertyNode : propertyNodes ) {
				propertyNode.resolveEntitiesToReindex( collector, runtimeIntrospector, cast );
			}
		}
	}
}
