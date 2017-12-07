/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.bridge.builtin.spatial;

import org.hibernate.search.v6poc.entity.pojo.bridge.builtin.spatial.impl.LatitudeMarker;
import org.hibernate.search.v6poc.entity.pojo.bridge.mapping.AnnotationMarkerBuilder;

public class LatitudeMarkerBuilder implements AnnotationMarkerBuilder<GeoPointBridge.Latitude> {

	private String markerSet;

	@Override
	public void initialize(GeoPointBridge.Latitude annotation) {
		markerSet( annotation.markerSet() );
	}

	public LatitudeMarkerBuilder markerSet(String markerSet) {
		this.markerSet = markerSet;
		return this;
	}

	@Override
	public Object build() {
		return new LatitudeMarker( markerSet );
	}

}
