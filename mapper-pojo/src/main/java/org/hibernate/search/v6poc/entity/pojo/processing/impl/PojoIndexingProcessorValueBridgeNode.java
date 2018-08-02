/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.processing.impl;

import org.hibernate.search.v6poc.backend.document.DocumentElement;
import org.hibernate.search.v6poc.backend.document.IndexFieldAccessor;
import org.hibernate.search.v6poc.entity.pojo.bridge.ValueBridge;
import org.hibernate.search.v6poc.util.impl.common.ToStringTreeBuilder;

/**
 * A node inside a {@link PojoIndexingProcessor} responsible for applying a {@link ValueBridge} to a value.
 *
 * @param <V> The processed type
 */
public class PojoIndexingProcessorValueBridgeNode<V> extends PojoIndexingProcessor<V> {

	private final ValueBridge<? super V, ?> bridge;
	private final IndexFieldAccessor<V> indexFieldAccessor;

	public PojoIndexingProcessorValueBridgeNode(ValueBridge<? super V, ?> bridge,
			IndexFieldAccessor<V> indexFieldAccessor) {
		this.bridge = bridge;
		this.indexFieldAccessor = indexFieldAccessor;
	}

	@Override
	public void close() {
		bridge.close();
	}

	@Override
	public void appendTo(ToStringTreeBuilder builder) {
		builder.attribute( "class", getClass().getSimpleName() );
		builder.attribute( "bridge", bridge );
		builder.attribute( "indexFieldAccessor", indexFieldAccessor );
	}

	@Override
	public void process(DocumentElement target, V source) {
		indexFieldAccessor.write( target, source );
	}

}
