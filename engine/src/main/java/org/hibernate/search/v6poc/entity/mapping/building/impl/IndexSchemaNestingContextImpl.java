/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.mapping.building.impl;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.hibernate.search.v6poc.backend.document.model.spi.IndexSchemaNestingContext;
import org.hibernate.search.v6poc.entity.model.spi.IndexableTypeOrdering;

/**
 * @author Yoann Rodiere
 */
class IndexSchemaNestingContextImpl implements IndexSchemaNestingContext {

	private final IndexedEmbeddedFilter filter;
	private final String prefixFromFilter;
	private final String unconsumedPrefix;

	public IndexSchemaNestingContextImpl(IndexableTypeOrdering typeOrdering) {
		this( new IndexedEmbeddedFilter( typeOrdering ), "", "" );
	}

	private IndexSchemaNestingContextImpl(IndexedEmbeddedFilter filter, String prefixFromFilter,
			String unconsumedPrefix) {
		this.filter = filter;
		this.prefixFromFilter = prefixFromFilter;
		this.unconsumedPrefix = unconsumedPrefix;
	}

	@Override
	public String toString() {
		return new StringBuilder( getClass().getSimpleName() )
				.append( "[" )
				.append( "filter=" ).append( filter )
				.append( ",prefixFromFilter=" ).append( prefixFromFilter )
				.append( ",unconsumedPrefix=" ).append( unconsumedPrefix )
				.append( "]" )
				.toString();
	}

	@Override
	public <T> T nest(String relativeName, Function<String, T> nestedElementFactoryIfIncluded,
			Function<String, T> nestedElementFactoryIfExcluded) {
		String nameRelativeToFilter = prefixFromFilter + relativeName;
		String prefixedRelativeName = unconsumedPrefix + relativeName;
		if ( filter.isPathIncluded( nameRelativeToFilter ) ) {
			return nestedElementFactoryIfIncluded.apply( prefixedRelativeName );
		}
		else {
			return nestedElementFactoryIfExcluded.apply( prefixedRelativeName );
		}
	}

	@Override
	public <T> T nest(String relativeName,
			BiFunction<String, IndexSchemaNestingContext, T> nestedElementFactoryIfIncluded,
			BiFunction<String, IndexSchemaNestingContext, T> nestedElementFactoryIfExcluded) {
		String nameRelativeToFilter = prefixFromFilter + relativeName;
		String prefixedRelativeName = unconsumedPrefix + relativeName;
		if ( filter.isPathIncluded( nameRelativeToFilter ) ) {
			IndexSchemaNestingContextImpl nestedFilter =
					new IndexSchemaNestingContextImpl( filter, nameRelativeToFilter + ".", "" );
			return nestedElementFactoryIfIncluded.apply( prefixedRelativeName, nestedFilter );
		}
		else {
			return nestedElementFactoryIfExcluded.apply( prefixedRelativeName, IndexSchemaNestingContext.excludeAll() );
		}
	}

	public <N, T> Optional<T> addIndexedEmbeddedIfIncluded(
			String relativePrefix,
			Function<IndexedEmbeddedFilter, IndexedEmbeddedFilter> filterCompositionFunction,
			N indexModelNode, BiFunction<N, String, N> recursionFunction,
			BiFunction<N, IndexSchemaNestingContextImpl, T> finisher) {
		IndexedEmbeddedFilter composedFilter = filterCompositionFunction.apply( filter );
		if ( !composedFilter.isTerminal() ) {
			String prefixToParse = unconsumedPrefix + relativePrefix;
			N currentNode = indexModelNode;
			int afterPreviousDotIndex = 0;
			int nextDotIndex = prefixToParse.indexOf( '.', afterPreviousDotIndex );
			while ( nextDotIndex >= 0 ) {
				String objectName = prefixToParse.substring( afterPreviousDotIndex, nextDotIndex );
				currentNode = recursionFunction.apply( currentNode, objectName );
				afterPreviousDotIndex = nextDotIndex + 1;
				nextDotIndex = prefixToParse.indexOf( '.', afterPreviousDotIndex );
			}
			String unconsumedPrefix = prefixToParse.substring( afterPreviousDotIndex );

			IndexSchemaNestingContextImpl nestedContext =
					new IndexSchemaNestingContextImpl( composedFilter, relativePrefix, unconsumedPrefix );
			return Optional.of( finisher.apply( currentNode, nestedContext ) );
		}
		else {
			return Optional.empty();
		}
	}
}