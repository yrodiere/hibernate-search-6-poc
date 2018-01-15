/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.model.spi;

import java.lang.annotation.Annotation;
import java.util.stream.Stream;

import org.hibernate.search.v6poc.entity.pojo.model.unwrap.spi.Unwrapper;
import org.hibernate.search.v6poc.entity.pojo.model.unwrap.spi.UnwrapperFactory;

/**
 * @author Yoann Rodiere
 */
public interface PojoModelMultiValueElement {

	// FIXME what if I want a PojoModelElementAccessor<List<MyType>>?
	<T> PojoModelMultiValueElementAccessor<T> createAccessor(Class<T> type);

	PojoModelElementAccessor<?> createAccessor();

	PojoModelMultiValueElement unwrapArrayElements();

	PojoModelMultiValueElement unwrapIterableElements();

	PojoModelMultiValueElement unwrapMapKeys();

	PojoModelMultiValueElement unwrapMapValues();

	PojoModelMultiValueElement unwrap(UnwrapperFactory unwrapperFactory);

	boolean isAssignableTo(Class<?> clazz);

	<M extends Annotation> Stream<M> markers(Class<M> markerType);

	PojoModelMultiValueElement property(String relativeName);

	Stream<PojoModelMultiValueElement> properties();

}
