/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.document.spi;

import org.hibernate.search.v6poc.backend.document.DocumentElement;
import org.hibernate.search.v6poc.backend.document.IndexFieldAccessor;
import org.hibernate.search.v6poc.backend.document.converter.ToIndexFieldValueConverter;

class ConvertingIndexFieldAccessor<V, F> implements IndexFieldAccessor<V> {

	private final IndexFieldAccessor<F> delegate;
	private final ToIndexFieldValueConverter<? super V, ? extends F> converter;

	ConvertingIndexFieldAccessor(IndexFieldAccessor<F> delegate,
			ToIndexFieldValueConverter<? super V, ? extends F> converter) {
		this.delegate = delegate;
		this.converter = converter;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + delegate + "," + converter + "]";
	}

	@Override
	public void write(DocumentElement target, V value) {
		F converted = converter.convert( value );
		delegate.write( target, converted );
	}
}
