/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.mapping.impl;

import java.util.Collection;

import org.hibernate.search.v6poc.util.SearchException;

/**
 * A container for a set of per-type workers,
 * caching the workers when they are created and reusing them when they are next requested.
 *
 * @param <W> The type of per-type workers, both for indexed type and for contained types.
 * @param <I> The type of per-type workers for indexed types.
 * @param <C> The type of per-type workers for contained types.
 */
public interface PojoTypeWorkerContainer<W, I extends W, C extends W> {

	/**
	 * @param entity An entity instance.
	 * @return The type worker for the given entity.
	 * @throws SearchException If the type of the given entity is not "workable"
	 * (neither indexed nor contained).
	 */
	W get(Object entity);

	/**
	 * @param entity An entity instance.
	 * @return The indexed type worker for the given entity.
	 * @throws SearchException If the type of the given entity is not indexed.
	 */
	I getIndexed(Object entity);

	/**
	 * @param entityType An entity type.
	 * @return The type worker for the given entity.
	 * @throws SearchException If the given type is not "workable"
	 * (neither indexed nor contained).
	 */
	W get(Class<?> entityType);

	/**
	 * @param entityType An entity type.
	 * @return The indexed type worker for the given entity type.
	 * @throws SearchException If the given type is not indexed.
	 */
	I getIndexed(Class<?> entityType);

	Collection<I> getAllIndexed();

	/**
	 * @return All the indexed type workers added to the cache so far.
	 */
	Collection<I> getCachedIndexed();

	/**
	 * @return All the contained type workers added to the cache so far.
	 */
	Collection<C> getCachedContained();

	/**
	 * Clear all worker caches.
	 */
	void clear();

}
