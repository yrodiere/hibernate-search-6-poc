/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.integrationtest.orm.bridge;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.search.v6poc.backend.document.DocumentElement;
import org.hibernate.search.v6poc.backend.document.IndexObjectFieldAccessor;
import org.hibernate.search.v6poc.backend.document.model.IndexSchemaElement;
import org.hibernate.search.v6poc.engine.spi.BuildContext;
import org.hibernate.search.v6poc.entity.model.SearchModel;
import org.hibernate.search.v6poc.entity.pojo.bridge.PropertyBridge;
import org.hibernate.search.v6poc.entity.pojo.bridge.mapping.AnnotationBridgeBuilder;
import org.hibernate.search.v6poc.entity.pojo.model.PojoElement;
import org.hibernate.search.v6poc.entity.pojo.model.PojoModelProperty;
import org.hibernate.search.v6poc.integrationtest.orm.bridge.annotation.CustomMarkerConsumingPropertyBridgeAnnotation;

public final class CustomMarkerConsumingPropertyBridge implements PropertyBridge {

	public static final class Builder
			implements AnnotationBridgeBuilder<PropertyBridge, CustomMarkerConsumingPropertyBridgeAnnotation> {
		@Override
		public void initialize(CustomMarkerConsumingPropertyBridgeAnnotation annotation) {
			// Nothing to do
		}

		@Override
		public PropertyBridge build(BuildContext buildContext) {
			return new CustomMarkerConsumingPropertyBridge();
		}
	}

	private List<IndexObjectFieldAccessor> objectFieldAccessors = new ArrayList<>();

	private CustomMarkerConsumingPropertyBridge() {
	}

	@Override
	public void bind(IndexSchemaElement indexSchemaElement, PojoModelProperty bridgedPojoModelProperty,
			SearchModel searchModel) {
		List<PojoModelProperty> markedProperties = bridgedPojoModelProperty.properties()
				.filter( property -> property.markers( CustomMarker.class ).findAny().isPresent() )
				.collect( Collectors.toList() );
		for ( PojoModelProperty property : markedProperties ) {
			objectFieldAccessors.add( indexSchemaElement.objectField( property.getName() ).createAccessor() );
		}
	}

	@Override
	public void write(DocumentElement target, PojoElement source) {
		for ( IndexObjectFieldAccessor objectFieldAccessor : objectFieldAccessors ) {
			objectFieldAccessor.add( target );
		}
	}
}
