/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.integrationtest;

import java.util.List;
import java.util.Map;

import org.hibernate.search.v6poc.backend.elasticsearch.client.impl.StubElasticsearchClient;
import org.hibernate.search.v6poc.backend.elasticsearch.client.impl.StubElasticsearchClient.Request;
import org.hibernate.search.v6poc.backend.elasticsearch.impl.ElasticsearchBackendFactory;
import org.hibernate.search.v6poc.backend.spatial.GeoPoint;
import org.hibernate.search.v6poc.engine.SearchMappingRepository;
import org.hibernate.search.v6poc.engine.SearchMappingRepositoryBuilder;
import org.hibernate.search.v6poc.entity.javabean.JavaBeanMapping;
import org.hibernate.search.v6poc.entity.javabean.JavaBeanMappingContributor;
import org.hibernate.search.v6poc.entity.pojo.bridge.builtin.spatial.GeoPointBridge;
import org.hibernate.search.v6poc.entity.pojo.mapping.PojoSearchManager;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation.AnnotationMappingDefinition;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation.DocumentId;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation.Indexed;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.json.JSONException;

import static org.hibernate.search.v6poc.integrationtest.util.StubAssert.assertRequest;

/**
 * @author Yoann Rodiere
 */
public class JavaBeanElasticsearchAnnotationMappingGeoPointIT {

	private SearchMappingRepository mappingRepository;

	private JavaBeanMapping mapping;

	private static final String HOST = "http://es1.mycompany.com:9200/";

	@Before
	public void setup() throws JSONException {
		SearchMappingRepositoryBuilder mappingRepositoryBuilder = SearchMappingRepository.builder()
				.setProperty( "backend.elasticsearchBackend.type", ElasticsearchBackendFactory.class.getName() )
				.setProperty( "backend.elasticsearchBackend.host", HOST )
				.setProperty( "index.default.backend", "elasticsearchBackend" );

		JavaBeanMappingContributor contributor = new JavaBeanMappingContributor( mappingRepositoryBuilder );

		AnnotationMappingDefinition mappingDefinition = contributor.annotationMapping();

		mappingDefinition.add( GeoPointOnTypeEntity.class );
		mappingDefinition.add( GeoPointOnCoordinatesPropertyEntity.class );
		mappingDefinition.add( GeoPointOnCustomCoordinatesPropertyEntity.class );
		mappingDefinition.add( CustomCoordinates.class );

		mappingRepository = mappingRepositoryBuilder.build();
		mapping = contributor.getResult();

		Map<String, List<Request>> requests = StubElasticsearchClient.drainRequestsByIndex();

		assertRequest( requests, GeoPointOnTypeEntity.INDEX, 0, HOST, "createIndex", null,
				"{"
					+ "'mapping': {"
						+ "'properties': {"
							+ "'homeLocation': {"
								+ "'type': 'geo_point'"
							+ "},"
							+ "'workLocation': {"
								+ "'type': 'geo_point'"
							+ "}"
						+ "}"
					+ "}"
				+ "}" );

		assertRequest( requests, GeoPointOnCoordinatesPropertyEntity.INDEX, 0, HOST, "createIndex", null,
				"{"
					+ "'mapping': {"
						+ "'properties': {"
							+ "'location': {"
								+ "'type': 'geo_point'"
							+ "}"
						+ "}"
					+ "}"
				+ "}" );

		assertRequest( requests, GeoPointOnCustomCoordinatesPropertyEntity.INDEX, 0, HOST, "createIndex", null,
				"{"
					+ "'mapping': {"
						+ "'properties': {"
							+ "'location': {"
								+ "'type': 'geo_point'"
							+ "}"
						+ "}"
					+ "}"
				+ "}" );
	}

	@After
	public void cleanup() {
		StubElasticsearchClient.drainRequestsByIndex();
		if ( mappingRepository != null ) {
			mappingRepository.close();
		}
	}

	@Test
	public void index() throws JSONException {
		try ( PojoSearchManager manager = mapping.createSearchManager() ) {
			GeoPointOnTypeEntity entity1 = new GeoPointOnTypeEntity();
			entity1.setId( 1 );
			entity1.setHomeLatitude( 1.1d );
			entity1.setHomeLongitude( 1.2d );
			entity1.setWorkLatitude( 1.3d );
			entity1.setWorkLongitude( 1.4d );
			GeoPointOnCoordinatesPropertyEntity entity2 = new GeoPointOnCoordinatesPropertyEntity();
			entity2.setId( 2 );
			entity2.setCoord( new GeoPoint() {
				@Override
				public double getLatitude() {
					return 2.1d;
				}
				@Override
				public double getLongitude() {
					return 2.2d;
				}
			} );
			GeoPointOnCustomCoordinatesPropertyEntity entity3 = new GeoPointOnCustomCoordinatesPropertyEntity();
			entity3.setId( 3 );
			entity3.setCoord( new CustomCoordinates( 3.1d, 3.2d ) );

			manager.getMainWorker().add( entity1 );
			manager.getMainWorker().add( entity2 );
			manager.getMainWorker().add( entity3 );
		}

		Map<String, List<Request>> requests = StubElasticsearchClient.drainRequestsByIndex();
		assertRequest( requests, GeoPointOnTypeEntity.INDEX, 0, HOST, "add", "1",
				"{"
					+ "'homeLocation': {"
						+ "'lat': 1.1,"
						+ "'lon': 1.2"
					+ "},"
					+ "'workLocation': {"
						+ "'lat': 1.3,"
						+ "'lon': 1.4"
					+ "}"
				+ "}" );
		assertRequest( requests, GeoPointOnCoordinatesPropertyEntity.INDEX, 0, HOST, "add", "2",
				"{"
					+ "'location': {"
						+ "'lat': 2.1,"
						+ "'lon': 2.2"
					+ "}"
				+ "}" );
		assertRequest( requests, GeoPointOnCustomCoordinatesPropertyEntity.INDEX, 0, HOST, "add", "3",
				"{"
					+ "'location': {"
						+ "'lat': 3.1,"
						+ "'lon': 3.2"
					+ "}"
				+ "}" );
	}

	@Indexed(index = GeoPointOnTypeEntity.INDEX)
	@GeoPointBridge(fieldName = "homeLocation", markerSet = "home")
	@GeoPointBridge(fieldName = "workLocation", markerSet = "work")
	public static final class GeoPointOnTypeEntity {

		public static final String INDEX = "GeoPointOnTypeEntity";

		private Integer id;

		private Double homeLatitude;

		private Double homeLongitude;

		private Double workLatitude;

		private Double workLongitude;

		@DocumentId
		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		@GeoPointBridge.Latitude(markerSet = "home")
		public Double getHomeLatitude() {
			return homeLatitude;
		}

		public void setHomeLatitude(Double homeLatitude) {
			this.homeLatitude = homeLatitude;
		}

		@GeoPointBridge.Longitude(markerSet = "home")
		public Double getHomeLongitude() {
			return homeLongitude;
		}

		public void setHomeLongitude(Double homeLongitude) {
			this.homeLongitude = homeLongitude;
		}

		@GeoPointBridge.Latitude(markerSet = "work")
		public Double getWorkLatitude() {
			return workLatitude;
		}

		public void setWorkLatitude(Double workLatitude) {
			this.workLatitude = workLatitude;
		}

		@GeoPointBridge.Longitude(markerSet = "work")
		public Double getWorkLongitude() {
			return workLongitude;
		}

		public void setWorkLongitude(Double workLongitude) {
			this.workLongitude = workLongitude;
		}

	}

	@Indexed(index = GeoPointOnCoordinatesPropertyEntity.INDEX)
	public static final class GeoPointOnCoordinatesPropertyEntity {

		public static final String INDEX = "GeoPointOnCoordinatesPropertyEntity";

		private Integer id;

		private GeoPoint coord;

		@DocumentId
		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		@GeoPointBridge(fieldName = "location")
		public GeoPoint getCoord() {
			return coord;
		}

		public void setCoord(GeoPoint coord) {
			this.coord = coord;
		}

	}

	@Indexed(index = GeoPointOnCustomCoordinatesPropertyEntity.INDEX)
	public static final class GeoPointOnCustomCoordinatesPropertyEntity {

		public static final String INDEX = "GeoPointOnCustomCoordinatesPropertyEntity";

		private Integer id;

		private CustomCoordinates coord;

		@DocumentId
		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		@GeoPointBridge(fieldName = "location")
		public CustomCoordinates getCoord() {
			return coord;
		}

		public void setCoord(CustomCoordinates coord) {
			this.coord = coord;
		}

	}

	// Does not implement GeoPoint on purpose
	public static class CustomCoordinates {

		private final Double lat;
		private final Double lon;

		public CustomCoordinates(Double lat, Double lon) {
			this.lat = lat;
			this.lon = lon;
		}

		// TODO make this work even when the property is of primitive type double
		@GeoPointBridge.Latitude
		public Double getLat() {
			return lat;
		}

		@GeoPointBridge.Longitude
		public Double getLon() {
			return lon;
		}
	}

}