/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.model.spi;

/**
 * An indexable pojo type model, i.e. a type that is fully defined by its {@link Class}.
 * <p>
 * This excludes in particular parameterized types such as {@code ArrayList<Integer>},
 * because we cannot tell the difference between instances of such types and instances of the same type
 * with different parameters, such as {@code ArrayList<String>}.
 * Thus the mapper would be unable to find which mapping to use when indexing such an instance,
 * and it would be impossible to target the index from the {@link Class} only.
 *
 * @param <T> The pojo type
 */
public interface PojoIndexableTypeModel<T> extends PojoTypeModel<T> {

	/**
	 * @return The exact Java {@link Class} for this type.
	 */
	@Override
	Class<T> getJavaClass();

}
