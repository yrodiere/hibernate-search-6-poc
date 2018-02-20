/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.cfg.impl;

import java.util.Objects;
import java.util.Optional;

import org.hibernate.search.v6poc.cfg.ConfigurationPropertySource;

public class PrefixedConfigurationPropertySource implements ConfigurationPropertySource {
	private final ConfigurationPropertySource propertiesToPrefix;
	private final String radix;
	private final int radixLength;

	public PrefixedConfigurationPropertySource(ConfigurationPropertySource toPrefix, String mask) {
		this.propertiesToPrefix = Objects.requireNonNull( toPrefix );
		this.radix = Objects.requireNonNull( mask ) + ".";
		this.radixLength = radix.length();
	}

	@Override
	public Optional<?> get(String key) {
		if ( key.startsWith( radix ) ) {
			return propertiesToPrefix.get( key.substring( radixLength ) );
		}
		else {
			return Optional.empty();
		}
	}

	@Override
	public ConfigurationPropertySource withPrefix(String prefix) {
		return new PrefixedConfigurationPropertySource( propertiesToPrefix, prefix + radix.substring( 0, radix.length() - 1 ) );
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder( getClass().getSimpleName() )
				.append( "[" )
				.append( "prefix=" ).append( radix )
				.append( ", propertiesToPrefix=" ).append( propertiesToPrefix )
				.append( "]" );
		return sb.toString();
	}
}
