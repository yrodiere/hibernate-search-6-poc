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

import org.hibernate.search.v6poc.backend.document.model.dsl.Sortable;
import org.hibernate.search.v6poc.backend.document.model.dsl.Store;
import org.hibernate.search.v6poc.entity.pojo.extractor.ContainerValueExtractor;

/**
 * @author Yoann Rodiere
 */
@Documented
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Repeatable( Field.List.class )
public @interface Field {

	/**
	 * @return The name of the index field.
	 */
	String name() default "";

	/**
	 * @return A reference to the value bridge to use for this field.
	 * Cannot be used in the same {@code @Field} annotation as {@link #valueBridgeBuilder()}:
	 * either a bridge or a bridge builder can be provided, but never both.
	 */
	ValueBridgeBeanReference valueBridge() default @ValueBridgeBeanReference;

	/**
	 * @return A reference to the builder to use to build a value bridge for this field.
	 * Cannot be used in the same {@code @Field} annotation as {@link #valueBridge()}:
	 * either a bridge or a bridge builder can be provided, but never both.
	 */
	ValueBridgeBuilderBeanReference valueBridgeBuilder() default @ValueBridgeBuilderBeanReference;

	String analyzer() default "";

	String normalizer() default "";

	Store store() default Store.DEFAULT;

	Sortable sortable() default Sortable.DEFAULT;

	// TODO index, analyze, norms, termVector
	// TODO analyzer, normalizer
	// TODO indexNullAs? => Maybe we should rather use "missing" queries?

	/**
	 * @return An array of reference to container value extractor implementation classes,
	 * which will be applied to the source value before applying this bridge.
	 * By default, Hibernate Search will try to apply a set of extractors for common types
	 * ({@link java.lang.Iterable}, {@link java.util.Collection}, {@link java.util.Optional}, ...).
	 * To prevent Hibernate Search from applying any extractor, set this attribute to an empty array (<code>{}</code>).
	 */
	ContainerValueExtractorBeanReference[] extractors()
			default @ContainerValueExtractorBeanReference( type = DefaultExtractors.class );

	/**
	 * Class used as a marker for the default value of the {@link #extractors()} attribute.
	 */
	abstract class DefaultExtractors implements ContainerValueExtractor<Object, Object> {
		private DefaultExtractors() {
		}
	}

	@Documented
	@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD })
	@Retention(RetentionPolicy.RUNTIME)
	@interface List {
		Field[] value();
	}

}
