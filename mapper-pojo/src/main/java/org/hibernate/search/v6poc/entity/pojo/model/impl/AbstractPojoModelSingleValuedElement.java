/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.model.impl;

import java.util.Optional;

import org.hibernate.search.v6poc.entity.mapping.building.spi.TypeMetadataContributorProvider;
import org.hibernate.search.v6poc.entity.pojo.extractor.impl.BoundContainerValueExtractor;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoIndexModelBinder;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoTypeNodeMetadataContributor;
import org.hibernate.search.v6poc.entity.pojo.model.PojoModelElement;
import org.hibernate.search.v6poc.entity.pojo.model.PojoModelElementAccessor;
import org.hibernate.search.v6poc.entity.pojo.model.PojoModelMultiValuedElement;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoGenericTypeModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoPropertyModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoTypeModel;
import org.hibernate.search.v6poc.util.SearchException;


/**
 * @author Yoann Rodiere
 */
abstract class AbstractPojoModelSingleValuedElement<T>
		extends AbstractPojoModelElement<T, PojoModelPropertyNestedSingleValuedElement<?>>
		implements PojoModelElement {

	AbstractPojoModelSingleValuedElement(PojoGenericTypeModel<T> typeModel,
			TypeMetadataContributorProvider<PojoTypeNodeMetadataContributor> modelContributorProvider,
			PojoIndexModelBinder binder) {
		super( typeModel, modelContributorProvider, binder );
	}

	@Override
	public PojoModelElementAccessor<T> createAccessor() {
		return doCreateAccessor( getTypeModel() );
	}

	@Override
	public <U> PojoModelElementAccessor<U> createAccessor(Class<U> requestedType) {
		Optional<PojoTypeModel<U>> superTypeModel = getTypeModel().getSuperType( requestedType );
		if ( !superTypeModel.isPresent() ) {
			throw new SearchException( "Requested incompatible type for '" + createAccessor() + "': '" + requestedType + "'" );
		}
		return doCreateAccessor( superTypeModel.get() );
	}

	@Override
	PojoModelPropertyNestedSingleValuedElement<?> doCreateProperty(PojoPropertyModel<?> model,
			TypeMetadataContributorProvider<PojoTypeNodeMetadataContributor> modelContributorProvider,
			PojoIndexModelBinder binder) {
		return new PojoModelPropertyNestedSingleValuedElement<>( this, model, modelContributorProvider, binder );
	}

	@Override
	PojoModelMultiValuedElement doCreateContainer(
			BoundContainerValueExtractor<? super T, ?> boundContainerValueExtractor,
			TypeMetadataContributorProvider<PojoTypeNodeMetadataContributor> modelContributorProvider,
			PojoIndexModelBinder binder) {
		return new PojoModelSingleContainerNestedElement<>( this, boundContainerValueExtractor,
				modelContributorProvider, binder );
	}

	abstract <U> PojoModelElementAccessor<U> doCreateAccessor(PojoTypeModel<U> typeModel);

}
