/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.hibernate.search.v6poc.entity.pojo.extractor.ContainerValueExtractor;

@Documented
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface AssociationInverseSide {

	/**
	 * @return An array of reference to container value extractor implementation classes,
	 * allowing to precisely define which association the inverse side is being defined for.
	 * For instance, on a property of type {@code Map<EntityA, EntityB>},
	 * {@code @AssociationInverseSide(extractors = @ContainerValueExtractorBeanReference(type = MapKeyExtractor.class))}
	 * would define the inverse side of the association for the map keys (of type EntityA),
	 * while {@code @AssociationInverseSide(extractors = @ContainerValueExtractorBeanReference(type = MapValueExtractor.class))}
	 * would define the inverse side of the association for the map values (of type EntityB).
	 * By default, Hibernate Search will try to apply a set of extractors for common types
	 * ({@link Iterable}, {@link java.util.Collection}, {@link java.util.Optional}, ...).
	 * To prevent Hibernate Search from applying any extractor, set this attribute to an empty array (<code>{}</code>).
	 */
	ContainerValueExtractorBeanReference[] extractors()
			default @ContainerValueExtractorBeanReference( type = DefaultExtractors.class );

	/**
	 * @return The path to the targeted entity on the inverse side of the association.
	 */
	PropertyValue[] inversePath();

	/**
	 * Class used as a marker for the default value of the {@link #extractors()} attribute.
	 */
	abstract class DefaultExtractors implements ContainerValueExtractor<Object, Object> {
		private DefaultExtractors() {
		}
	}

}
