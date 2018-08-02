/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.bridge.binding.impl;

import org.hibernate.search.v6poc.backend.document.converter.FromIndexFieldValueConverter;
import org.hibernate.search.v6poc.backend.document.model.dsl.IndexSchemaFieldContext;
import org.hibernate.search.v6poc.entity.pojo.bridge.binding.ValueBridgeBindingContext;
import org.hibernate.search.v6poc.entity.pojo.model.PojoModelValue;

public class ValueBridgeBindingContextImpl<F> implements ValueBridgeBindingContext<F> {
	private final PojoModelValue bridgedElement;
	private final IndexSchemaFieldContext indexSchemaFieldContext;
	private FromIndexFieldValueConverter<F, ?> fromIndexFieldValueConverter;

	public ValueBridgeBindingContextImpl(PojoModelValue bridgedElement,
			IndexSchemaFieldContext indexSchemaFieldContext) {
		this.bridgedElement = bridgedElement;
		this.indexSchemaFieldContext = indexSchemaFieldContext;
	}

	@Override
	public PojoModelValue getBridgedElement() {
		return bridgedElement;
	}

	@Override
	public IndexSchemaFieldContext getIndexSchemaFieldContext() {
		return indexSchemaFieldContext;
	}

	@Override
	public <V> void setFromIndexFieldValueConverter(FromIndexFieldValueConverter<F, V> fromIndexFieldValueConverter) {
		this.fromIndexFieldValueConverter = fromIndexFieldValueConverter;
	}

	public FromIndexFieldValueConverter<F, ?> getFromIndexFieldValueConverter() {
		return fromIndexFieldValueConverter;
	}
}
