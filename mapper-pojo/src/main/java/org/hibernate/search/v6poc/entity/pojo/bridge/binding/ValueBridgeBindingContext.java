/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.bridge.binding;

import org.hibernate.search.v6poc.backend.document.converter.FromIndexFieldValueConverter;
import org.hibernate.search.v6poc.backend.document.model.dsl.IndexSchemaFieldContext;
import org.hibernate.search.v6poc.entity.pojo.bridge.ValueBridge;
import org.hibernate.search.v6poc.entity.pojo.model.PojoModelValue;

/**
 * The context provided to the {@link ValueBridge#bind(ValueBridgeBindingContext)} method.
 */
public interface ValueBridgeBindingContext<F> {

	/**
	 * @return An entry point allowing to inspect the type of values that will be passed to this bridge.
	 */
	PojoModelValue getBridgedElement();

	/**
	 * @return An entry point allowing to declare expectations regarding the index schema.
	 */
	IndexSchemaFieldContext getIndexSchemaFieldContext();

	/**
	 * Register a converter for use when projecting on the index field bound to the value bridge.
	 * <p>
	 * By default, projecting on a bridged field will throw an exception; setting a converter though this method
	 * will enable projecting on the bridged field.
	 * <p>
	 * The converter will transform the indexed field value back to the value initially extracted from the POJO,
	 * or to any implementation-defined value.
	 * For instance, a {@code ValueBridge} indexing JPA entities by putting their identifier in a field
	 * might not be able to resolve the identifier back to an entity, so it could just return the identifier as-is.
	 *
	 * @param fromIndexFieldValueConverter The converter to use when projecting
	 * on the index field bound to the value bridge.
	 * @param <V> The type of values returned by the converter.
	 */
	<V> void setFromIndexFieldValueConverter(FromIndexFieldValueConverter<F, V> fromIndexFieldValueConverter);

}
