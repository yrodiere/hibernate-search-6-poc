/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.model.impl;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.hibernate.search.v6poc.entity.mapping.building.spi.TypeMetadataContributorProvider;
import org.hibernate.search.v6poc.entity.pojo.bridge.mapping.MarkerBuilder;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoIndexModelBinder;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoPropertyNodeModelCollector;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoTypeNodeMetadataContributor;
import org.hibernate.search.v6poc.entity.pojo.model.PojoModelMultiValuedElementAccessor;
import org.hibernate.search.v6poc.entity.pojo.model.PojoModelMultiValuedProperty;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoPropertyModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoTypeModel;


/**
 * The element obtained when getting property values on another element where extractors were applied.
 *
 * @see PojoModelPropertyNestedSingleValuedElement
 */
class PojoModelPropertyNestedMultiValuedElement<T> extends AbstractPojoModelMultiValuedElement<T>
		implements PojoModelMultiValuedProperty, PojoPropertyNodeModelCollector {

	private final AbstractPojoModelMultiValuedElement<?> parent;

	private final PojoPropertyModel<?> propertyModel;

	private final Map<Class<?>, List<?>> markers = new HashMap<>();

	PojoModelPropertyNestedMultiValuedElement(AbstractPojoModelMultiValuedElement<?> parent,
			PojoPropertyModel<T> propertyModel,
			TypeMetadataContributorProvider<PojoTypeNodeMetadataContributor> modelContributorProvider,
			PojoIndexModelBinder binder) {
		super( propertyModel.getTypeModel(), modelContributorProvider, binder );
		this.parent = parent;
		this.propertyModel = propertyModel;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <M> Stream<M> markers(Class<M> markerType) {
		return ( (List<M>) this.markers.getOrDefault( markerType, Collections.emptyList() ) )
				.stream();
	}

	@Override
	public final void marker(MarkerBuilder builder) {
		doAddMarker( builder.build() );
	}

	@Override
	public String getName() {
		return propertyModel.getName();
	}

	@Override
	<U> PojoModelMultiValuedElementAccessor<U> doCreateAccessor(PojoTypeModel<U> typeModel) {
		return new PojoModelPropertyNestedMultiValuedElementAccessor<>(
				parent.createAccessor(), propertyModel.getHandle()
		);
	}

	@SuppressWarnings("unchecked")
	private <M> void doAddMarker(M marker) {
		Class<M> markerType = (Class<M>) (
				marker instanceof Annotation ? ((Annotation) marker).annotationType()
						: marker.getClass()
		);
		List<M> list = (List<M>) markers.computeIfAbsent( markerType, ignored -> new ArrayList<M>() );
		list.add( marker );
	}
}
