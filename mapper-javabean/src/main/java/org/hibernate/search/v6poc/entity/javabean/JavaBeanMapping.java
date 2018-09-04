/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.javabean;

import java.lang.invoke.MethodHandles;

import org.hibernate.search.v6poc.cfg.ConfigurationPropertySource;
import org.hibernate.search.v6poc.entity.pojo.mapping.PojoMapping;
import org.hibernate.search.v6poc.entity.pojo.mapping.PojoSearchManager;

public interface JavaBeanMapping extends PojoMapping {

	PojoSearchManager createSearchManager();

	JavaBeanSearchManagerBuilder createSearchManagerWithOptions();

	static JavaBeanMappingBuilder builder() {
		return builder( MethodHandles.publicLookup() );
	}

	static JavaBeanMappingBuilder builder(MethodHandles.Lookup lookup) {
		return builder( ConfigurationPropertySource.empty(), lookup );
	}

	static JavaBeanMappingBuilder builder(ConfigurationPropertySource propertySource, MethodHandles.Lookup lookup) {
		return new JavaBeanMappingBuilder( propertySource, lookup );
	}

}
