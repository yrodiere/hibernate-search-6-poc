/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.elasticsearch.search.predicate.impl;

import org.hibernate.search.v6poc.search.SearchPredicate;

import com.google.gson.JsonObject;

class ElasticsearchSearchPredicate
		implements SearchPredicate, ElasticsearchSearchPredicateBuilder {

	private final JsonObject jsonObject;

	ElasticsearchSearchPredicate(JsonObject jsonObject) {
		this.jsonObject = jsonObject;
	}

	@Override
	public JsonObject build() {
		return jsonObject;
	}

}
