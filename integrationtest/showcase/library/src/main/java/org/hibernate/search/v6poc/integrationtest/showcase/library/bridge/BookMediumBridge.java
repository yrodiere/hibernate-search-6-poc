/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.integrationtest.showcase.library.bridge;

import org.hibernate.search.v6poc.entity.pojo.bridge.FunctionBridge;
import org.hibernate.search.v6poc.integrationtest.showcase.library.model.BookMedium;

public class BookMediumBridge implements FunctionBridge<BookMedium, String> {
	@Override
	public String toIndexedValue(BookMedium propertyValue) {
		return propertyValue == null ? null : propertyValue.name();
	}

	@Override
	public Object fromIndexedValue(String fieldValue) {
		return fieldValue == null ? null : BookMedium.valueOf( fieldValue );
	}
}
