/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.util.impl.integrationtest.common.stub.backend.document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.hibernate.search.v6poc.util.impl.integrationtest.common.stub.StubTreeNode;

public class StubDocumentNode extends StubTreeNode<StubDocumentNode> {

	public static Builder document() {
		return new Builder( null, null );
	}

	public static Builder object(Builder parent, String relativeName) {
		return new Builder( parent, relativeName );
	}

	private StubDocumentNode(Builder builder) {
		super( builder );
	}

	public static class Builder extends StubTreeNode.Builder<StubDocumentNode> {

		private Builder(Builder parent, String relativeName) {
			super( parent, relativeName );
		}

		public Builder field(String relativeName, Object value) {
			attribute( relativeName, value );
			return this;
		}

		/*
		 * The signature is a bit weird, but that's on purpose:
		 * we want to avoid ambiguity on the call site when passing null
		 * to the other version of this method.
		 */
		public Builder field(String relativeName, Object value, Object ... values) {
			List<Object> list = new ArrayList<>();
			list.add( value );
			Collections.addAll( list, values );
			attribute( relativeName, list.toArray() );
			return this;
		}

		public Builder objectField(String relativeName, Consumer<Builder> contributor) {
			Builder childBuilder = StubDocumentNode.object( this, relativeName );
			contributor.accept( childBuilder );
			child( childBuilder );
			return this;
		}

		public Builder missingObjectField(String relativeName) {
			missingChild( relativeName );
			return this;
		}

		@Override
		public void child(StubTreeNode.Builder<StubDocumentNode> nodeBuilder) {
			super.child( nodeBuilder );
		}

		@Override
		public StubDocumentNode build() {
			return new StubDocumentNode( this );
		}
	}


}
