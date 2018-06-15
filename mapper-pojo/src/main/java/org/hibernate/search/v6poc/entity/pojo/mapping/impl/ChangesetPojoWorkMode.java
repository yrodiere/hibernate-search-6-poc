/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.mapping.impl;

import org.hibernate.search.v6poc.entity.pojo.mapping.spi.PojoSessionContext;

final class ChangesetPojoWorkMode implements PojoWorkMode<
		ChangesetPojoIndexedTypeWorker<?, ?, ?>,
		ChangesetPojoContainedTypeWorker<?>
		> {

	private static final ChangesetPojoWorkMode INSTANCE = new ChangesetPojoWorkMode();

	public static ChangesetPojoWorkMode get() {
		return INSTANCE;
	}

	private ChangesetPojoWorkMode() {
		// Private constructor, use get() instead
	}

	@Override
	public ChangesetPojoIndexedTypeWorker<?, ?, ?> createIndexedTypeWorker(PojoIndexedTypeManager<?, ?, ?> typeManager,
			PojoSessionContext sessionContext) {
		return typeManager.createWorker( sessionContext );

	}

	@Override
	public ChangesetPojoContainedTypeWorker<?> createContainedTypeWorker(PojoContainedTypeManager<?> typeManager,
			PojoSessionContext sessionContext) {
		return typeManager.createWorker( sessionContext );
	}

}
