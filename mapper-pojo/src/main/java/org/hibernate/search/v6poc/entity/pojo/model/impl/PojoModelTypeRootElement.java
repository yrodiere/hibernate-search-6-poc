/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.model.impl;

import org.hibernate.search.v6poc.entity.mapping.building.spi.TypeMetadataContributorProvider;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoTypeMetadataContributor;
import org.hibernate.search.v6poc.entity.pojo.model.PojoModelElementAccessor;
import org.hibernate.search.v6poc.entity.pojo.model.PojoModelType;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoTypeModel;


/**
 * @author Yoann Rodiere
 */
public class PojoModelTypeRootElement extends AbstractPojoModelElement implements PojoModelType {

	private final PojoTypeModel<?> typeModel;

	public PojoModelTypeRootElement(PojoTypeModel<?> typeModel,
			TypeMetadataContributorProvider<PojoTypeMetadataContributor> modelContributorProvider) {
		super( modelContributorProvider );
		this.typeModel = typeModel;
	}

	@Override
	public String toString() {
		return typeModel.toString();
	}

	@Override
	public PojoModelElementAccessor<?> createAccessor() {
		return new PojoModelRootElementAccessor<>();
	}

	@Override
	PojoTypeModel<?> getTypeModel() {
		return typeModel;
	}
}
