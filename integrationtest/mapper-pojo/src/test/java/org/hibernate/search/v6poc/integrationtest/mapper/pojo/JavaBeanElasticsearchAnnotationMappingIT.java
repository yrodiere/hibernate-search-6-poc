/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.integrationtest.mapper.pojo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.search.v6poc.backend.document.DocumentElement;
import org.hibernate.search.v6poc.backend.document.IndexFieldAccessor;
import org.hibernate.search.v6poc.backend.document.IndexObjectFieldAccessor;
import org.hibernate.search.v6poc.backend.document.model.IndexSchemaElement;
import org.hibernate.search.v6poc.backend.document.model.spi.IndexSchemaObjectField;
import org.hibernate.search.v6poc.engine.SearchMappingRepository;
import org.hibernate.search.v6poc.engine.SearchMappingRepositoryBuilder;
import org.hibernate.search.v6poc.engine.spi.BuildContext;
import org.hibernate.search.v6poc.entity.javabean.JavaBeanMapping;
import org.hibernate.search.v6poc.entity.javabean.JavaBeanMappingContributor;
import org.hibernate.search.v6poc.entity.model.SearchModel;
import org.hibernate.search.v6poc.entity.pojo.bridge.Bridge;
import org.hibernate.search.v6poc.entity.pojo.bridge.FunctionBridge;
import org.hibernate.search.v6poc.entity.pojo.bridge.builtin.impl.DefaultIntegerIdentifierBridge;
import org.hibernate.search.v6poc.entity.pojo.bridge.declaration.BridgeMapping;
import org.hibernate.search.v6poc.entity.pojo.bridge.declaration.BridgeMappingBuilderReference;
import org.hibernate.search.v6poc.entity.pojo.bridge.mapping.AnnotationBridgeBuilder;
import org.hibernate.search.v6poc.entity.pojo.mapping.PojoSearchManager;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation.DocumentId;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation.Field;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation.FunctionBridgeBeanReference;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation.IdentifierBridgeBeanReference;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.hibernate.search.v6poc.entity.pojo.mapping.impl.PojoReferenceImpl;
import org.hibernate.search.v6poc.entity.pojo.model.PojoElement;
import org.hibernate.search.v6poc.entity.pojo.model.PojoModelElement;
import org.hibernate.search.v6poc.entity.pojo.model.PojoModelElementAccessor;
import org.hibernate.search.v6poc.entity.pojo.search.PojoReference;
import org.hibernate.search.v6poc.integrationtest.util.common.rule.BackendMock;
import org.hibernate.search.v6poc.integrationtest.util.common.rule.StubSearchWorkBehavior;
import org.hibernate.search.v6poc.integrationtest.util.common.stub.backend.index.impl.StubBackendFactory;
import org.hibernate.search.v6poc.search.ProjectionConstants;
import org.hibernate.search.v6poc.search.SearchQuery;
import org.hibernate.search.v6poc.search.SearchResult;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.hibernate.search.v6poc.integrationtest.util.common.assertion.SearchResultAssert.assertThat;
import static org.hibernate.search.v6poc.integrationtest.util.common.stub.backend.StubBackendUtils.reference;

/**
 * @author Yoann Rodiere
 */
public class JavaBeanElasticsearchAnnotationMappingIT {

	private SearchMappingRepository mappingRepository;

	private JavaBeanMapping mapping;

	@Rule
	public BackendMock backendMock = new BackendMock( "stubBackend" );

	@Before
	public void setup() {
		SearchMappingRepositoryBuilder mappingRepositoryBuilder = SearchMappingRepository.builder()
				.setProperty( "backend.stubBackend.type", StubBackendFactory.class.getName() )
				.setProperty( "index.default.backend", "stubBackend" );

		JavaBeanMappingContributor contributor = new JavaBeanMappingContributor( mappingRepositoryBuilder );

		contributor.annotationMapping().add( IndexedEntity.class );

		Set<Class<?>> classSet = new HashSet<>();
		classSet.add( OtherIndexedEntity.class );
		classSet.add( YetAnotherIndexedEntity.class );
		contributor.annotationMapping().add( classSet );

		backendMock.expectSchema( OtherIndexedEntity.INDEX, b -> b
				.field( "numeric", Integer.class )
				.field( "numericAsString", String.class )
		);
		backendMock.expectSchema( YetAnotherIndexedEntity.INDEX, b -> b
				.objectField( "customBridgeOnProperty", b2 -> b2
						.field( "date", LocalDate.class )
						.field( "text", String.class )
				)
				.field( "myLocalDateField", LocalDate.class )
				.field( "numeric", Integer.class )
		);
		backendMock.expectSchema( IndexedEntity.INDEX, b -> b
				.objectField( "customBridgeOnClass", b2 -> b2
						.field( "date", LocalDate.class )
						.field( "text", String.class )
				)
				.objectField( "customBridgeOnProperty", b2 -> b2
						.field( "date", LocalDate.class )
						.field( "text", String.class )
				)
				.objectField( "embedded", b2 -> b2
						.objectField( "prefix_customBridgeOnClass", b3 -> b3
								.field( "date", LocalDate.class )
								.field( "text", String.class )
						)
						.objectField( "prefix_customBridgeOnProperty", b3 -> b3
								.field( "date", LocalDate.class )
								.field( "text", String.class )
						)
						.objectField( "prefix_embedded", b3 -> b3
								.objectField( "prefix_customBridgeOnClass", b4 -> b4
										.field( "text", String.class )
								)
						)
						.field( "prefix_myLocalDateField", LocalDate.class )
						.field( "prefix_myTextField", String.class )
				)
				.field( "myLocalDateField", LocalDate.class )
		);

		mappingRepository = mappingRepositoryBuilder.build();
		mapping = contributor.getResult();
		backendMock.verifyExpectationsMet();
	}

	@After
	public void cleanup() {
		if ( mappingRepository != null ) {
			mappingRepository.close();
		}
	}

	@Test
	public void index() {
		try (PojoSearchManager manager = mapping.createSearchManager()) {
			IndexedEntity entity1 = new IndexedEntity();
			entity1.setId( 1 );
			entity1.setText( "this is text (1)" );
			entity1.setLocalDate( LocalDate.of( 2017, 11, 1 ) );
			IndexedEntity entity2 = new IndexedEntity();
			entity2.setId( 2 );
			entity2.setText( "some more text (2)" );
			entity2.setLocalDate( LocalDate.of( 2017, 11, 2 ) );
			IndexedEntity entity3 = new IndexedEntity();
			entity3.setId( 3 );
			entity3.setText( "some more text (3)" );
			entity3.setLocalDate( LocalDate.of( 2017, 11, 3 ) );
			OtherIndexedEntity entity4 = new OtherIndexedEntity();
			entity4.setId( 4 );
			entity4.setNumeric( 404 );

			entity1.setEmbedded( entity2 );
			entity2.setEmbedded( entity3 );

			manager.getMainWorker().add( entity1 );
			manager.getMainWorker().add( entity2 );
			manager.getMainWorker().add( entity4 );
			manager.getMainWorker().delete( entity1 );
			manager.getMainWorker().add( entity3 );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.add( "2", b -> b
							.field( "myLocalDateField", entity2.getLocalDate() )
							.field( "myTextField", entity2.getText() )
							.objectField( "customBridgeOnClass", b2 -> b2
									.field( "text", entity2.getText() )
									.field( "date", entity2.getLocalDate() )
							)
							.objectField( "customBridgeOnProperty", b2 -> b2
									.field( "text", entity3.getText() )
									.field( "date", entity3.getLocalDate() )
							)
							.objectField( "embedded", b2 -> b2
									.objectField( "prefix_customBridgeOnClass", b3 -> b3
											.field( "text", entity3.getText() )
											.field( "date", entity3.getLocalDate() )
									)
									.field( "prefix_myTextField", entity3.getText() )
									.field( "prefix_myLocalDateField", entity3.getLocalDate() )
							)
					)
					.add( "3", b -> b
							.field( "myLocalDateField", entity3.getLocalDate() )
							.field( "myTextField", entity3.getText() )
							.objectField( "customBridgeOnClass", b2 -> b2
									.field( "text", entity3.getText() )
									.field( "date", entity3.getLocalDate() )
							)
					)
					.preparedThenExecuted();
			backendMock.expectWorks( OtherIndexedEntity.INDEX )
					.add( "4", b -> b
							.field( "numeric", entity4.getNumeric() )
							.field( "numericAsString", String.valueOf( entity4.getNumeric() ) )
					)
					.preparedThenExecuted();
		}
	}

	@Test
	public void search() {
		try (PojoSearchManager manager = mapping.createSearchManager()) {
			SearchQuery<PojoReference> query = manager.search(
					Arrays.asList( IndexedEntity.class, YetAnotherIndexedEntity.class )
			)
					.query()
					.asReferences()
					.predicate().all().end()
					.build();
			query.setFirstResult( 3L );
			query.setMaxResults( 2L );

			backendMock.expectSearchReferences(
					Arrays.asList( IndexedEntity.INDEX, YetAnotherIndexedEntity.INDEX ),
					b -> b
							.firstResultIndex( 3L )
							.maxResultsCount( 2L ),
					StubSearchWorkBehavior.of(
							6L,
							c -> c.collectReference( reference( IndexedEntity.INDEX, "0" ) ),
							c -> c.collectReference( reference( YetAnotherIndexedEntity.INDEX, "1" ) )
					)
			);

			SearchResult<PojoReference> result = query.execute();
			backendMock.verifyExpectationsMet();
			assertThat( result )
					.hasHits(
							new PojoReferenceImpl( IndexedEntity.class, 0 ),
							new PojoReferenceImpl( YetAnotherIndexedEntity.class, 1 )
					)
					.hasHitCount( 6 );
		}
	}

	@Test
	public void search_projection() {
		try (PojoSearchManager manager = mapping.createSearchManager()) {
			SearchQuery<List<?>> query = manager.search(
					Arrays.asList( IndexedEntity.class, YetAnotherIndexedEntity.class )
			)
					.query()
					.asProjections(
							"myTextField",
							ProjectionConstants.REFERENCE,
							"myLocalDateField",
							ProjectionConstants.DOCUMENT_REFERENCE,
							"customBridgeOnClass.text"
					)
					.predicate().all().end()
					.build();
			query.setFirstResult( 3L );
			query.setMaxResults( 2L );

			backendMock.expectSearchProjections(
					Arrays.asList( IndexedEntity.INDEX, YetAnotherIndexedEntity.INDEX ),
					b -> b
							.firstResultIndex( 3L )
							.maxResultsCount( 2L ),
					StubSearchWorkBehavior.of(
							2L,
							c -> {
								c.collectProjection( "text1" );
								c.collectReference( reference( IndexedEntity.INDEX, "0" ) );
								c.collectProjection( LocalDate.of( 2017, 11, 1 ) );
								c.collectProjection( reference( IndexedEntity.INDEX, "0" ) );
								c.collectProjection( "text2" );
							},
							c -> {
								c.collectProjection( null );
								c.collectReference( reference( YetAnotherIndexedEntity.INDEX, "1" ) );
								c.collectProjection( LocalDate.of( 2017, 11, 2 ) );
								c.collectProjection( reference( YetAnotherIndexedEntity.INDEX, "1" ) );
								c.collectProjection( null );
							}
					)
			);

			SearchResult<List<?>> result = query.execute();
			assertThat( result )
					.hasHits(
							Arrays.asList(
									"text1",
									new PojoReferenceImpl( IndexedEntity.class, 0 ),
									LocalDate.of( 2017, 11, 1 ),
									reference( IndexedEntity.INDEX, "0" ),
									"text2"
							),
							Arrays.asList(
									null,
									new PojoReferenceImpl( YetAnotherIndexedEntity.class, 1 ),
									LocalDate.of( 2017, 11, 2 ),
									reference( YetAnotherIndexedEntity.INDEX, "1" ),
									null
							)
					)
					.hasHitCount( 2L );
		}
	}

	public static class ParentIndexedEntity {

		private LocalDate localDate;

		private IndexedEntity embedded;

		@Field(name = "myLocalDateField")
		public LocalDate getLocalDate() {
			return localDate;
		}

		public void setLocalDate(LocalDate localDate) {
			this.localDate = localDate;
		}

		@MyBridge(objectName = "customBridgeOnProperty")
		public IndexedEntity getEmbedded() {
			return embedded;
		}

		public void setEmbedded(IndexedEntity embedded) {
			this.embedded = embedded;
		}

	}

	@Indexed(index = IndexedEntity.INDEX)
	@MyBridge(objectName = "customBridgeOnClass")
	public static final class IndexedEntity extends ParentIndexedEntity {

		public static final String INDEX = "IndexedEntity";

		// TODO make it work with a primitive int too
		private Integer id;

		private String text;

		@DocumentId
		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		@Field(name = "myTextField")
		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		@Override
		@IndexedEmbedded(prefix = "embedded.prefix_", maxDepth = 1,
				includePaths = "embedded.prefix_customBridgeOnClass.text")
		public IndexedEntity getEmbedded() {
			return super.getEmbedded();
		}
	}

	@Indexed(index = OtherIndexedEntity.INDEX)
	public static final class OtherIndexedEntity {

		public static final String INDEX = "OtherIndexedEntity";

		// TODO make it work with a primitive int too
		private Integer id;

		private Integer numeric;

		@DocumentId(identifierBridge = @IdentifierBridgeBeanReference(type = DefaultIntegerIdentifierBridge.class))
		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		@Field
		@Field(name = "numericAsString", functionBridge = @FunctionBridgeBeanReference(type = IntegerAsStringFunctionBridge.class))
		public Integer getNumeric() {
			return numeric;
		}

		public void setNumeric(Integer numeric) {
			this.numeric = numeric;
		}

	}

	@Indexed(index = YetAnotherIndexedEntity.INDEX)
	public static final class YetAnotherIndexedEntity extends ParentIndexedEntity {

		public static final String INDEX = "YetAnotherIndexedEntity";

		private Integer id;

		private Integer numeric;

		@DocumentId
		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		@Field
		public Integer getNumeric() {
			return numeric;
		}

		public void setNumeric(Integer numeric) {
			this.numeric = numeric;
		}
	}

	public static final class IntegerAsStringFunctionBridge implements FunctionBridge<Integer, String> {
		@Override
		public String toIndexedValue(Integer propertyValue) {
			return propertyValue == null ? null : propertyValue.toString();
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.TYPE })
	@BridgeMapping(builder = @BridgeMappingBuilderReference(type = MyBridgeBuilder.class))
	public @interface MyBridge {

		String objectName();

	}

	public static final class MyBridgeBuilder implements AnnotationBridgeBuilder<Bridge, MyBridge> {

		private String objectName;

		public MyBridgeBuilder objectName(String value) {
			this.objectName = value;
			return this;
		}

		@Override
		public void initialize(MyBridge annotation) {
			objectName( annotation.objectName() );
		}

		@Override
		public Bridge build(BuildContext buildContext) {
			return new MyBridgeImpl( objectName );
		}
	}

	private static final class MyBridgeImpl implements Bridge {

		private final String objectName;

		private PojoModelElementAccessor<IndexedEntity> sourceAccessor;
		private IndexObjectFieldAccessor objectFieldAccessor;
		private IndexFieldAccessor<String> textFieldAccessor;
		private IndexFieldAccessor<LocalDate> localDateFieldAccessor;

		MyBridgeImpl(String objectName) {
			this.objectName = objectName;
		}

		@Override
		public void bind(
				IndexSchemaElement indexSchemaElement, PojoModelElement bridgedPojoModelElement,
				SearchModel searchModel) {
			sourceAccessor = bridgedPojoModelElement.createAccessor( IndexedEntity.class );
			IndexSchemaObjectField objectField = indexSchemaElement.objectField( objectName );
			objectFieldAccessor = objectField.createAccessor();
			textFieldAccessor = objectField.field( "text" ).asString().createAccessor();
			localDateFieldAccessor = objectField.field( "date" ).asLocalDate().createAccessor();
		}

		@Override
		public void write(DocumentElement target, PojoElement source) {
			IndexedEntity sourceValue = sourceAccessor.read( source );
			if ( sourceValue != null ) {
				DocumentElement object = objectFieldAccessor.add( target );
				textFieldAccessor.write( object, sourceValue.getText() );
				localDateFieldAccessor.write( object, sourceValue.getLocalDate() );
			}
		}

	}

}
