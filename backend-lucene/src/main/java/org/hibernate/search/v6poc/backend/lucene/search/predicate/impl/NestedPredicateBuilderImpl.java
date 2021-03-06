/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.lucene.search.predicate.impl;

import org.hibernate.search.v6poc.backend.lucene.search.impl.LuceneQueries;
import org.hibernate.search.v6poc.search.predicate.spi.NestedPredicateBuilder;

import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.join.QueryBitSetProducer;
import org.apache.lucene.search.join.ScoreMode;
import org.apache.lucene.search.join.ToParentBlockJoinQuery;


/**
 * @author Guillaume Smet
 */
class NestedPredicateBuilderImpl extends AbstractSearchPredicateBuilder
		implements NestedPredicateBuilder<LuceneSearchPredicateBuilder> {

	private final String absoluteFieldPath;

	private LuceneSearchPredicateBuilder nestedBuilder;

	NestedPredicateBuilderImpl(String absoluteFieldPath) {
		this.absoluteFieldPath = absoluteFieldPath;
	}

	@Override
	public void nested(LuceneSearchPredicateBuilder nestedBuilder) {
		this.nestedBuilder = nestedBuilder;
	}

	@Override
	protected Query doBuild(LuceneSearchPredicateContext context) {
		LuceneSearchPredicateContext childContext = new LuceneSearchPredicateContext( absoluteFieldPath );

		BooleanQuery.Builder childQueryBuilder = new BooleanQuery.Builder();
		childQueryBuilder.add( LuceneQueries.childDocumentQuery(), Occur.FILTER );
		childQueryBuilder.add( LuceneQueries.nestedDocumentPathQuery( absoluteFieldPath ), Occur.FILTER );
		childQueryBuilder.add( nestedBuilder.build( childContext ), Occur.MUST );

		Query parentQuery;
		if ( context.getNestedPath() == null ) {
			parentQuery = LuceneQueries.mainDocumentQuery();
		}
		else {
			parentQuery = LuceneQueries.nestedDocumentPathQuery( context.getNestedPath() );
		}

		// TODO at some point we should have a parameter for the score mode
		return new ToParentBlockJoinQuery( childQueryBuilder.build(), new QueryBitSetProducer( parentQuery ), ScoreMode.Avg );
	}
}
