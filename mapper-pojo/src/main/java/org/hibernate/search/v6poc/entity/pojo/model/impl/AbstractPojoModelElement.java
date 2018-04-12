/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.model.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.hibernate.search.v6poc.entity.pojo.dirtiness.building.impl.PojoIndexingDependencyCollectorTypeNode;
import org.hibernate.search.v6poc.entity.pojo.model.PojoModelElement;
import org.hibernate.search.v6poc.entity.pojo.model.PojoModelElementAccessor;
import org.hibernate.search.v6poc.entity.pojo.model.PojoModelProperty;
import org.hibernate.search.v6poc.entity.pojo.model.additionalmetadata.building.impl.PojoTypeAdditionalMetadataProvider;
import org.hibernate.search.v6poc.entity.pojo.model.additionalmetadata.impl.PojoPropertyAdditionalMetadata;
import org.hibernate.search.v6poc.entity.pojo.model.additionalmetadata.impl.PojoTypeAdditionalMetadata;
import org.hibernate.search.v6poc.entity.pojo.model.path.impl.BoundPojoModelPathTypeNode;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoPropertyModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoRawTypeModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoTypeModel;
import org.hibernate.search.v6poc.util.SearchException;

abstract class AbstractPojoModelElement<V> implements PojoModelElement {

	private final PojoTypeAdditionalMetadataProvider typeAdditionalMetadataProvider;
	private final Map<String, PojoModelNestedElement<V, ?>> properties = new HashMap<>();
	private PojoTypeAdditionalMetadata typeAdditionalMetadata;
	private boolean propertiesInitialized = false;

	private PojoModelElementAccessor<?> accessor;

	AbstractPojoModelElement(PojoTypeAdditionalMetadataProvider typeAdditionalMetadataProvider) {
		this.typeAdditionalMetadataProvider = typeAdditionalMetadataProvider;
	}

	@Override
	@SuppressWarnings("unchecked") // The cast is checked using reflection
	public final <T> PojoModelElementAccessor<T> createAccessor(Class<T> requestedType) {
		if ( !isAssignableTo( requestedType ) ) {
			throw new SearchException( "Requested incompatible type for '" + createAccessor() + "': '" + requestedType + "'" );
		}
		return (PojoModelElementAccessor<T>) createAccessor();
	}

	@Override
	public PojoModelElementAccessor<?> createAccessor() {
		if ( accessor == null ) {
			accessor = doCreateAccessor();
		}
		return accessor;
	}

	@Override
	public boolean isAssignableTo(Class<?> clazz) {
		return getTypeModel().getRawType().isSubTypeOf( clazz );
	}

	@Override
	public PojoModelNestedElement<?, ?> property(String relativeName) {
		return properties.computeIfAbsent( relativeName, name -> {
			BoundPojoModelPathTypeNode<V> modelPathTypeNode = getModelPathTypeNode();
			PojoPropertyModel<?> model = modelPathTypeNode.getTypeModel().getProperty( name );
			PojoPropertyAdditionalMetadata additionalMetadata = getTypeAdditionalMetadata().getPropertyAdditionalMetadata( name );
			return new PojoModelNestedElement<>(
					this,
					modelPathTypeNode.property( model.getHandle() ),
					additionalMetadata,
					typeAdditionalMetadataProvider
			);
		} );
	}

	@SuppressWarnings({ "unchecked", "rawtypes" }) // Stream is covariant in T
	@Override
	public Stream<? extends PojoModelProperty> properties() {
		if ( !propertiesInitialized ) {
			// Populate all the known properties
			getTypeModel().getRawType().getAscendingSuperTypes()
					.flatMap( PojoRawTypeModel::getDeclaredProperties )
					.map( PojoPropertyModel::getName )
					.forEach( this::property );
			propertiesInitialized = true;
		}
		return properties.values().stream();
	}

	abstract PojoModelElementAccessor<V> doCreateAccessor();

	abstract BoundPojoModelPathTypeNode<V> getModelPathTypeNode();

	final boolean hasAccessor() {
		return accessor != null;
	}

	final void contributePropertyDependencies(PojoIndexingDependencyCollectorTypeNode<V> dependencyCollector) {
		for ( Map.Entry<String, PojoModelNestedElement<V, ?>> entry : properties.entrySet() ) {
			entry.getValue().contributeDependencies( dependencyCollector );
		}
	}

	private PojoTypeModel<V> getTypeModel() {
		return getModelPathTypeNode().getTypeModel();
	}

	private PojoTypeAdditionalMetadata getTypeAdditionalMetadata() {
		if ( typeAdditionalMetadata == null ) {
			typeAdditionalMetadata = typeAdditionalMetadataProvider.get( getModelPathTypeNode().getTypeModel().getRawType() );
		}
		return typeAdditionalMetadata;
	}
}
