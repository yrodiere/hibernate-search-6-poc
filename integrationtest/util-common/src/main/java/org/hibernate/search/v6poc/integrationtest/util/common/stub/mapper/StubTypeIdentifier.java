/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.integrationtest.util.common.stub.mapper;

import java.util.Objects;

import org.hibernate.search.v6poc.entity.model.spi.IndexedTypeIdentifier;

class StubTypeIdentifier implements IndexedTypeIdentifier {

	private final String typeIdentifier;

	StubTypeIdentifier(String typeIdentifier) {
		this.typeIdentifier = typeIdentifier;
	}

	public String asString() {
		return typeIdentifier;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}
		StubTypeIdentifier that = (StubTypeIdentifier) o;
		return Objects.equals( typeIdentifier, that.typeIdentifier );
	}

	@Override
	public int hashCode() {
		return Objects.hash( typeIdentifier );
	}
}
