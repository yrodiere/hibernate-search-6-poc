/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.integrationtest.util.common.stub.backend;

import java.util.Locale;

import org.hibernate.search.v6poc.search.DocumentReference;

public final class StubBackendUtils {

	private StubBackendUtils() {
	}

	public static String normalizeIndexName(String indexName) {
		return indexName.toLowerCase( Locale.ROOT );
	}

	public static DocumentReference reference(String indexName, String id) {
		return rawReference( StubBackendUtils.normalizeIndexName( indexName ), id );
	}

	public static DocumentReference rawReference(String indexName, String id) {
		return new StubDocumentReference( indexName, id );
	}
}
