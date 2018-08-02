/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.elasticsearch.types.codec.impl;

import com.google.gson.JsonElement;

/**
 * Defines how a given value will be encoded as JSON and decoded from JSON.
 * <p>
 * Encodes values received from an {@link org.hibernate.search.v6poc.backend.document.IndexFieldAccessor} when indexing,
 * and returns decoded values to the {@link org.hibernate.search.v6poc.backend.elasticsearch.types.converter.impl.ElasticsearchFieldConverter}
 * when projecting in a search query.
 */
public interface ElasticsearchFieldCodec<F> {

	JsonElement encode(F value);

	F decode(JsonElement element);

	default boolean supportsSortingByDistance() {
		return false;
	}

	/**
	 * @param obj An object to compare
	 * @return {@code true} if {@code obj} is a codec whose {@link #encode(Object)} method is
	 * guaranteed to always return the exact same output value as this codec for any input value,
	 * {@code false} otherwise.
	 */
	@Override
	boolean equals(Object obj);

	@Override
	int hashCode();

}
