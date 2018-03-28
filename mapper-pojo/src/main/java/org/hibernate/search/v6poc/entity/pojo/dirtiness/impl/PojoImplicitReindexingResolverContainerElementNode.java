/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.dirtiness.impl;

import java.util.Collection;
import java.util.stream.Stream;

import org.hibernate.search.v6poc.entity.pojo.extractor.ContainerValueExtractor;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoRuntimeIntrospector;
import org.hibernate.search.v6poc.util.impl.common.ToStringTreeBuilder;

public class PojoImplicitReindexingResolverContainerElementNode<C, V> extends PojoImplicitReindexingResolver<C> {

	private final ContainerValueExtractor<C, V> extractor;
	private final Collection<PojoImplicitReindexingResolver<V>> nestedNodes;

	public PojoImplicitReindexingResolverContainerElementNode(ContainerValueExtractor<C, V> extractor,
			Collection<PojoImplicitReindexingResolver<V>> nestedNodes) {
		this.extractor = extractor;
		this.nestedNodes = nestedNodes;
	}

	@Override
	public void appendTo(ToStringTreeBuilder builder) {
		builder.attribute( "class", getClass().getSimpleName() );
		builder.attribute( "extractor", extractor );
		builder.startList( "nestedNodes" );
		for ( PojoImplicitReindexingResolver<?> nestedNode : nestedNodes ) {
			builder.value( nestedNode );
		}
		builder.endList();
	}

	@Override
	public void resolveEntitiesToReindex(PojoReindexingCollector collector,
			PojoRuntimeIntrospector runtimeIntrospector, C dirty) {
		try ( Stream<V> stream = extractor.extract( dirty ) ) {
			stream.forEach( sourceItem -> resolveDirtyForItem( collector, runtimeIntrospector, sourceItem ) );
		}
	}

	private void resolveDirtyForItem(PojoReindexingCollector collector,
			PojoRuntimeIntrospector runtimeIntrospector, V dirtyItem) {
		if ( dirtyItem != null ) {
			for ( PojoImplicitReindexingResolver<V> node : nestedNodes ) {
				node.resolveEntitiesToReindex( collector, runtimeIntrospector, dirtyItem );
			}
		}
	}
}
