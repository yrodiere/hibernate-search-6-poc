/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.dirtiness.impl;

/**
 * An object responsible for resolving the set of entities that should be reindexed when a given entity changes.
 *
 * @param <T>
 */
public interface PojoImplicitReindexingResolver<T> {

	/**
	 * Assuming {@code dirty} is dirty, add all entities that should be reindexed to {@code collector}.
	 * @param collector A collector for dirty entities that should be reindexed.
	 * @param dirty A value that can be assumed dirty.
	 */
	void resolveEntitiesToReindex(EntityReindexingCollector collector, T dirty);

	static <T> PojoImplicitReindexingResolver<T> noOp() {
		return (ignored1, ignored2) -> { };
	}

}
