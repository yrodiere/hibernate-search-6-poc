/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.dirtiness.building.impl;

import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoIndexModelBinder;
import org.hibernate.search.v6poc.entity.pojo.model.path.impl.PojoModelPath;

abstract class AbstractPojoImplicitReindexingResolverNodeBuilder {

	final PojoIndexModelBinder indexModelBinder;

	AbstractPojoImplicitReindexingResolverNodeBuilder(PojoIndexModelBinder indexModelBinder) {
		this.indexModelBinder = indexModelBinder;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + getModelPath() + "]";
	}

	abstract PojoModelPath getModelPath();

}
