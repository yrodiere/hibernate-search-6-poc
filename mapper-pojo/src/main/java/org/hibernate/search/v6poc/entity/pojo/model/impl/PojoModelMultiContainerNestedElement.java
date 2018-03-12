/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.model.impl;

import org.hibernate.search.v6poc.entity.mapping.building.spi.TypeMetadataContributorProvider;
import org.hibernate.search.v6poc.entity.pojo.extractor.ContainerValueExtractor;
import org.hibernate.search.v6poc.entity.pojo.extractor.impl.BoundContainerValueExtractor;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoIndexModelBinder;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoTypeNodeMetadataContributor;
import org.hibernate.search.v6poc.entity.pojo.model.PojoModelMultiValuedElementAccessor;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoTypeModel;


/**
 * The element obtained when applying extractors on another element where extractors were already applied.
 *
 * @see PojoModelSingleContainerNestedElement
 */
class PojoModelMultiContainerNestedElement<C, T> extends AbstractPojoModelMultiValuedElement<T> {

	private final AbstractPojoModelMultiValuedElement<C> parent;
	private ContainerValueExtractor<? super C, T> extractor;

	PojoModelMultiContainerNestedElement(AbstractPojoModelMultiValuedElement<C> parent,
			BoundContainerValueExtractor<? super C, T> boundContainerValueExtractor,
			TypeMetadataContributorProvider<PojoTypeNodeMetadataContributor> modelContributorProvider,
			PojoIndexModelBinder binder) {
		super( boundContainerValueExtractor.getExtractedType(), modelContributorProvider, binder );
		this.parent = parent;
		this.extractor = boundContainerValueExtractor.getExtractor();
	}

	@SuppressWarnings("unchecked") // U is always a supertype of T and the accessors are covariant in T
	@Override
	<U> PojoModelMultiValuedElementAccessor<U> doCreateAccessor(PojoTypeModel<U> typeModel) {
		return (PojoModelMultiContainerNestedElementAccessor<C, U>)
				new PojoModelMultiContainerNestedElementAccessor<>( parent.createAccessor(), extractor );
	}

}
