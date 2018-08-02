/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.bridge.builtin.impl;

import org.hibernate.search.v6poc.backend.document.model.dsl.IndexSchemaFieldContext;
import org.hibernate.search.v6poc.backend.document.model.dsl.IndexSchemaFieldTypedContext;
import org.hibernate.search.v6poc.entity.pojo.bridge.ValueBridge;
import org.hibernate.search.v6poc.entity.pojo.bridge.binding.ValueBridgeBindingContext;
import org.hibernate.search.v6poc.util.impl.common.Contracts;

/**
 * A pass-through value bridge, i.e. a bridge that passes the input value as-is to the underlying backend.
 * <p>
 * This bridge will not work for any type: only types supported by the backend
 * though {@link IndexSchemaFieldContext#as(Class)} will work.
 *
 * @param <F> The type of input values, as well as the type of the index field.
 */
public final class PassThroughValueBridge<F> implements ValueBridge<F, F> {

	private final Class<F> fieldType;

	public PassThroughValueBridge(Class<F> fieldType) {
		Contracts.assertNotNull( fieldType, "fieldType" );
		this.fieldType = fieldType;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + fieldType.getName() + "]";
	}

	@Override
	public IndexSchemaFieldTypedContext<F> bind(ValueBridgeBindingContext context) {
		return context.getIndexSchemaFieldContext().as( fieldType );
	}

	@Override
	public F cast(Object value) {
		return fieldType.cast( value );
	}

	@Override
	public F toIndexedValue(F value) {
		return value;
	}

	@Override
	public Object fromIndexedValue(F indexedValue) {
		return indexedValue;
	}

	@Override
	public boolean isCompatibleWith(ValueBridge<?, ?> other) {
		if ( !getClass().equals( other.getClass() ) ) {
			return false;
		}
		PassThroughValueBridge<?> castedOther = (PassThroughValueBridge<?>) other;
		return fieldType.equals( castedOther.fieldType );
	}
}