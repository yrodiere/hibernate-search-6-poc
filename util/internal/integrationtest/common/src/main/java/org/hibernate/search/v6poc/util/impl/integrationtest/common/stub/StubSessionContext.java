/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.util.impl.integrationtest.common.stub;

import org.hibernate.search.v6poc.engine.spi.SessionContext;

public class StubSessionContext implements SessionContext {

	private final String tenantIdentifier;

	public StubSessionContext() {
		this( null );
	}

	public StubSessionContext(String tenantIdentifier) {
		this.tenantIdentifier = tenantIdentifier;
	}

	@Override
	public String getTenantIdentifier() {
		return tenantIdentifier;
	}
}
