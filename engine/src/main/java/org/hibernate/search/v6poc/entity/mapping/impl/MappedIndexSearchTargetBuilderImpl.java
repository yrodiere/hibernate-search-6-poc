/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.mapping.impl;

import org.hibernate.search.v6poc.backend.index.spi.SearchTargetContextBuilder;
import org.hibernate.search.v6poc.entity.mapping.spi.MappedIndexManager;
import org.hibernate.search.v6poc.entity.mapping.spi.MappedIndexSearchTarget;
import org.hibernate.search.v6poc.entity.mapping.spi.MappedIndexSearchTargetBuilder;
import org.hibernate.search.v6poc.search.dsl.spi.SearchTargetContext;

public final class MappedIndexSearchTargetBuilderImpl implements MappedIndexSearchTargetBuilder {

	private final SearchTargetContextBuilder contextBuilder;

	MappedIndexSearchTargetBuilderImpl(SearchTargetContextBuilder contextBuilder) {
		this.contextBuilder = contextBuilder;
	}

	@Override
	public MappedIndexSearchTargetBuilder add(MappedIndexManager other) {
		( (MappedIndexManagerImpl<?>) other ).indexManager.addToSearchTargetContext( contextBuilder );
		return this;
	}

	@Override
	public MappedIndexSearchTarget build() {
		SearchTargetContext<?> context = contextBuilder.build();
		return new MappedIndexSearchTargetImpl( context );
	}

}
