/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.model.augmented.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.hibernate.search.v6poc.entity.pojo.extractor.impl.ContainerValueExtractorPath;

public class PojoAugmentedPropertyModel {

	private final String propertyName;
	private final Map<ContainerValueExtractorPath, PojoAugmentedValueModel> values;
	private final Map<Class<?>, List<?>> markers;

	public PojoAugmentedPropertyModel(String propertyName,
			Map<ContainerValueExtractorPath, PojoAugmentedValueModel> values,
			Map<Class<?>, List<?>> markers) {
		this.propertyName = propertyName;
		this.values = values;
		this.markers = markers;
	}

	public PojoAugmentedValueModel getValue(ContainerValueExtractorPath extractorPath) {
		PojoAugmentedValueModel result = values.get( extractorPath );
		if ( result == null ) {
			result = new PojoAugmentedValueModel(
					new PojoAssociationPath( propertyName, extractorPath ),
					null
			);
		}
		return result;
	}

	public Collection<PojoAugmentedValueModel> getAugmentedValues() {
		return values.values();
	}

	@SuppressWarnings("unchecked")
	public <M> Stream<M> getMarkers(Class<M> markerType) {
		return ( (List<M>) this.markers.getOrDefault( markerType, Collections.emptyList() ) )
				.stream();
	}


}
