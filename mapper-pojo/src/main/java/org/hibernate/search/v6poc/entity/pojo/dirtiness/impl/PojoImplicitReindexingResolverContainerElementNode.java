/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.dirtiness.impl;

import java.util.stream.Stream;

import org.hibernate.search.v6poc.entity.pojo.extractor.ContainerValueExtractor;

public class PojoImplicitReindexingResolverContainerElementNode<P, T> implements PojoImplicitReindexingResolver<P> {

	private final ContainerValueExtractor<P, T> extractor;
	private final boolean markForReindexing;
	private final PojoImplicitReindexingResolverTypeNode<T> valueTypeNode;

	public PojoImplicitReindexingResolverContainerElementNode(ContainerValueExtractor<P, T> extractor,
			boolean markForReindexing, PojoImplicitReindexingResolverTypeNode<T> valueTypeNode) {
		this.extractor = extractor;
		this.markForReindexing = markForReindexing;
		this.valueTypeNode = valueTypeNode;
	}

	@Override
	public void resolveEntitiesToReindex(EntityReindexingCollector collector, P dirty) {
		try ( Stream<T> stream = extractor.extract( dirty ) ) {
			stream.forEach( sourceItem -> resolveDirtyForItem( collector, sourceItem ) );
		}
	}

	private void resolveDirtyForItem(EntityReindexingCollector collector, T dirtyItem) {
		if ( markForReindexing ) {
			collector.markForReindexing( dirtyItem );
		}
		if ( valueTypeNode != null ) {
			valueTypeNode.resolveEntitiesToReindex( collector, dirtyItem );
		}
	}
}
