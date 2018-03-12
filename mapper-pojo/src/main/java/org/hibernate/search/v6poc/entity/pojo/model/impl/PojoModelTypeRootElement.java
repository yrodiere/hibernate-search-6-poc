/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.model.impl;

import org.hibernate.search.v6poc.entity.mapping.building.spi.TypeMetadataContributorProvider;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoIndexModelBinder;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoTypeNodeMetadataContributor;
import org.hibernate.search.v6poc.entity.pojo.model.PojoModelElementAccessor;
import org.hibernate.search.v6poc.entity.pojo.model.PojoModelType;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoGenericTypeModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoTypeModel;


/**
 * @author Yoann Rodiere
 */
public class PojoModelTypeRootElement<T> extends AbstractPojoModelSingleValuedElement<T> implements PojoModelType {

	public PojoModelTypeRootElement(PojoGenericTypeModel<T> typeModel,
			TypeMetadataContributorProvider<PojoTypeNodeMetadataContributor> modelContributorProvider,
			PojoIndexModelBinder binder) {
		super( typeModel, modelContributorProvider, binder );
	}

	@Override
	public String toString() {
		return getTypeModel().toString();
	}

	@Override
	<U> PojoModelElementAccessor<U> doCreateAccessor(PojoTypeModel<U> typeModel) {
		return new PojoModelRootElementAccessor<>( typeModel );
	}
}
