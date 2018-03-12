/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.model;

import java.util.List;
import java.util.stream.Stream;

import org.hibernate.search.v6poc.entity.pojo.extractor.ContainerValueExtractor;

/**
 * @author Yoann Rodiere
 */
public interface PojoModelMultiValuedElement {

	PojoModelMultiValuedElementAccessor<PojoElement> createAccessor();

	<T> PojoModelMultiValuedElementAccessor<T> createAccessor(Class<T> type);

	PojoModelMultiValuedElement extract(Class<? extends ContainerValueExtractor> extractorClass);

	PojoModelMultiValuedElement extract(List<? extends Class<? extends ContainerValueExtractor>> extractorClasses);

	boolean isAssignableTo(Class<?> clazz);

	PojoModelMultiValuedProperty property(String relativeName);

	Stream<? extends PojoModelMultiValuedProperty> properties();

}
