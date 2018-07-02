/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation.impl;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.search.v6poc.backend.document.model.dsl.Sortable;
import org.hibernate.search.v6poc.backend.document.model.dsl.Store;
import org.hibernate.search.v6poc.engine.spi.BeanProvider;
import org.hibernate.search.v6poc.engine.spi.BeanReference;
import org.hibernate.search.v6poc.engine.spi.ImmutableBeanReference;
import org.hibernate.search.v6poc.entity.pojo.bridge.IdentifierBridge;
import org.hibernate.search.v6poc.entity.pojo.bridge.PropertyBridge;
import org.hibernate.search.v6poc.entity.pojo.bridge.RoutingKeyBridge;
import org.hibernate.search.v6poc.entity.pojo.bridge.TypeBridge;
import org.hibernate.search.v6poc.entity.pojo.bridge.ValueBridge;
import org.hibernate.search.v6poc.entity.pojo.bridge.declaration.MarkerMapping;
import org.hibernate.search.v6poc.entity.pojo.bridge.declaration.MarkerMappingBuilderReference;
import org.hibernate.search.v6poc.entity.pojo.bridge.declaration.PropertyBridgeAnnotationBuilderReference;
import org.hibernate.search.v6poc.entity.pojo.bridge.declaration.PropertyBridgeMapping;
import org.hibernate.search.v6poc.entity.pojo.bridge.declaration.PropertyBridgeReference;
import org.hibernate.search.v6poc.entity.pojo.bridge.declaration.RoutingKeyBridgeAnnotationBuilderReference;
import org.hibernate.search.v6poc.entity.pojo.bridge.declaration.RoutingKeyBridgeMapping;
import org.hibernate.search.v6poc.entity.pojo.bridge.declaration.RoutingKeyBridgeReference;
import org.hibernate.search.v6poc.entity.pojo.bridge.declaration.TypeBridgeAnnotationBuilderReference;
import org.hibernate.search.v6poc.entity.pojo.bridge.declaration.TypeBridgeMapping;
import org.hibernate.search.v6poc.entity.pojo.bridge.declaration.TypeBridgeReference;
import org.hibernate.search.v6poc.entity.pojo.bridge.impl.BeanResolverBridgeBuilder;
import org.hibernate.search.v6poc.entity.pojo.bridge.mapping.AnnotationBridgeBuilder;
import org.hibernate.search.v6poc.entity.pojo.bridge.mapping.AnnotationMarkerBuilder;
import org.hibernate.search.v6poc.entity.pojo.bridge.mapping.BridgeBuilder;
import org.hibernate.search.v6poc.entity.pojo.bridge.mapping.MarkerBuilder;
import org.hibernate.search.v6poc.entity.pojo.dirtiness.ReindexOnUpdate;
import org.hibernate.search.v6poc.entity.pojo.extractor.ContainerValueExtractorPath;
import org.hibernate.search.v6poc.entity.pojo.logging.impl.Log;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation.AssociationInverseSide;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation.ContainerValueExtractorBeanReference;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation.DocumentId;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation.Field;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation.IdentifierBridgeBeanReference;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation.IdentifierBridgeBuilderBeanReference;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation.IndexingDependency;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation.ObjectPath;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation.PropertyValue;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation.ValueBridgeBeanReference;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation.ValueBridgeBuilderBeanReference;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.programmatic.IndexingDependencyMappingContext;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.programmatic.PropertyFieldMappingContext;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.programmatic.PropertyMappingContext;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.programmatic.TypeMappingContext;
import org.hibernate.search.v6poc.entity.pojo.model.path.PojoModelPath;
import org.hibernate.search.v6poc.entity.pojo.model.path.PojoModelPathValueNode;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoPropertyModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoRawTypeModel;
import org.hibernate.search.v6poc.util.impl.common.CollectionHelper;
import org.hibernate.search.v6poc.util.impl.common.LoggerFactory;

class AnnotationProcessorProvider {

	private static final Log log = LoggerFactory.make( Log.class, MethodHandles.lookup() );

	private final BeanProvider beanProvider;

	private final List<TypeAnnotationProcessor<?>> typeAnnotationProcessors;
	private final List<PropertyAnnotationProcessor<?>> propertyAnnotationProcessors;

	AnnotationProcessorProvider(BeanProvider beanProvider) {
		this.beanProvider = beanProvider;

		this.typeAnnotationProcessors = CollectionHelper.toImmutableList( CollectionHelper.asList(
				new RoutingKeyBridgeProcessor(),
				new TypeBridgeProcessor()
		) );

		this.propertyAnnotationProcessors = CollectionHelper.toImmutableList( CollectionHelper.asList(
				new MarkerProcessor(),
				new AssociationInverseSideProcessor(),
				new IndexingDependencyProcessor(),
				new DocumentIdProcessor(),
				new PropertyBridgeProcessor(),
				new FieldProcessor(),
				new IndexedEmbeddedProcessor()
		) );
	}

	List<TypeAnnotationProcessor<?>> getTypeAnnotationProcessors() {
		return typeAnnotationProcessors;
	}

	List<PropertyAnnotationProcessor<?>> getPropertyAnnotationProcessors() {
		return propertyAnnotationProcessors;
	}

	private Optional<PojoModelPathValueNode> getPojoModelPathValueNode(ObjectPath objectPath) {
		PropertyValue[] inversePathElements = objectPath.value();
		PojoModelPathValueNode inversePath = null;
		for ( PropertyValue element : inversePathElements ) {
			String inversePropertyName = element.propertyName();
			ContainerValueExtractorPath inverseExtractorPath = getExtractorPath(
					element.extractors(), PropertyValue.DefaultExtractors.class
			);
			if ( inversePath == null ) {
				inversePath = PojoModelPath.fromRoot( inversePropertyName ).value( inverseExtractorPath );
			}
			else {
				inversePath = inversePath.property( inversePropertyName ).value( inverseExtractorPath );
			}
		}
		return Optional.ofNullable( inversePath );
	}

	private ContainerValueExtractorPath getExtractorPath(
			ContainerValueExtractorBeanReference[] extractors, Class<?> defaultExtractorsClass) {
		if ( extractors.length == 0 ) {
			return ContainerValueExtractorPath.noExtractors();
		}
		else if ( extractors.length == 1 && defaultExtractorsClass.equals( extractors[0].type() ) ) {
			return ContainerValueExtractorPath.defaultExtractors();
		}
		else {
			return ContainerValueExtractorPath.explicitExtractors(
					Arrays.stream( extractors )
							.map( ContainerValueExtractorBeanReference::type )
							.collect( Collectors.toList() )
			);
		}
	}

	private <A extends Annotation> MarkerBuilder createMarkerBuilder(A annotation) {
		MarkerMapping markerMapping = annotation.annotationType().getAnnotation( MarkerMapping.class );
		MarkerMappingBuilderReference markerBuilderReferenceAnnotation = markerMapping.builder();
		BeanReference markerBuilderReference =
				toBeanReference(
						markerBuilderReferenceAnnotation.name(),
						markerBuilderReferenceAnnotation.type(),
						MarkerMappingBuilderReference.UndefinedImplementationType.class
				)
						.orElseThrow( () -> log.missingBuilderReferenceInMarkerMapping( annotation.annotationType() ) );

		// TODO check generic parameters of builder.getClass() somehow, maybe in a similar way to what we do in PojoIndexModelBinderImpl#addValueBridge
		AnnotationMarkerBuilder<A> builder =
				beanProvider.getBean( markerBuilderReference, AnnotationMarkerBuilder.class );

		builder.initialize( annotation );

		return builder;
	}

	private BridgeBuilder<? extends IdentifierBridge<?>> createIdentifierBridgeBuilder(
			DocumentId annotation, PojoPropertyModel<?> annotationHolder) {
		IdentifierBridgeBeanReference bridgeReferenceAnnotation = annotation.identifierBridge();
		Optional<BeanReference> bridgeReference = toBeanReference(
				bridgeReferenceAnnotation.name(),
				bridgeReferenceAnnotation.type(),
				IdentifierBridgeBeanReference.UndefinedImplementationType.class
		);
		IdentifierBridgeBuilderBeanReference bridgeBuilderReferenceAnnotation = annotation.identifierBridgeBuilder();
		Optional<BeanReference> bridgeBuilderReference = toBeanReference(
				bridgeBuilderReferenceAnnotation.name(),
				bridgeBuilderReferenceAnnotation.type(),
				IdentifierBridgeBuilderBeanReference.UndefinedImplementationType.class
		);

		if ( bridgeReference.isPresent() && bridgeBuilderReference.isPresent() ) {
			throw log.invalidDocumentIdDefiningBothBridgeReferenceAndBridgeBuilderReference( annotationHolder.getName() );
		}
		else if ( bridgeReference.isPresent() ) {
			// The builder will return an object of some class T where T extends ValueBridge<?, ?>, so this is safe
			@SuppressWarnings( "unchecked" )
			BridgeBuilder<? extends IdentifierBridge<?>> castedBuilder =
					new BeanResolverBridgeBuilder( IdentifierBridge.class, bridgeReference.get() );
			return castedBuilder;
		}
		else if ( bridgeBuilderReference.isPresent() ) {
			// TODO check generic parameters of builder.getClass() somehow, maybe in a similar way to what we do in PojoIndexModelBinderImpl#addValueBridge
			return beanProvider.getBean( bridgeBuilderReference.get(), BridgeBuilder.class );
		}
		else {
			// The bridge will be auto-detected from the property type
			return null;
		}
	}

	private <A extends Annotation> BridgeBuilder<? extends RoutingKeyBridge> createRoutingKeyBridgeBuilder(A annotation) {
		RoutingKeyBridgeMapping bridgeMapping = annotation.annotationType().getAnnotation( RoutingKeyBridgeMapping.class );
		RoutingKeyBridgeReference bridgeReferenceAnnotation = bridgeMapping.bridge();
		RoutingKeyBridgeAnnotationBuilderReference bridgeBuilderReferenceAnnotation = bridgeMapping.builder();

		return createAnnotationMappedBridgeBuilder(
				RoutingKeyBridgeMapping.class, RoutingKeyBridge.class, annotation,
				toBeanReference(
						bridgeReferenceAnnotation.name(),
						bridgeReferenceAnnotation.type(),
						RoutingKeyBridgeReference.UndefinedImplementationType.class
				),
				toBeanReference(
						bridgeBuilderReferenceAnnotation.name(),
						bridgeBuilderReferenceAnnotation.type(),
						RoutingKeyBridgeAnnotationBuilderReference.UndefinedImplementationType.class
				)
		);
	}

	private <A extends Annotation> BridgeBuilder<? extends TypeBridge> createTypeBridgeBuilder(A annotation) {
		TypeBridgeMapping bridgeMapping = annotation.annotationType().getAnnotation( TypeBridgeMapping.class );
		TypeBridgeReference bridgeReferenceAnnotation = bridgeMapping.bridge();
		TypeBridgeAnnotationBuilderReference bridgeBuilderReferenceAnnotation = bridgeMapping.builder();

		return createAnnotationMappedBridgeBuilder(
				TypeBridgeMapping.class, TypeBridge.class, annotation,
				toBeanReference(
						bridgeReferenceAnnotation.name(),
						bridgeReferenceAnnotation.type(),
						TypeBridgeReference.UndefinedImplementationType.class
				),
				toBeanReference(
						bridgeBuilderReferenceAnnotation.name(),
						bridgeBuilderReferenceAnnotation.type(),
						TypeBridgeAnnotationBuilderReference.UndefinedImplementationType.class
				)
		);
	}

	private <A extends Annotation> BridgeBuilder<? extends PropertyBridge> createPropertyBridgeBuilder(A annotation) {
		PropertyBridgeMapping bridgeMapping = annotation.annotationType().getAnnotation( PropertyBridgeMapping.class );
		PropertyBridgeReference bridgeReferenceAnnotation = bridgeMapping.bridge();
		PropertyBridgeAnnotationBuilderReference bridgeBuilderReferenceAnnotation = bridgeMapping.builder();

		return createAnnotationMappedBridgeBuilder(
				PropertyBridgeMapping.class, PropertyBridge.class, annotation,
				toBeanReference(
						bridgeReferenceAnnotation.name(),
						bridgeReferenceAnnotation.type(),
						PropertyBridgeReference.UndefinedImplementationType.class
				),
				toBeanReference(
						bridgeBuilderReferenceAnnotation.name(),
						bridgeBuilderReferenceAnnotation.type(),
						PropertyBridgeAnnotationBuilderReference.UndefinedImplementationType.class
				)
		);
	}

	private BridgeBuilder<? extends ValueBridge<?, ?>> createValueBridgeBuilder(
			Field annotation, PojoPropertyModel<?> annotationHolder) {
		ValueBridgeBeanReference bridgeReferenceAnnotation = annotation.valueBridge();
		Optional<BeanReference> bridgeReference = toBeanReference(
				bridgeReferenceAnnotation.name(),
				bridgeReferenceAnnotation.type(),
				ValueBridgeBeanReference.UndefinedImplementationType.class
		);
		ValueBridgeBuilderBeanReference bridgeBuilderReferenceAnnotation = annotation.valueBridgeBuilder();
		Optional<BeanReference> bridgeBuilderReference = toBeanReference(
				bridgeBuilderReferenceAnnotation.name(),
				bridgeBuilderReferenceAnnotation.type(),
				ValueBridgeBuilderBeanReference.UndefinedImplementationType.class
		);

		if ( bridgeReference.isPresent() && bridgeBuilderReference.isPresent() ) {
			throw log.invalidFieldDefiningBothBridgeReferenceAndBridgeBuilderReference( annotationHolder.getName() );
		}
		else if ( bridgeReference.isPresent() ) {
			// The builder will return an object of some class T where T extends ValueBridge<?, ?>, so this is safe
			@SuppressWarnings( "unchecked" )
			BridgeBuilder<? extends ValueBridge<?, ?>> castedBuilder =
					new BeanResolverBridgeBuilder( ValueBridge.class, bridgeReference.get() );
			return castedBuilder;
		}
		else if ( bridgeBuilderReference.isPresent() ) {
			// TODO check generic parameters of builder.getClass() somehow, maybe in a similar way to what we do in PojoIndexModelBinderImpl#addValueBridge
			return beanProvider.getBean( bridgeBuilderReference.get(), BridgeBuilder.class );
		}
		else {
			// The bridge will be auto-detected from the property type
			return null;
		}
	}

	private <A extends Annotation, B> BridgeBuilder<B> createAnnotationMappedBridgeBuilder(
			Class<? extends Annotation> bridgeMappingAnnotation, Class<B> bridgeClass, A annotation,
			Optional<BeanReference> bridgeReferenceOptional, Optional<BeanReference> builderReferenceOptional) {
		if ( bridgeReferenceOptional.isPresent() && builderReferenceOptional.isPresent() ) {
			throw log.conflictingBridgeReferenceInBridgeMapping( bridgeMappingAnnotation, annotation.annotationType() );
		}
		else if ( bridgeReferenceOptional.isPresent() ) {
			return new BeanResolverBridgeBuilder<>( bridgeClass, bridgeReferenceOptional.get() );
		}
		else if ( builderReferenceOptional.isPresent() ) {
			AnnotationBridgeBuilder builder = beanProvider.getBean( builderReferenceOptional.get(), AnnotationBridgeBuilder.class );
			// TODO check generic parameters of builder.getClass() somehow, maybe in a similar way to what we do in PojoIndexModelBinderImpl#addValueBridge
			builder.initialize( annotation );
			return builder;
		}
		else {
			throw log.missingBridgeReferenceInBridgeMapping( bridgeMappingAnnotation, annotation.annotationType() );
		}
	}

	private Optional<BeanReference> toBeanReference(String name, Class<?> type, Class<?> undefinedTypeMarker) {
		String cleanedUpName = name.isEmpty() ? null : name;
		Class<?> cleanedUpType = undefinedTypeMarker.equals( type ) ? null : type;
		if ( cleanedUpName == null && cleanedUpType == null ) {
			return Optional.empty();
		}
		else {
			return Optional.of( new ImmutableBeanReference( cleanedUpName, cleanedUpType ) );
		}
	}

	private class MarkerProcessor extends PropertyAnnotationProcessor<Annotation> {
		@Override
		Stream<? extends Annotation> extractAnnotations(PojoPropertyModel<?> propertyModel) {
			return propertyModel.getAnnotationsByMetaAnnotationType( MarkerMapping.class );
		}

		@Override
		void doProcess(PropertyMappingContext mappingContext,
				PojoRawTypeModel<?> typeModel, PojoPropertyModel<?> propertyModel, Annotation annotation) {
			MarkerBuilder builder = createMarkerBuilder( annotation );
			mappingContext.marker( builder );
		}
	}

	private class AssociationInverseSideProcessor extends PropertyAnnotationProcessor<AssociationInverseSide> {
		@Override
		Stream<? extends AssociationInverseSide> extractAnnotations(PojoPropertyModel<?> propertyModel) {
			return propertyModel.getAnnotationsByType( AssociationInverseSide.class );
		}

		@Override
		void doProcess(PropertyMappingContext mappingContext,
				PojoRawTypeModel<?> typeModel, PojoPropertyModel<?> propertyModel,
				AssociationInverseSide annotation) {
			ContainerValueExtractorPath extractorPath = getExtractorPath(
					annotation.extractors(), AssociationInverseSide.DefaultExtractors.class
			);

			Optional<PojoModelPathValueNode> inversePathOptional = getPojoModelPathValueNode( annotation.inversePath() );
			if ( !inversePathOptional.isPresent() ) {
				throw log.missingInversePathInAssociationInverseSideMapping( typeModel, propertyModel.getName() );
			}

			mappingContext.associationInverseSide( inversePathOptional.get() )
					.withExtractors( extractorPath );
		}
	}

	private class IndexingDependencyProcessor extends PropertyAnnotationProcessor<IndexingDependency> {
		@Override
		Stream<? extends IndexingDependency> extractAnnotations(PojoPropertyModel<?> propertyModel) {
			return propertyModel.getAnnotationsByType( IndexingDependency.class );
		}

		@Override
		void doProcess(PropertyMappingContext mappingContext,
				PojoRawTypeModel<?> typeModel, PojoPropertyModel<?> propertyModel,
				IndexingDependency annotation) {
			ContainerValueExtractorPath extractorPath = getExtractorPath(
					annotation.extractors(), IndexingDependency.DefaultExtractors.class
			);

			ReindexOnUpdate reindexOnUpdate = annotation.reindexOnUpdate();

			IndexingDependencyMappingContext indexingDependencyContext = mappingContext.indexingDependency()
					.withExtractors( extractorPath );

			indexingDependencyContext.reindexOnUpdate( reindexOnUpdate );

			ObjectPath[] derivedFromAnnotations = annotation.derivedFrom();
			if ( derivedFromAnnotations.length > 0 ) {
				for ( ObjectPath objectPath : annotation.derivedFrom() ) {
					Optional<PojoModelPathValueNode> pojoModelPathOptional = getPojoModelPathValueNode( objectPath );
					if ( !pojoModelPathOptional.isPresent() ) {
						throw log.missingPathInIndexingDependencyDerivedFrom( typeModel, propertyModel.getName() );
					}
					indexingDependencyContext.derivedFrom( pojoModelPathOptional.get() );
				}
			}
		}
	}

	private class DocumentIdProcessor extends PropertyAnnotationProcessor<DocumentId> {
		@Override
		Stream<? extends DocumentId> extractAnnotations(PojoPropertyModel<?> propertyModel) {
			return propertyModel.getAnnotationsByType( DocumentId.class );
		}

		@Override
		void doProcess(PropertyMappingContext mappingContext,
				PojoRawTypeModel<?> typeModel, PojoPropertyModel<?> propertyModel,
				DocumentId annotation) {
			BridgeBuilder<? extends IdentifierBridge<?>> builder =
					createIdentifierBridgeBuilder( annotation, propertyModel );

			mappingContext.documentId().identifierBridge( builder );
		}
	}

	private class RoutingKeyBridgeProcessor extends TypeAnnotationProcessor<Annotation> {
		@Override
		Stream<? extends Annotation> extractAnnotations(PojoRawTypeModel<?> typeModel) {
			return typeModel.getAnnotationsByMetaAnnotationType( RoutingKeyBridgeMapping.class );
		}

		@Override
		void doProcess(TypeMappingContext mappingContext, PojoRawTypeModel<?> typeModel, Annotation annotation) {
			BridgeBuilder<? extends RoutingKeyBridge> builder = createRoutingKeyBridgeBuilder( annotation );
			mappingContext.routingKeyBridge( builder );
		}
	}

	private class TypeBridgeProcessor extends TypeAnnotationProcessor<Annotation> {
		@Override
		Stream<? extends Annotation> extractAnnotations(PojoRawTypeModel<?> typeModel) {
			return typeModel.getAnnotationsByMetaAnnotationType( TypeBridgeMapping.class );
		}

		@Override
		void doProcess(TypeMappingContext mappingContext, PojoRawTypeModel<?> typeModel, Annotation annotation) {
			BridgeBuilder<? extends TypeBridge> builder = createTypeBridgeBuilder( annotation );
			mappingContext.bridge( builder );
		}
	}

	private class PropertyBridgeProcessor extends PropertyAnnotationProcessor<Annotation> {
		@Override
		Stream<? extends Annotation> extractAnnotations(PojoPropertyModel<?> propertyModel) {
			return propertyModel.getAnnotationsByMetaAnnotationType( PropertyBridgeMapping.class );
		}

		@Override
		void doProcess(PropertyMappingContext mappingContext,
				PojoRawTypeModel<?> typeModel, PojoPropertyModel<?> propertyModel,
				Annotation annotation) {
			BridgeBuilder<? extends PropertyBridge> builder = createPropertyBridgeBuilder( annotation );
			mappingContext.bridge( builder );
		}
	}

	private class FieldProcessor extends PropertyAnnotationProcessor<Field> {
		@Override
		Stream<? extends Field> extractAnnotations(PojoPropertyModel<?> propertyModel) {
			return propertyModel.getAnnotationsByType( Field.class );
		}

		@Override
		void doProcess(PropertyMappingContext mappingContext,
				PojoRawTypeModel<?> typeModel, PojoPropertyModel<?> propertyModel,
				Field annotation) {
			String cleanedUpRelativeFieldName = annotation.name();
			if ( cleanedUpRelativeFieldName.isEmpty() ) {
				cleanedUpRelativeFieldName = null;
			}

			BridgeBuilder<? extends ValueBridge<?, ?>> builder = createValueBridgeBuilder( annotation, propertyModel );

			ContainerValueExtractorPath extractorPath =
					getExtractorPath( annotation.extractors(), Field.DefaultExtractors.class );

			PropertyFieldMappingContext fieldContext = mappingContext.field( cleanedUpRelativeFieldName )
					.withExtractors( extractorPath )
					.valueBridge( builder );

			if ( !Store.DEFAULT.equals( annotation.store() ) ) {
				fieldContext.store( annotation.store() );
			}
			if ( !Sortable.DEFAULT.equals( annotation.sortable() ) ) {
				fieldContext.sortable( annotation.sortable() );
			}
			if ( !annotation.analyzer().isEmpty() ) {
				fieldContext.analyzer( annotation.analyzer() );
			}
			if ( !annotation.normalizer().isEmpty() ) {
				fieldContext.normalizer( annotation.normalizer() );
			}
		}
	}

	private class IndexedEmbeddedProcessor extends PropertyAnnotationProcessor<IndexedEmbedded> {
		@Override
		Stream<? extends IndexedEmbedded> extractAnnotations(PojoPropertyModel<?> propertyModel) {
			return propertyModel.getAnnotationsByType( IndexedEmbedded.class );
		}

		@Override
		void doProcess(PropertyMappingContext mappingContext,
				PojoRawTypeModel<?> typeModel, PojoPropertyModel<?> propertyModel,
				IndexedEmbedded annotation) {
			String cleanedUpPrefix = annotation.prefix();
			if ( cleanedUpPrefix.isEmpty() ) {
				cleanedUpPrefix = null;
			}

			Integer cleanedUpMaxDepth = annotation.maxDepth();
			if ( cleanedUpMaxDepth.equals( -1 ) ) {
				cleanedUpMaxDepth = null;
			}

			String[] includePathsArray = annotation.includePaths();
			Set<String> cleanedUpIncludePaths;
			if ( includePathsArray.length > 0 ) {
				cleanedUpIncludePaths = new HashSet<>();
				Collections.addAll( cleanedUpIncludePaths, includePathsArray );
			}
			else {
				cleanedUpIncludePaths = Collections.emptySet();
			}

			ContainerValueExtractorPath extractorPath =
					getExtractorPath( annotation.extractors(), IndexedEmbedded.DefaultExtractors.class );

			mappingContext.indexedEmbedded()
					.withExtractors( extractorPath )
					.prefix( cleanedUpPrefix )
					.storage( annotation.storage() )
					.maxDepth( cleanedUpMaxDepth )
					.includePaths( cleanedUpIncludePaths );
		}
	}
}
