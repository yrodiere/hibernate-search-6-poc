/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.mapping.impl;

interface PojoTypeWorker {

	void add(Object entity);

	void add(Object id, Object entity);

	void update(Object entity);

	void update(Object id, Object entity);

	void delete(Object entity);

	void delete(Object id, Object entity);

}
