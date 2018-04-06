/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.model;

import java.util.stream.Stream;

/**
 * @author Yoann Rodiere
 */
public interface PojoModelElement {

	// FIXME what if I want a PojoModelElementAccessor<List<MyType>>?
	<T> PojoModelElementAccessor<T> createAccessor(Class<T> type);

	PojoModelElementAccessor<?> createAccessor();

	boolean isAssignableTo(Class<?> clazz);

	PojoModelProperty property(String relativeName);

	Stream<? extends PojoModelProperty> properties();

}
