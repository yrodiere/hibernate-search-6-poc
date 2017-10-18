/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.hibernate.search.v6poc.backend.document.model.spi.IndexModelCollector;
import org.hibernate.search.v6poc.backend.document.spi.DocumentState;
import org.hibernate.search.v6poc.backend.document.spi.IndexFieldReference;
import org.hibernate.search.v6poc.backend.elasticsearch.ElasticsearchExtension;
import org.hibernate.search.v6poc.backend.elasticsearch.client.impl.StubElasticsearchClient;
import org.hibernate.search.v6poc.backend.elasticsearch.client.impl.StubElasticsearchClient.Request;
import org.hibernate.search.v6poc.backend.elasticsearch.impl.ElasticsearchBackendFactory;
import org.hibernate.search.v6poc.bridge.declaration.spi.BridgeBeanReference;
import org.hibernate.search.v6poc.bridge.declaration.spi.BridgeMapping;
import org.hibernate.search.v6poc.bridge.mapping.BridgeDefinitionBase;
import org.hibernate.search.v6poc.bridge.spi.Bridge;
import org.hibernate.search.v6poc.engine.SearchManagerFactory;
import org.hibernate.search.v6poc.entity.javabean.mapping.JavaBeanMapper;
import org.hibernate.search.v6poc.entity.javabean.mapping.JavaBeanMappingType;
import org.hibernate.search.v6poc.entity.model.spi.Indexable;
import org.hibernate.search.v6poc.entity.model.spi.IndexableModel;
import org.hibernate.search.v6poc.entity.model.spi.IndexableReference;
import org.hibernate.search.v6poc.entity.pojo.mapping.PojoSearchManager;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.programmatic.MappingDefinition;
import org.hibernate.search.v6poc.entity.pojo.search.PojoReference;
import org.hibernate.search.v6poc.search.SearchQuery;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.json.JSONException;

import static org.hibernate.search.v6poc.util.StubAssert.assertRequest;

/**
 * @author Yoann Rodiere
 */
public class PojoElasticsearchExtensionIT {

	private SearchManagerFactory managerFactory;

	private static final String HOST_1 = "http://es1.mycompany.com:9200/";

	@Before
	public void setup() throws JSONException {
		MappingDefinition mapping = JavaBeanMapper.get().programmaticMapping();
		mapping.type( IndexedEntity.class )
				.indexed( IndexedEntity.INDEX )
				.property( "id" )
						.documentId()
				.property( "jsonString" )
					.bridge(
							new MyElasticsearchBridgeDefinition()
					);

		managerFactory = SearchManagerFactory.builder()
				.addMapping( mapping )
				.setProperty( "backend.elasticsearchBackend_1.type", ElasticsearchBackendFactory.class.getName() )
				.setProperty( "backend.elasticsearchBackend_1.host", HOST_1 )
				.setProperty( "index.default.backend", "elasticsearchBackend_1" )
				.build();

		Map<String, List<Request>> requests = StubElasticsearchClient.drainRequestsByIndex();

		assertRequest( requests, IndexedEntity.INDEX, 0, HOST_1, "createIndex", null,
				"{"
					+ "'mapping': {"
						+ "'properties': {"
							+ "'jsonStringField': {"
								// As defined in MyElasticsearchBridgeImpl
								+ "'esAttribute1': 'val1'"
							+ "}"
						+ "}"
					+ "}"
				+ "}" );
	}

	@After
	public void cleanup() {
		StubElasticsearchClient.drainRequestsByIndex();
		if ( managerFactory != null ) {
			managerFactory.close();
		}
	}

	@Test
	public void index() throws JSONException {
		try ( PojoSearchManager manager = managerFactory.createSearchManager( JavaBeanMappingType.get() ) ) {
			IndexedEntity entity1 = new IndexedEntity();
			entity1.setId( 1 );
			entity1.setJsonString( "{'esProperty1':'val1'}" );

			manager.getWorker().add( entity1 );
		}

		Map<String, List<Request>> requests = StubElasticsearchClient.drainRequestsByIndex();
		// We expect the first add to be removed due to the delete
		assertRequest( requests, IndexedEntity.INDEX, 0, HOST_1, "add", "2",
				"{"
					+ "'jsonStringField': {"
						+ "'esProperty1': 'val1'"
					+ "}"
				+ "}" );
	}

	@Test
	public void search() throws JSONException {
		try (PojoSearchManager manager = managerFactory.createSearchManager( JavaBeanMappingType.get() )) {
			SearchQuery<PojoReference> query = manager.search( IndexedEntity.class )
					.asReferences()
					.bool()
							.should().withExtension( ElasticsearchExtension.get() )
									.fromJsonString( "{'es1': 'val1'}" )
							.should().withExtensionOptional(
									ElasticsearchExtension.get(),
									// FIXME find some way to forbid using the context twice... ?
									c -> c.fromJsonString( "{'es2': 'val2'}" )
							)
							.must().withExtensionOptional(
									ElasticsearchExtension.get(),
									// FIXME find some way to forbid using the context twice... ?
									c -> c.fromJsonString( "{'es3': 'val3'}" ),
									c -> c.match().onField( "fallback1" ).matching( "val1" )
							)
							.end()
					.build();

			query.execute();
		}

		Map<String, List<Request>> requests = StubElasticsearchClient.drainRequestsByIndex();
		assertRequest( requests, Arrays.asList( IndexedEntity.INDEX ), 0,
				HOST_1, "search", null /* No ID */,
				null,
				"{"
					+ "'query': {"
						+ "'bool': {"
							+ "'should': ["
								+ "{"
									+ "'es1': 'val1'"
								+ "},"
								+ "{"
									+ "'es2': 'val2'"
								+ "}"
							+ "],"
							+ "'must': {"
								+ "'es3': 'val3'"
							+ "}"
						+ "}"
					+ "}"
				+ "}" );
	}

	public static final class IndexedEntity {

		public static final String INDEX = "IndexedEntity";

		private Integer id;

		private String jsonString;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getJsonString() {
			return jsonString;
		}

		public void setJsonString(String jsonString) {
			this.jsonString = jsonString;
		}

	}

	@BridgeMapping(implementation = @BridgeBeanReference(type = MyElasticsearchBridgeImpl.class))
	@Target(value = { ElementType.TYPE, ElementType.METHOD, ElementType.FIELD })
	@Retention(RetentionPolicy.RUNTIME)
	public @interface MyElasticsearchBridge {
	}

	public static final class MyElasticsearchBridgeDefinition extends BridgeDefinitionBase<MyElasticsearchBridge> {

		@Override
		protected Class<MyElasticsearchBridge> getAnnotationClass() {
			return MyElasticsearchBridge.class;
		}

		public MyElasticsearchBridgeDefinition objectName(String value) {
			addParameter( "objectName", value );
			return this;
		}
	}

	public static final class MyElasticsearchBridgeImpl implements Bridge<MyElasticsearchBridge> {

		private IndexableReference<String> sourceRef;
		private IndexFieldReference<String> fieldRef;

		@Override
		public void bind(IndexableModel indexableModel, IndexModelCollector indexModelCollector) {
			sourceRef = indexableModel.asReference( String.class );
			fieldRef = indexModelCollector.field( "jsonStringField" )
					.withExtension( ElasticsearchExtension.get() )
					.fromJsonString(
							"{"
								+ "'esAttribute1': 'val1'"
							+ "}"
					).asReference();
		}

		@Override
		public void toDocument(Indexable source, DocumentState target) {
			String sourceValue = source.get( sourceRef );
			fieldRef.add( target, sourceValue );
		}

	}
}