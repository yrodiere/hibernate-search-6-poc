/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.mapping.impl;

import org.hibernate.search.v6poc.entity.pojo.mapping.spi.PojoSessionContext;

final class StreamPojoWorkMode implements PojoWorkMode<
		StreamPojoIndexedTypeWorker<?, ?, ?>,
		StreamPojoContainedTypeWorker<?>
		> {

	private static final StreamPojoWorkMode INSTANCE = new StreamPojoWorkMode();

	public static StreamPojoWorkMode get() {
		return INSTANCE;
	}

	private StreamPojoWorkMode() {
		// Private constructor, use get() instead
	}

	@Override
	public StreamPojoIndexedTypeWorker<?, ?, ?> createIndexedTypeWorker(PojoIndexedTypeManager<?, ?, ?> typeManager,
			PojoSessionContext sessionContext) {
		return typeManager.createStreamWorker( sessionContext );

	}

	@Override
	public StreamPojoContainedTypeWorker<?> createContainedTypeWorker(PojoContainedTypeManager<?> typeManager,
			PojoSessionContext sessionContext) {
		return typeManager.createStreamWorker( sessionContext );
	}

}
