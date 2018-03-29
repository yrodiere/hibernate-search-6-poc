/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.lucene.document.impl;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.lucene.document.Document;
import org.hibernate.search.v6poc.backend.lucene.document.model.impl.LuceneIndexSchemaObjectNode;
import org.hibernate.search.v6poc.logging.impl.Log;
import org.hibernate.search.v6poc.util.impl.common.LoggerFactory;

/**
 * @author Guillaume Smet
 */
public abstract class AbstractLuceneDocumentBuilder implements LuceneDocumentBuilder {

	private static final Log log = LoggerFactory.make( Log.class, MethodHandles.lookup() );

	protected final LuceneIndexSchemaObjectNode schemaNode;

	private List<LuceneFlattenedObjectDocumentBuilder> flattenedObjectDocumentBuilders;

	private List<LuceneNestedObjectDocumentBuilder> nestedObjectDocumentBuilders;

	protected AbstractLuceneDocumentBuilder(LuceneIndexSchemaObjectNode schemaNode) {
		this.schemaNode = schemaNode;
	}

	@Override
	public void addNestedObjectDocumentBuilder(LuceneIndexSchemaObjectNode expectedParentNode, LuceneNestedObjectDocumentBuilder nestedObjectDocumentBuilder) {
		checkTreeConsistency( expectedParentNode );

		if ( nestedObjectDocumentBuilders == null ) {
			nestedObjectDocumentBuilders = new ArrayList<>();
		}

		nestedObjectDocumentBuilders.add( nestedObjectDocumentBuilder );
	}

	@Override
	public void addFlattenedObjectDocumentBuilder(LuceneIndexSchemaObjectNode expectedParentNode, LuceneFlattenedObjectDocumentBuilder flattenedObjectDocumentBuilder) {
		checkTreeConsistency( expectedParentNode );

		if ( flattenedObjectDocumentBuilders == null ) {
			flattenedObjectDocumentBuilders = new ArrayList<>();
		}

		flattenedObjectDocumentBuilders.add( flattenedObjectDocumentBuilder );
	}

	protected void checkTreeConsistency( LuceneIndexSchemaObjectNode expectedParentNode ) {
		if ( !Objects.equals( expectedParentNode, schemaNode ) ) {
			throw log.invalidParentDocumentObjectState( expectedParentNode.getAbsolutePath(), schemaNode.getAbsolutePath() );
		}
	}

	void contribute(String rootIndexName, String tenantId, String rootId, Document currentDocument, List<Document> nestedDocuments) {
		if ( flattenedObjectDocumentBuilders != null ) {
			for ( LuceneFlattenedObjectDocumentBuilder flattenedObjectDocumentBuilder : flattenedObjectDocumentBuilders ) {
				flattenedObjectDocumentBuilder.contribute( rootIndexName, tenantId, rootId, currentDocument, nestedDocuments );
			}
		}

		if ( nestedObjectDocumentBuilders != null ) {
			for ( LuceneNestedObjectDocumentBuilder nestedObjectDocumentBuilder : nestedObjectDocumentBuilders ) {
				nestedObjectDocumentBuilder.contribute( rootIndexName, tenantId, rootId, currentDocument, nestedDocuments );
			}
		}
	}
}
