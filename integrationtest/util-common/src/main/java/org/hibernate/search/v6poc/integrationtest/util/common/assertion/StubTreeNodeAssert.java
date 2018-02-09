/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.integrationtest.util.common.assertion;

import java.util.Map;

import org.hibernate.search.v6poc.integrationtest.util.common.stub.StubTreeNode;
import org.hibernate.search.v6poc.integrationtest.util.common.stub.StubTreeNodeCompare;
import org.hibernate.search.v6poc.integrationtest.util.common.stub.StubTreeNodeMismatch;

import junit.framework.AssertionFailedError;

public class StubTreeNodeAssert<T extends StubTreeNode<T>> {

	public static <T extends StubTreeNode<T>> StubTreeNodeAssert<T> assertThat(T node) {
		return new StubTreeNodeAssert<>( node );
	}

	private final T actual;

	private String messageBase = "StubTreeNode did not match: ";

	private StubTreeNodeAssert(T actual) {
		this.actual = actual;
	}

	public StubTreeNodeAssert<T> as(String messageBase) {
		this.messageBase = messageBase;
		return this;
	}

	public StubTreeNodeAssert<T> matches(T expected) {
		Map<String, StubTreeNodeMismatch> mismatchesByPath = StubTreeNodeCompare.compare( expected, actual );
		if ( !mismatchesByPath.isEmpty() ) {
			StringBuilder builder = new StringBuilder( messageBase );
			StubTreeNodeCompare.appendTo( builder, mismatchesByPath, "\n\t", "\t" );
			throw new AssertionFailedError( builder.toString() );
		}
		return this;
	}
}
