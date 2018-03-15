/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.mapping.building.impl;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;

import org.hibernate.search.v6poc.backend.document.IndexFieldAccessor;
import org.hibernate.search.v6poc.backend.document.model.IndexSchemaElement;
import org.hibernate.search.v6poc.backend.document.model.IndexSchemaFieldContext;
import org.hibernate.search.v6poc.backend.document.model.IndexSchemaFieldTypedContext;
import org.hibernate.search.v6poc.engine.spi.BuildContext;
import org.hibernate.search.v6poc.entity.mapping.building.spi.FieldModelContributor;
import org.hibernate.search.v6poc.entity.mapping.building.spi.IndexModelBindingContext;
import org.hibernate.search.v6poc.entity.mapping.building.spi.IndexSchemaContributionListener;
import org.hibernate.search.v6poc.entity.model.SearchModel;
import org.hibernate.search.v6poc.entity.pojo.bridge.PropertyBridge;
import org.hibernate.search.v6poc.entity.pojo.bridge.FunctionBridge;
import org.hibernate.search.v6poc.entity.pojo.bridge.IdentifierBridge;
import org.hibernate.search.v6poc.entity.pojo.bridge.RoutingKeyBridge;
import org.hibernate.search.v6poc.entity.pojo.bridge.TypeBridge;
import org.hibernate.search.v6poc.entity.pojo.bridge.impl.BridgeResolver;
import org.hibernate.search.v6poc.entity.pojo.bridge.mapping.BridgeBuilder;
import org.hibernate.search.v6poc.entity.pojo.extractor.ContainerValueExtractor;
import org.hibernate.search.v6poc.entity.pojo.extractor.impl.BoundContainerValueExtractor;
import org.hibernate.search.v6poc.entity.pojo.extractor.impl.ContainerValueExtractorResolver;
import org.hibernate.search.v6poc.entity.pojo.logging.impl.Log;
import org.hibernate.search.v6poc.entity.pojo.model.PojoModelElement;
import org.hibernate.search.v6poc.entity.pojo.model.PojoModelProperty;
import org.hibernate.search.v6poc.entity.pojo.model.PojoModelType;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoGenericTypeModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoBootstrapIntrospector;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoTypeModel;
import org.hibernate.search.v6poc.entity.pojo.processing.impl.PojoIndexingProcessorFunctionBridgeNode;
import org.hibernate.search.v6poc.entity.pojo.util.impl.GenericTypeContext;
import org.hibernate.search.v6poc.entity.pojo.util.impl.ReflectionUtils;
import org.hibernate.search.v6poc.util.spi.LoggerFactory;

/**
 * Binds a mapping to a given entity model and index model
 * by creating the appropriate {@link ContainerValueExtractor extractors} and bridges.
 * <p>
 * Also binds the bridges where appropriate:
 * {@link TypeBridge#bind(IndexSchemaElement, PojoModelType, SearchModel)},
 * {@link PropertyBridge#bind(IndexSchemaElement, PojoModelProperty, SearchModel)},
 * {@link FunctionBridge#bind(IndexSchemaFieldContext)}.
 * <p>
 * Incidentally, this will also generate the index model,
 * due to bridges contributing to the index model as we bind them.
 *
 * @author Yoann Rodiere
 */
public class PojoIndexModelBinder {

	private static final Log log = LoggerFactory.make( Log.class, MethodHandles.lookup() );

	private final BuildContext buildContext;
	private final PojoBootstrapIntrospector introspector;
	private final ContainerValueExtractorResolver extractorResolver;
	private final BridgeResolver bridgeResolver;

	PojoIndexModelBinder(BuildContext buildContext, PojoBootstrapIntrospector introspector,
			ContainerValueExtractorResolver extractorResolver, BridgeResolver bridgeResolver) {
		this.buildContext = buildContext;
		this.introspector = introspector;
		this.extractorResolver = extractorResolver;
		this.bridgeResolver = bridgeResolver;
	}

	public <T> Optional<BoundContainerValueExtractor<? super T, ?>> createDefaultExtractors(
			PojoGenericTypeModel<T> pojoGenericTypeModel) {
		return extractorResolver.resolveDefaultContainerValueExtractors( introspector, pojoGenericTypeModel );
	}

	public <T> BoundContainerValueExtractor<? super T, ?> createExplicitExtractors(
			PojoGenericTypeModel<T> pojoGenericTypeModel,
			List<? extends Class<? extends ContainerValueExtractor>> extractorClasses) {
		return extractorResolver.<T>resolveExplicitContainerValueExtractors(
				introspector, pojoGenericTypeModel, extractorClasses
		);
	}

	public <T> IdentifierBridge<T> createIdentifierBridge(PojoModelElement pojoModelElement, PojoTypeModel<T> typeModel,
			BridgeBuilder<? extends IdentifierBridge<?>> builder) {
		BridgeBuilder<? extends IdentifierBridge<?>> defaultedBuilder = builder;
		if ( builder == null ) {
			defaultedBuilder = bridgeResolver.resolveIdentifierBridgeForType( typeModel );
		}
		/*
		 * TODO check that the bridge is suitable for the given typeModel
		 * (use introspection, similarly to what we do to detect the function bridges field type?)
		 */
		IdentifierBridge<?> bridge = defaultedBuilder.build( buildContext );

		return (IdentifierBridge<T>) bridge;
	}

	public RoutingKeyBridge addRoutingKeyBridge(IndexModelBindingContext bindingContext,
			PojoModelElement pojoModelElement, BridgeBuilder<? extends RoutingKeyBridge> builder) {
		RoutingKeyBridge bridge = builder.build( buildContext );
		bridge.bind( pojoModelElement );

		bindingContext.explicitRouting();

		return bridge;
	}

	public Optional<TypeBridge> addTypeBridge(IndexModelBindingContext bindingContext,
			PojoModelType pojoModelType, BridgeBuilder<? extends TypeBridge> builder) {
		TypeBridge bridge = builder.build( buildContext );

		IndexSchemaContributionListenerImpl listener = new IndexSchemaContributionListenerImpl();

		bridge.bind( bindingContext.getSchemaElement( listener ), pojoModelType, bindingContext.getSearchModel() );

		// If all fields are filtered out, we should ignore the bridge
		if ( listener.schemaContributed ) {
			return Optional.of( bridge );
		}
		else {
			bridge.close();
			return Optional.empty();
		}
	}

	public Optional<PropertyBridge> addPropertyBridge(IndexModelBindingContext bindingContext,
			PojoModelProperty pojoModelProperty, BridgeBuilder<? extends PropertyBridge> builder) {
		PropertyBridge bridge = builder.build( buildContext );

		IndexSchemaContributionListenerImpl listener = new IndexSchemaContributionListenerImpl();

		bridge.bind( bindingContext.getSchemaElement( listener ), pojoModelProperty, bindingContext.getSearchModel() );

		// If all fields are filtered out, we should ignore the bridge
		if ( listener.schemaContributed ) {
			return Optional.of( bridge );
		}
		else {
			bridge.close();
			return Optional.empty();
		}
	}

	public <T> Optional<PojoIndexingProcessorFunctionBridgeNode<T, ?>> addFunctionBridge(IndexModelBindingContext bindingContext,
			PojoTypeModel<T> typeModel, BridgeBuilder<? extends FunctionBridge<?, ?>> builder,
			String fieldName, FieldModelContributor contributor) {
		BridgeBuilder<? extends FunctionBridge<?, ?>> defaultedBuilder = builder;
		if ( builder == null ) {
			defaultedBuilder = bridgeResolver.resolveFunctionBridgeForType( typeModel );
		}

		FunctionBridge<?, ?> bridge = defaultedBuilder.build( buildContext );

		GenericTypeContext bridgeTypeContext = new GenericTypeContext( bridge.getClass() );

		Class<?> bridgeParameterType = bridgeTypeContext.resolveTypeArgument( FunctionBridge.class, 0 )
				.map( ReflectionUtils::getRawType )
				.orElseThrow( () -> log.unableToInferFunctionBridgeInputType( bridge ) );
		if ( !typeModel.getSuperType( bridgeParameterType ).isPresent() ) {
			throw log.invalidInputTypeForFunctionBridge( bridge, typeModel );
		}

		@SuppressWarnings( "unchecked" ) // We checked just above that this cast is valid
		FunctionBridge<? super T, ?> typedBridge = (FunctionBridge<? super T, ?>) bridge;

		return doAddFunctionBridge( bindingContext, typedBridge, bridgeTypeContext, fieldName, contributor );
	}

	private <T, R> Optional<PojoIndexingProcessorFunctionBridgeNode<T, ?>> doAddFunctionBridge(IndexModelBindingContext bindingContext,
			FunctionBridge<? super T, R> bridge, GenericTypeContext bridgeTypeContext,
			String fieldName, FieldModelContributor contributor) {
		IndexSchemaContributionListenerImpl listener = new IndexSchemaContributionListenerImpl();

		IndexSchemaFieldContext fieldContext = bindingContext.getSchemaElement( listener ).field( fieldName );

		// First give the bridge a chance to contribute to the model
		IndexSchemaFieldTypedContext<? super R> typedFieldContext = bridge.bind( fieldContext );
		if ( typedFieldContext == null ) {
			@SuppressWarnings( "unchecked" ) // We ensure this cast is safe through reflection
			Class<? super R> returnType =
					(Class<? super R>) bridgeTypeContext.resolveTypeArgument( FunctionBridge.class, 1 )
					.map( ReflectionUtils::getRawType )
					.orElseThrow( () -> log.unableToInferFunctionBridgeIndexFieldType( bridge ) );
			typedFieldContext = fieldContext.as( returnType );
		}
		// Then give the mapping a chance to override some of the model (add storage, ...)
		contributor.contribute( typedFieldContext );

		IndexFieldAccessor<? super R> indexFieldAccessor = typedFieldContext.createAccessor();

		// If all fields are filtered out, we should ignore the bridge
		if ( listener.schemaContributed ) {
			return Optional.of( new PojoIndexingProcessorFunctionBridgeNode<>( bridge, indexFieldAccessor ) );
		}
		else {
			bridge.close();
			return Optional.empty();
		}
	}

	private class IndexSchemaContributionListenerImpl implements IndexSchemaContributionListener {
		private boolean schemaContributed = false;

		@Override
		public void onSchemaContributed() {
			schemaContributed = true;
		}
	}
}
