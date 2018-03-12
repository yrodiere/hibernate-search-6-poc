/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.hibernate.search.v6poc.entity.mapping.building.spi.TypeMetadataContributorProvider;
import org.hibernate.search.v6poc.entity.pojo.extractor.ContainerValueExtractor;
import org.hibernate.search.v6poc.entity.pojo.extractor.impl.BoundContainerValueExtractor;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoIndexModelBinder;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoPropertyNodeModelCollector;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoTypeNodeMetadataContributor;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoTypeNodeModelCollector;
import org.hibernate.search.v6poc.entity.pojo.model.PojoModelMultiValuedElement;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoGenericTypeModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoPropertyModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoTypeModel;


/**
 * @author Yoann Rodiere
 */
abstract class AbstractPojoModelElement<T, P extends PojoPropertyNodeModelCollector>
		implements PojoTypeNodeModelCollector {

	private final PojoGenericTypeModel<T> typeModel;
	private final TypeMetadataContributorProvider<PojoTypeNodeMetadataContributor> modelContributorProvider;
	private final PojoIndexModelBinder binder;

	private final Map<String, P> propertyModelsByName = new HashMap<>();
	private final Map<List<Class<? extends ContainerValueExtractor>>, PojoModelMultiValuedElement>
			extractedElementByExtractorClasses = new HashMap<>();

	private boolean markersForTypeInitialized = false;

	AbstractPojoModelElement(PojoGenericTypeModel<T> typeModel,
			TypeMetadataContributorProvider<PojoTypeNodeMetadataContributor> modelContributorProvider,
			PojoIndexModelBinder binder) {
		this.typeModel = typeModel;
		this.modelContributorProvider = modelContributorProvider;
		this.binder = binder;
	}

	public PojoModelMultiValuedElement extract(Class<? extends ContainerValueExtractor> extractorClass) {
		return extract( Collections.singletonList( extractorClass ) );
	}

	public PojoModelMultiValuedElement extract(List<? extends Class<? extends ContainerValueExtractor>> extractorClasses) {
		List<Class<? extends ContainerValueExtractor>> immutableExtractorClassList =
				new ArrayList<>( extractorClasses );
		return extractedElementByExtractorClasses.computeIfAbsent( immutableExtractorClassList, classes -> {
			BoundContainerValueExtractor<? super T, ?> boundContainerValueExtractor =
					binder.<T>createExplicitExtractors( typeModel, classes );
			return doCreateContainer( boundContainerValueExtractor, modelContributorProvider, binder );
		} );
	}

	public boolean isAssignableTo(Class<?> clazz) {
		return typeModel.getSuperType( clazz ).isPresent();
	}

	public P property(String relativeName) {
		initMarkersForType();
		return propertyModelsByName.computeIfAbsent( relativeName, name -> {
			PojoPropertyModel<?> model = typeModel.getProperty( name );
			return doCreateProperty( model, modelContributorProvider, binder );
		} );
	}

	public Stream<P> properties() {
		initMarkersForType();
		return propertyModelsByName.values().stream();
	}

	abstract P doCreateProperty(PojoPropertyModel<?> model,
			TypeMetadataContributorProvider<PojoTypeNodeMetadataContributor> modelContributorProvider,
			PojoIndexModelBinder binder);

	abstract PojoModelMultiValuedElement doCreateContainer(
			BoundContainerValueExtractor<? super T, ?> boundContainerValueExtractor,
			TypeMetadataContributorProvider<PojoTypeNodeMetadataContributor> modelContributorProvider,
			PojoIndexModelBinder binder);

	final PojoTypeModel<T> getTypeModel() {
		return typeModel;
	}

	/*
	 * Lazily initialize markers.
	 * Lazy initialization is necessary to avoid infinite recursion.
	 */
	private void initMarkersForType() {
		if ( !markersForTypeInitialized ) {
			this.markersForTypeInitialized = true;
			modelContributorProvider.forEach(
					typeModel.getRawType(),
					c -> c.contributeModel( this )
			);
		}
	}

}
