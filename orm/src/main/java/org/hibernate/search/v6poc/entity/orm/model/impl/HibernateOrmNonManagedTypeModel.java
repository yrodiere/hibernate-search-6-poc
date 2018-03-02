/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.orm.model.impl;

import java.util.List;

import org.hibernate.EntityMode;
import org.hibernate.annotations.common.reflection.XProperty;
import org.hibernate.search.v6poc.entity.pojo.model.spi.GenericContextAwarePojoGenericTypeModel.RawTypeDeclaringContext;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoPropertyModel;

class HibernateOrmNonManagedTypeModel<T> extends AbstractHibernateOrmTypeModel<T> {

	HibernateOrmNonManagedTypeModel(HibernateOrmIntrospector introspector, Class<T> clazz,
			RawTypeDeclaringContext<T> rawTypeDeclaringContext) {
		super( introspector, clazz, rawTypeDeclaringContext );
	}

	@Override
	PojoPropertyModel<?> createPropertyModel(String propertyName, List<XProperty> declaredXProperties) {
		return introspector.createFallbackPropertyModel(
				this,
				null,
				EntityMode.POJO,
				propertyName,
				declaredXProperties
		);
	}
}
