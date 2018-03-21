/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.dirtiness.impl;

import java.util.Collection;

import org.hibernate.search.v6poc.entity.pojo.model.spi.PropertyHandle;

public class PojoImplicitReindexingResolverPropertyNode<P, T> implements PojoImplicitReindexingResolver<P> {

	private final PropertyHandle handle;
	private final boolean markForReindexing;
	private final Collection<PojoImplicitReindexingResolver<? super T>> nestedNodes;

	public PojoImplicitReindexingResolverPropertyNode(PropertyHandle handle, boolean markForReindexing,
			Collection<PojoImplicitReindexingResolver<? super T>> nestedNodes) {
		this.handle = handle;
		this.markForReindexing = markForReindexing;
		this.nestedNodes = nestedNodes;
	}

	@Override
	public void resolveEntitiesToReindex(EntityReindexingCollector collector, P dirty) {
		// TODO add generic type parameters to property handles
		T propertyValue = (T) handle.get( dirty );
		if ( markForReindexing ) {
			collector.markForReindexing( dirty );
		}
		for ( PojoImplicitReindexingResolver<? super T> propertyContainingResolver : nestedNodes ) {
			propertyContainingResolver.resolveEntitiesToReindex( collector, propertyValue );
		}
	}
}
