/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.model.spi;

import java.lang.annotation.Annotation;
import java.util.stream.Stream;

/**
 * @author Yoann Rodiere
 */
public interface PojoModelElement {

	// FIXME what if I want a PojoModelElementAccessor<List<MyType>>?
	<T> PojoModelElementAccessor<T> createAccessor(Class<T> type);

	<T> PojoModelMultiValueElementAccessor<T> unwrapArrayElements(Class<T> elementType);

	<T> PojoModelMultiValueElementAccessor<T> unwrapIterableElements(Class<T> elementType);

	<T> PojoModelMultiValueElementAccessor<T> unwrapMapKeys(Class<T> elementType);

	<T> PojoModelMultiValueElementAccessor<T> unwrapMapValues(Class<T> elementType);

	<T> PojoModelElementAccessor<T> createAccessor(Class<T> elementType, Supplier<Unwrapper<T>>);

	PojoModelElementAccessor<?> createAccessor();

	boolean isAssignableTo(Class<?> clazz);

	<M extends Annotation> Stream<M> markers(Class<M> markerType);

	PojoModelElement property(String relativeName);

	Stream<PojoModelElement> properties();

}
