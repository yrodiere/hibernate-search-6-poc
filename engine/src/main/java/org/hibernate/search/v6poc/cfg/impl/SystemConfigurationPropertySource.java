/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.cfg.impl;

import java.util.Optional;

import org.hibernate.search.v6poc.cfg.ConfigurationPropertySource;

public class SystemConfigurationPropertySource implements ConfigurationPropertySource {

	private static final SystemConfigurationPropertySource INSTANCE = new SystemConfigurationPropertySource();

	public static ConfigurationPropertySource get() {
		return INSTANCE;
	}

	private SystemConfigurationPropertySource() {
	}

	@Override
	public Optional<?> get(String key) {
		String value = System.getProperty( key );
		return Optional.ofNullable( value );
	}
}
