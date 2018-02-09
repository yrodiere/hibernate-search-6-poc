/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.integrationtest.showcase.library.model;

import java.util.List;
import javax.persistence.Basic;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.search.v6poc.entity.pojo.bridge.builtin.spatial.GeoPointBridge;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation.DocumentId;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation.Field;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation.Indexed;

@Entity
@Indexed(index = Library.INDEX)
@GeoPointBridge(fieldName = "location")
public class Library {

	public static final String INDEX = "Library";

	@Id
	@DocumentId
	private Integer id;

	@Basic
	// TODO use multi-fields here
	// TODO use a different analyzer/normalizer for these fields
	@Field(analyzer = "default")
	@Field(name = "name_sort")
	private String name;

	@Basic
	@GeoPointBridge.Latitude
	private Double latitude;

	@Basic
	@GeoPointBridge.Longitude
	private Double longitude;

	@ElementCollection
	//@Field // FIXME field unwrapping
	private List<LibraryService> services;

	@Override
	public String toString() {
		return new StringBuilder( getClass().getSimpleName() )
				.append( "[" )
				.append( id )
				.append( "]" )
				.toString();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public List<LibraryService> getServices() {
		return services;
	}

	public void setServices(List<LibraryService> services) {
		this.services = services;
	}
}
