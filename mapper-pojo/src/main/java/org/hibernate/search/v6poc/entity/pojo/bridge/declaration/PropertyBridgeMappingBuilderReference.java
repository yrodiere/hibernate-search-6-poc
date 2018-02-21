/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.bridge.declaration;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.hibernate.search.v6poc.entity.pojo.bridge.mapping.AnnotationBridgeBuilder;
import org.hibernate.search.v6poc.entity.pojo.bridge.PropertyBridge;

/**
 * @author Yoann Rodiere
 */
@Documented
@Target({}) // Only used as a component in other annotations
@Retention(RetentionPolicy.RUNTIME)
public @interface PropertyBridgeMappingBuilderReference {

	String name() default "";

	Class<? extends AnnotationBridgeBuilder<? extends PropertyBridge,?>> type() default UndefinedImplementationType.class;

	/**
	 * Class used as a marker for the default value of the {@link #type()} attribute.
	 */
	abstract class UndefinedImplementationType implements AnnotationBridgeBuilder<PropertyBridge, Annotation> {
		private UndefinedImplementationType() {
		}
	}
}

