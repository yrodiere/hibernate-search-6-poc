/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.mapping.impl;

import org.hibernate.search.v6poc.entity.pojo.mapping.spi.PojoSessionContext;

/**
 * @param <E> The contained entity type.
 */
class StreamPojoContainedTypeWorker<E> extends StreamPojoTypeWorker {

	private final PojoContainedTypeManager<E> typeManager;

	StreamPojoContainedTypeWorker(PojoContainedTypeManager<E> typeManager, PojoSessionContext sessionContext) {
		super( sessionContext );
		this.typeManager = typeManager;
	}

	// TODO add operations that may make sense for contained types in streamed mode. Maybe "update all containing entities"?

}
