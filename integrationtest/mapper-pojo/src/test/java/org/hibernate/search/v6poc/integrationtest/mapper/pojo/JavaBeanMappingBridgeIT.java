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
import java.lang.invoke.MethodHandles;
import java.util.stream.Stream;

import org.hibernate.search.v6poc.entity.pojo.bridge.declaration.MarkerMapping;
import org.hibernate.search.v6poc.entity.pojo.bridge.declaration.MarkerMappingBuilderReference;
import org.hibernate.search.v6poc.entity.pojo.bridge.declaration.PropertyBridgeAnnotationBuilderReference;
import org.hibernate.search.v6poc.entity.pojo.bridge.declaration.PropertyBridgeMapping;
import org.hibernate.search.v6poc.entity.pojo.bridge.declaration.PropertyBridgeReference;
import org.hibernate.search.v6poc.entity.pojo.bridge.declaration.TypeBridgeAnnotationBuilderReference;
import org.hibernate.search.v6poc.entity.pojo.bridge.declaration.TypeBridgeMapping;
import org.hibernate.search.v6poc.entity.pojo.bridge.declaration.TypeBridgeReference;
import org.hibernate.search.v6poc.entity.pojo.extractor.ContainerValueExtractor;
import org.hibernate.search.v6poc.entity.pojo.extractor.builtin.CollectionElementExtractor;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation.ContainerValueExtractorBeanReference;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation.DocumentId;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation.Field;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.v6poc.integrationtest.mapper.pojo.test.util.rule.JavaBeanMappingSetupHelper;
import org.hibernate.search.v6poc.util.SearchException;
import org.hibernate.search.v6poc.util.impl.integrationtest.common.FailureReportUtils;
import org.hibernate.search.v6poc.util.impl.integrationtest.common.rule.BackendMock;
import org.hibernate.search.v6poc.util.impl.test.SubTest;

import org.junit.Rule;
import org.junit.Test;

public class JavaBeanMappingBridgeIT {

	@Rule
	public BackendMock backendMock = new BackendMock( "stubBackend" );

	@Rule
	public JavaBeanMappingSetupHelper setupHelper = new JavaBeanMappingSetupHelper( MethodHandles.lookup() );

	@Test
	public void typeBridgeMapping_error_missingBridgeReference() {
		@Indexed
		@BridgeAnnotationWithEmptyTypeBridgeMapping
		class IndexedEntity {
			Integer id;
			@DocumentId
			public Integer getId() {
				return id;
			}
		}
		SubTest.expectException(
				() -> setupHelper.withBackendMock( backendMock ).setup( IndexedEntity.class )
		)
				.assertThrown()
				.isInstanceOf( SearchException.class )
				.hasMessageMatching( FailureReportUtils.buildSingleContextFailureReportPattern()
						.typeContext( IndexedEntity.class.getName() )
						.annotationContextAnyParameters( BridgeAnnotationWithEmptyTypeBridgeMapping.class )
						.failure(
								"Annotation type '" + BridgeAnnotationWithEmptyTypeBridgeMapping.class.getName()
										+ "' is annotated with '" + TypeBridgeMapping.class.getName() + "',"
										+ " but neither a bridge reference nor a bridge builder reference was provided."
						)
						.build()
				);
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@TypeBridgeMapping
	private @interface BridgeAnnotationWithEmptyTypeBridgeMapping {
	}

	@Test
	public void typeBridgeMapping_error_conflictingBridgeReferenceInBridgeMapping() {
		@Indexed
		@BridgeAnnotationWithConflictingReferencesInTypeBridgeMapping
		class IndexedEntity {
			Integer id;
			@DocumentId
			public Integer getId() {
				return id;
			}
		}
		SubTest.expectException(
				() -> setupHelper.withBackendMock( backendMock ).setup( IndexedEntity.class )
		)
				.assertThrown()
				.isInstanceOf( SearchException.class )
				.hasMessageMatching( FailureReportUtils.buildSingleContextFailureReportPattern()
						.typeContext( IndexedEntity.class.getName() )
						.annotationContextAnyParameters( BridgeAnnotationWithConflictingReferencesInTypeBridgeMapping.class )
						.failure(
								"Annotation type '" + BridgeAnnotationWithConflictingReferencesInTypeBridgeMapping.class.getName()
										+ "' is annotated with '" + TypeBridgeMapping.class.getName() + "',"
										+ " but both a bridge reference and a bridge builder reference were provided"
						)
						.build()
				);
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@TypeBridgeMapping(bridge = @TypeBridgeReference(name = "foo"), builder = @TypeBridgeAnnotationBuilderReference(name = "bar"))
	private @interface BridgeAnnotationWithConflictingReferencesInTypeBridgeMapping {
	}

	@Test
	public void propertyBridgeMapping_error_missingBridgeReference() {
		@Indexed
		class IndexedEntity {
			Integer id;
			@DocumentId
			@BridgeAnnotationWithEmptyPropertyBridgeMapping
			public Integer getId() {
				return id;
			}
		}
		SubTest.expectException(
				() -> setupHelper.withBackendMock( backendMock ).setup( IndexedEntity.class )
		)
				.assertThrown()
				.isInstanceOf( SearchException.class )
				.hasMessageMatching( FailureReportUtils.buildSingleContextFailureReportPattern()
						.typeContext( IndexedEntity.class.getName() )
						.pathContext( ".id" )
						.annotationContextAnyParameters( BridgeAnnotationWithEmptyPropertyBridgeMapping.class )
						.failure(
								"Annotation type '" + BridgeAnnotationWithEmptyPropertyBridgeMapping.class.getName()
										+ "' is annotated with '" + PropertyBridgeMapping.class.getName() + "',"
										+ " but neither a bridge reference nor a bridge builder reference was provided."
						)
						.build()
				);
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.FIELD, ElementType.METHOD})
	@PropertyBridgeMapping
	private @interface BridgeAnnotationWithEmptyPropertyBridgeMapping {
	}

	@Test
	public void propertyBridgeMapping_error_conflictingBridgeReferenceInBridgeMapping() {
		@Indexed
		class IndexedEntity {
			Integer id;
			@DocumentId
			@BridgeAnnotationWithConflictingReferencesInPropertyBridgeMapping
			public Integer getId() {
				return id;
			}
		}
		SubTest.expectException(
				() -> setupHelper.withBackendMock( backendMock ).setup( IndexedEntity.class )
		)
				.assertThrown()
				.isInstanceOf( SearchException.class )
				.hasMessageMatching( FailureReportUtils.buildSingleContextFailureReportPattern()
						.typeContext( IndexedEntity.class.getName() )
						.pathContext( ".id" )
						.annotationContextAnyParameters( BridgeAnnotationWithConflictingReferencesInPropertyBridgeMapping.class )
						.failure(
								"Annotation type '" + BridgeAnnotationWithConflictingReferencesInPropertyBridgeMapping.class.getName()
										+ "' is annotated with '" + PropertyBridgeMapping.class.getName() + "',"
										+ " but both a bridge reference and a bridge builder reference were provided"
						)
						.build()
				);
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.FIELD, ElementType.METHOD})
	@PropertyBridgeMapping(bridge = @PropertyBridgeReference(name = "foo"), builder = @PropertyBridgeAnnotationBuilderReference(name = "bar"))
	private @interface BridgeAnnotationWithConflictingReferencesInPropertyBridgeMapping {
	}

	@Test
	public void markerMapping_error_missingBuilderReference() {
		@Indexed
		class IndexedEntity {
			Integer id;
			@DocumentId
			@MarkerAnnotationWithEmptyMarkerMapping
			public Integer getId() {
				return id;
			}
		}
		SubTest.expectException(
				() -> setupHelper.withBackendMock( backendMock ).setup( IndexedEntity.class )
		)
				.assertThrown()
				.isInstanceOf( SearchException.class )
				.hasMessageMatching( FailureReportUtils.buildSingleContextFailureReportPattern()
						.typeContext( IndexedEntity.class.getName() )
						.pathContext( ".id" )
						.annotationContextAnyParameters( MarkerAnnotationWithEmptyMarkerMapping.class )
						.failure(
								"Annotation type '" + MarkerAnnotationWithEmptyMarkerMapping.class.getName()
										+ "' is annotated with '" + MarkerMapping.class.getName() + "',"
										+ " but the marker builder reference is empty"
						)
						.build()
				);
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.FIELD, ElementType.METHOD})
	@MarkerMapping(builder = @MarkerMappingBuilderReference)
	private @interface MarkerAnnotationWithEmptyMarkerMapping {
	}

	@Test
	public void containerValueExtractor_error_cannotClassTypePattern() {
		@Indexed
		class IndexedEntity {
			Integer id;
			@DocumentId
			@Field(extractors = @ContainerValueExtractorBeanReference(type = RawContainerValueExtractor.class))
			public Integer getId() {
				return id;
			}
		}
		SubTest.expectException(
				() -> setupHelper.withBackendMock( backendMock ).setup( IndexedEntity.class )
		)
				.assertThrown()
				.isInstanceOf( SearchException.class )
				.hasMessageMatching( FailureReportUtils.buildSingleContextFailureReportPattern()
						.typeContext( IndexedEntity.class.getName() )
						.pathContext( ".id" )
						.failure(
								"Cannot interpret the type arguments to the ContainerValueExtractor interface in "
										+ " implementation '" + RawContainerValueExtractor.class.getName()
										+ "'. Only the following implementations of ContainerValueExtractor are valid"
						)
						.build()
				);
	}

	private static class RawContainerValueExtractor implements ContainerValueExtractor {
		@Override
		public Stream extract(Object container) {
			throw new UnsupportedOperationException( "Should not be called" );
		}
	}

	@Test
	public void containerValueExtractor_error_invalidContainerValueExtractorForType() {
		@Indexed
		class IndexedEntity {
			Integer id;
			@DocumentId
			@Field(extractors = @ContainerValueExtractorBeanReference(type = CollectionElementExtractor.class))
			public Integer getId() {
				return id;
			}
		}
		SubTest.expectException(
				() -> setupHelper.withBackendMock( backendMock ).setup( IndexedEntity.class )
		)
				.assertThrown()
				.isInstanceOf( SearchException.class )
				.hasMessageMatching( FailureReportUtils.buildSingleContextFailureReportPattern()
						.typeContext( IndexedEntity.class.getName() )
						.pathContext( ".id" )
						.failure(
								"Cannot apply the requested container value extractor '"
										+ CollectionElementExtractor.class.getName()
										+ "' to type '" + Integer.class.getName() + "'"
						)
						.build()
				);
	}

}
