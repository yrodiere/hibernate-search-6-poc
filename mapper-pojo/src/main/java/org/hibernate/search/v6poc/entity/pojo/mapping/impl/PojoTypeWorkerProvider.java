/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.mapping.impl;

import org.hibernate.search.v6poc.entity.pojo.mapping.spi.PojoSessionContext;

/**
 * @param <M> The type of POJO type managers used to create the workers.
 * @param <W> The type of created type workers.
 */
public interface PojoTypeWorkerProvider<M, W> {

	W create(M typeManager, PojoSessionContext sessionContext);

}
