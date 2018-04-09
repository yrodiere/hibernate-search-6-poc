/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.hibernate.search.v6poc.entity.pojo.extractor.ContainerValueExtractor;

/**
 * Given an association on a entity type A to entity type B, defines the inverse side of an association,
 * i.e. the path from B to A.
 * <p>
 * This annotation is generally not needed, as inverse sides of associations should generally be inferred by the mapper.
 * For example, Hibernate ORM defines inverse sides using {@code @OneToMany#mappedBy}, {@code @OneToOne#mappedBy}, etc.,
 * and the Hibernate ORM mapper will register these inverse sides automatically.
 * <p>
 * The annotation must be applied to entity types only; adding this annotation to a property of a non-entity (embedded)
 * type will result in undefined behavior.
 * Use {@link #embeddedPath()} to define the inverse side of embedded associations.
 */
@Documented
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Repeatable( AssociationInverseSide.List.class )
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
	 * @return The path from the annotated value to the targeted entity on the original side of the association.
	 * Useful when the annotated value is a embedded, non-entity type that embeds an association.
	 */
	PropertyValue[] embeddedPath() default {};

	/**
	 * @return The path to the targeted entity on the inverse side of the association.
	 */
	PropertyValue[] inverseSidePath();

	/**
	 * Class used as a marker for the default value of the {@link #extractors()} attribute.
	 */
	abstract class DefaultExtractors implements ContainerValueExtractor<Object, Object> {
		private DefaultExtractors() {
		}
	}

	@Documented
	@Target({ ElementType.METHOD, ElementType.FIELD })
	@Retention(RetentionPolicy.RUNTIME)
	@interface List {
		AssociationInverseSide[] value();
	}

}
