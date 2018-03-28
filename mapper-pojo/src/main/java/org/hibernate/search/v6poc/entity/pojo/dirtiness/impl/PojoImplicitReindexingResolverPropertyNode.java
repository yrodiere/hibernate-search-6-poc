/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.dirtiness.impl;

import java.util.Collection;

import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoRuntimeIntrospector;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PropertyHandle;
import org.hibernate.search.v6poc.util.impl.common.ToStringTreeBuilder;

public class PojoImplicitReindexingResolverPropertyNode<T, P> extends PojoImplicitReindexingResolver<T> {

	private final PropertyHandle handle;
	private final Collection<PojoImplicitReindexingResolver<? super P>> nestedNodes;

	public PojoImplicitReindexingResolverPropertyNode(PropertyHandle handle,
			Collection<PojoImplicitReindexingResolver<? super P>> nestedNodes) {
		this.handle = handle;
		this.nestedNodes = nestedNodes;
	}

	@Override
	public void appendTo(ToStringTreeBuilder builder) {
		builder.attribute( "class", getClass().getSimpleName() );
		builder.attribute( "handle", handle );
		builder.startList( "nestedNodes" );
		for ( PojoImplicitReindexingResolver<?> nestedNode : nestedNodes ) {
			builder.value( nestedNode );
		}
		builder.endList();
	}

	@Override
	public void resolveEntitiesToReindex(PojoReindexingCollector collector,
			PojoRuntimeIntrospector runtimeIntrospector, T dirty) {
		// TODO add generic type parameters to property handles
		P propertyValue = (P) handle.get( dirty );
		if ( propertyValue != null ) {
			for ( PojoImplicitReindexingResolver<? super P> node : nestedNodes ) {
				node.resolveEntitiesToReindex( collector, runtimeIntrospector, propertyValue );
			}
		}
	}
}
