/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.mapping.building.impl;

import org.hibernate.search.v6poc.backend.document.IndexFieldAccessor;
import org.hibernate.search.v6poc.entity.pojo.bridge.ValueBridge;

public final class BoundValueBridge<V> {
	private final ValueBridge<? super V, ?> bridge;
	private final IndexFieldAccessor<V> indexFieldAccessor;

	BoundValueBridge(ValueBridge<? super V, ?> bridge, IndexFieldAccessor<V> indexFieldAccessor) {
		this.bridge = bridge;
		this.indexFieldAccessor = indexFieldAccessor;
	}

	public ValueBridge<? super V, ?> getBridge() {
		return bridge;
	}

	public IndexFieldAccessor<V> getIndexFieldAccessor() {
		return indexFieldAccessor;
	}
}
