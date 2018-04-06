/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.dirtiness.impl;

import java.util.stream.Stream;

import org.hibernate.search.v6poc.entity.pojo.extractor.ContainerValueExtractor;
import org.hibernate.search.v6poc.util.impl.common.ToStringTreeBuilder;

public class PojoImplicitReindexingResolverContainerElementNode<C, V> extends PojoImplicitReindexingResolver<C> {

	private final ContainerValueExtractor<C, V> extractor;
	private final boolean markForReindexing;
	private final PojoImplicitReindexingResolver<V> valueTypeNode;

	public PojoImplicitReindexingResolverContainerElementNode(ContainerValueExtractor<C, V> extractor,
			boolean markForReindexing, PojoImplicitReindexingResolver<V> valueTypeNode) {
		this.extractor = extractor;
		this.markForReindexing = markForReindexing;
		this.valueTypeNode = valueTypeNode;
	}

	@Override
	public void appendTo(ToStringTreeBuilder builder) {
		builder.attribute( "class", getClass().getSimpleName() );
		builder.attribute( "extractor", extractor );
		builder.attribute( "markForReindexing", markForReindexing );
		builder.attribute( "valueTypeNode", valueTypeNode );
	}

	@Override
	public void resolveEntitiesToReindex(PojoReindexingCollector collector, C dirty) {
		try ( Stream<V> stream = extractor.extract( dirty ) ) {
			stream.forEach( sourceItem -> resolveDirtyForItem( collector, sourceItem ) );
		}
	}

	private void resolveDirtyForItem(PojoReindexingCollector collector, V dirtyItem) {
		if ( dirtyItem != null ) {
			if ( markForReindexing ) {
				collector.markForReindexing( dirtyItem );
			}
			if ( valueTypeNode != null ) {
				valueTypeNode.resolveEntitiesToReindex( collector, dirtyItem );
			}
		}
	}
}
