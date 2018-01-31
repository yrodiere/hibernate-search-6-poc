/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.elasticsearch.search.predicate.impl;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl.ElasticsearchFieldFormatter;
import org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl.ElasticsearchIndexModel;
import org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl.ElasticsearchIndexSchemaNode;
import org.hibernate.search.v6poc.backend.elasticsearch.logging.impl.Log;
import org.hibernate.search.v6poc.backend.elasticsearch.search.dsl.impl.ElasticsearchSearchPredicateCollector;
import org.hibernate.search.v6poc.search.predicate.spi.BooleanJunctionPredicateBuilder;
import org.hibernate.search.v6poc.search.predicate.spi.MatchPredicateBuilder;
import org.hibernate.search.v6poc.search.predicate.spi.NestedPredicateBuilder;
import org.hibernate.search.v6poc.search.predicate.spi.RangePredicateBuilder;
import org.hibernate.search.v6poc.search.predicate.spi.SearchPredicateFactory;
import org.hibernate.search.v6poc.util.SearchException;
import org.hibernate.search.v6poc.util.spi.LoggerFactory;

/**
 * @author Yoann Rodiere
 */
// TODO have one version of the clause factory per dialect, if necessary
public class SearchPredicateFactoryImpl implements SearchPredicateFactory<ElasticsearchSearchPredicateCollector> {

	private static final Log log = LoggerFactory.make( Log.class );

	private final Collection<ElasticsearchIndexModel> indexModels;

	public SearchPredicateFactoryImpl(Collection<ElasticsearchIndexModel> indexModels) {
		this.indexModels = indexModels;
	}

	@Override
	public BooleanJunctionPredicateBuilder<ElasticsearchSearchPredicateCollector> bool() {
		return new BooleanJunctionPredicateBuilderImpl();
	}

	@Override
	public MatchPredicateBuilder<ElasticsearchSearchPredicateCollector> match(String absoluteFieldPath) {
		return new MatchPredicateBuilderImpl( absoluteFieldPath, getFormatter( "match", absoluteFieldPath ) );
	}

	@Override
	public RangePredicateBuilder<ElasticsearchSearchPredicateCollector> range(String absoluteFieldPath) {
		return new RangePredicateBuilderImpl( absoluteFieldPath, getFormatter( "range", absoluteFieldPath ) );
	}

	@Override
	public NestedPredicateBuilder<ElasticsearchSearchPredicateCollector> nested(String absoluteFieldPath) {
		checkNestedField( "nested", absoluteFieldPath );
		return new NestedPredicateBuilderImpl( absoluteFieldPath );
	}

	private ElasticsearchFieldFormatter getFormatter(String predicateName, String absoluteFieldPath) {
		ElasticsearchIndexModel indexModelForSelectedFormatter = null;
		ElasticsearchFieldFormatter selectedFormatter = null;
		for ( ElasticsearchIndexModel indexModel : indexModels ) {
			ElasticsearchIndexSchemaNode schemaNode = indexModel.getSchemaNode( absoluteFieldPath );
			if ( schemaNode != null ) {
				ElasticsearchFieldFormatter formatter;
				try {
					formatter = schemaNode.getFormatter();
				}
				catch (SearchException e) {
					throw log.cannotUsePredicateOnField( indexModel.getIndexName(), predicateName,
							absoluteFieldPath, e.getMessage(), e );
				}
				if ( selectedFormatter == null ) {
					selectedFormatter = formatter;
					indexModelForSelectedFormatter = indexModel;
				}
				else if ( !selectedFormatter.equals( formatter ) ) {
					throw log.conflictingFieldFormattersForSearch(
							absoluteFieldPath,
							selectedFormatter, indexModelForSelectedFormatter.getIndexName(),
							formatter, indexModel.getIndexName()
							);
				}
			}
		}
		if ( selectedFormatter == null ) {
			throw log.unknownFieldForSearch( getIndexNames(), predicateName, absoluteFieldPath );
		}
		return selectedFormatter;
	}

	private void checkNestedField(String predicateName, String absoluteFieldPath) {
		boolean found = false;

		for ( ElasticsearchIndexModel indexModel : indexModels ) {
			ElasticsearchIndexSchemaNode schemaNode = indexModel.getSchemaNode( absoluteFieldPath );
			if ( schemaNode != null ) {
				found = true;
				try {
					schemaNode.checkSuitableForNestedQuery();
				}
				catch (SearchException e) {
					throw log.cannotUsePredicateOnField( indexModel.getIndexName(), predicateName,
							absoluteFieldPath, e.getMessage(), e );
				}
			}
		}
		if ( !found ) {
			throw log.unknownFieldForSearch( getIndexNames(), predicateName, absoluteFieldPath );
		}
	}

	private List<String> getIndexNames() {
		return indexModels.stream().map( ElasticsearchIndexModel::getIndexName ).collect( Collectors.toList() );
	}

}
