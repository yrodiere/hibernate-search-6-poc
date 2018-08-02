/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.lucene.types.converter.impl;

import java.time.LocalDate;

import org.hibernate.search.v6poc.backend.document.spi.UserIndexFieldConverter;

public final class LocalDateFieldConverter extends AbstractFieldConverter<LocalDate, Long> {

	public LocalDateFieldConverter(UserIndexFieldConverter<LocalDate> userConverter) {
		super( userConverter );
	}

	@Override
	public Long convertFromDsl(Object value) {
		LocalDate rawValue = userConverter.convertFromDsl( value );
		if ( value == null ) {
			return null;
		}
		return rawValue.toEpochDay();
	}
}
