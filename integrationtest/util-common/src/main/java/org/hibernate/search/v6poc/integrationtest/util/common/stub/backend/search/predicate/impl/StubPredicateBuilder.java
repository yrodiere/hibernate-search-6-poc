/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.integrationtest.util.common.stub.backend.search.predicate.impl;

import org.hibernate.search.v6poc.integrationtest.util.common.stub.backend.search.StubQueryElementCollector;
import org.hibernate.search.v6poc.search.predicate.spi.AllPredicateBuilder;
import org.hibernate.search.v6poc.search.predicate.spi.BooleanJunctionPredicateBuilder;
import org.hibernate.search.v6poc.search.predicate.spi.MatchPredicateBuilder;
import org.hibernate.search.v6poc.search.predicate.spi.NestedPredicateBuilder;
import org.hibernate.search.v6poc.search.predicate.spi.RangePredicateBuilder;

public class StubPredicateBuilder implements AllPredicateBuilder<StubQueryElementCollector>,
		BooleanJunctionPredicateBuilder<StubQueryElementCollector>,
		MatchPredicateBuilder<StubQueryElementCollector>,
		RangePredicateBuilder<StubQueryElementCollector>,
		NestedPredicateBuilder<StubQueryElementCollector> {

	@Override
	public StubQueryElementCollector getMustCollector() {
		return StubQueryElementCollector.get();
	}

	@Override
	public StubQueryElementCollector getMustNotCollector() {
		return StubQueryElementCollector.get();
	}

	@Override
	public StubQueryElementCollector getShouldCollector() {
		return StubQueryElementCollector.get();
	}

	@Override
	public StubQueryElementCollector getFilterCollector() {
		return StubQueryElementCollector.get();
	}

	@Override
	public void value(Object value) {
		// No-op
	}

	@Override
	public void lowerLimit(Object value) {
		// No-op
	}

	@Override
	public void excludeLowerLimit() {
		// No-op
	}

	@Override
	public void upperLimit(Object value) {
		// No-op
	}

	@Override
	public void excludeUpperLimit() {
		// No-op
	}

	@Override
	public void boost(float boost) {
		// No-op
	}

	@Override
	public StubQueryElementCollector getNestedCollector() {
		return StubQueryElementCollector.get();
	}

	@Override
	public void contribute(StubQueryElementCollector collector) {
		collector.simulateCollectCall();
	}
}
