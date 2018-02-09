/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.integrationtest.util.common.stub.backend;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.hibernate.search.v6poc.integrationtest.util.common.stub.backend.document.model.StubIndexSchemaNode;
import org.hibernate.search.v6poc.integrationtest.util.common.stub.backend.index.StubIndexWork;
import org.hibernate.search.v6poc.integrationtest.util.common.stub.backend.search.StubSearchWork;
import org.hibernate.search.v6poc.search.SearchResult;
import org.hibernate.search.v6poc.search.query.spi.HitAggregator;

public abstract class StubBackendBehavior {

	private static final StubBackendBehavior DEFAULT = new StubBackendBehavior() {
		@Override
		public void pushSchema(String indexName, StubIndexSchemaNode rootSchemaNode) {
			throw new IllegalStateException( "The stub backend behavior was not set when a schema was pushed for index '"
					+ indexName + "': " + rootSchemaNode );
		}

		@Override
		public void prepareWorks(String indexName, List<StubIndexWork> works) {
			throw new IllegalStateException( "The stub backend behavior was not set when works were prepared for index '"
					+ indexName + "': " + works );
		}

		@Override
		public CompletableFuture<?> executeWorks(String indexName, List<StubIndexWork> works) {
			throw new IllegalStateException( "The stub backend behavior was not set when works were executed for index '"
					+ indexName + "': " + works );
		}

		@Override
		public <T> SearchResult<T> executeSearchWork(List<String> indexNames, StubSearchWork work,
				HitAggregator<?, List<T>> hitAggregator) {
			throw new IllegalStateException( "The stub backend behavior was not set when a search work was executed for indexes "
					+ indexNames + "': " + work );
		}
	};

	private static Map<String, StubBackendBehavior> BEHAVIORS = new HashMap<>();

	public static void set(String backendName, StubBackendBehavior behavior) {
		BEHAVIORS.put( backendName, behavior );
	}

	public static void unset(String backendName, StubBackendBehavior behavior) {
		BEHAVIORS.remove( backendName, behavior );
	}

	public static void clear() {
		BEHAVIORS.clear();
	}

	public static StubBackendBehavior get(String backendName) {
		return BEHAVIORS.getOrDefault( backendName, DEFAULT );
	}

	protected StubBackendBehavior() {
	}

	public abstract void pushSchema(String indexName, StubIndexSchemaNode rootSchemaNode);

	public abstract void prepareWorks(String indexName, List<StubIndexWork> works);

	public abstract CompletableFuture<?> executeWorks(String indexName, List<StubIndexWork> works);

	public abstract <T> SearchResult<T> executeSearchWork(List<String> indexNames, StubSearchWork work,
			HitAggregator<?, List<T>> hitAggregator);
}
