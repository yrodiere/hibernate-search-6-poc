/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.model.impl;

import java.util.stream.Stream;

import org.hibernate.search.v6poc.entity.mapping.building.spi.TypeMetadataContributorProvider;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoIndexModelBinder;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoTypeNodeMetadataContributor;
import org.hibernate.search.v6poc.entity.pojo.model.PojoModelElementAccessor;
import org.hibernate.search.v6poc.entity.pojo.model.PojoModelProperty;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoPropertyModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoTypeModel;


/**
 * @author Yoann Rodiere
 */
public class PojoModelPropertyRootElement<T> extends AbstractPojoModelSingleValuedElement<T> implements PojoModelProperty {

	private final PojoPropertyModel<?> propertyModel;

	public PojoModelPropertyRootElement(PojoPropertyModel<T> propertyModel,
			TypeMetadataContributorProvider<PojoTypeNodeMetadataContributor> modelContributorProvider,
			PojoIndexModelBinder binder) {
		super( propertyModel.getTypeModel(), modelContributorProvider, binder );
		this.propertyModel = propertyModel;
	}

	@Override
	public String toString() {
		return propertyModel.toString();
	}

	@Override
	public <M> Stream<M> markers(Class<M> markerType) {
		return Stream.empty();
	}

	@Override
	public String getName() {
		return propertyModel.getName();
	}

	@Override
	<U> PojoModelElementAccessor<U> doCreateAccessor(PojoTypeModel<U> typeModel) {
		return new PojoModelRootElementAccessor<>( typeModel );
	}
}
