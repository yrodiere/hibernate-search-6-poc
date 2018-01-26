/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.model.spi;

import java.lang.annotation.Annotation;
import java.util.stream.Stream;

public interface PropertyModel<T> {

	String getName();

	Class<T> getJavaType();

	<A extends Annotation> Stream<A> getAnnotationsByType(Class<A> annotationType);

	Stream<? extends Annotation> getAnnotationsByMetaAnnotationType(Class<? extends Annotation> metaAnnotationType);

	TypeModel<T> getTypeModel();

	PropertyHandle getHandle();

}
