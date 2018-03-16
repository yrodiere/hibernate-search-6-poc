/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.extractor.impl;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hibernate.search.v6poc.engine.spi.BeanResolver;
import org.hibernate.search.v6poc.engine.spi.BuildContext;
import org.hibernate.search.v6poc.entity.pojo.extractor.ContainerValueExtractor;
import org.hibernate.search.v6poc.entity.pojo.extractor.builtin.ArrayElementExtractor;
import org.hibernate.search.v6poc.entity.pojo.extractor.builtin.CollectionElementExtractor;
import org.hibernate.search.v6poc.entity.pojo.extractor.builtin.IterableElementExtractor;
import org.hibernate.search.v6poc.entity.pojo.extractor.builtin.MapValueExtractor;
import org.hibernate.search.v6poc.entity.pojo.extractor.builtin.OptionalDoubleValueExtractor;
import org.hibernate.search.v6poc.entity.pojo.extractor.builtin.OptionalIntValueExtractor;
import org.hibernate.search.v6poc.entity.pojo.extractor.builtin.OptionalLongValueExtractor;
import org.hibernate.search.v6poc.entity.pojo.extractor.builtin.OptionalValueExtractor;
import org.hibernate.search.v6poc.entity.pojo.logging.impl.Log;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoGenericTypeModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoBootstrapIntrospector;
import org.hibernate.search.v6poc.entity.pojo.model.typepattern.impl.TypePatternMatcher;
import org.hibernate.search.v6poc.entity.pojo.model.typepattern.impl.TypePatternMatcherFactory;
import org.hibernate.search.v6poc.entity.pojo.util.impl.GenericTypeContext;
import org.hibernate.search.v6poc.util.AssertionFailure;
import org.hibernate.search.v6poc.util.spi.LoggerFactory;

public class ContainerValueExtractorBinder {

	private static final Log log = LoggerFactory.make( Log.class, MethodHandles.lookup() );

	// TODO add an extension point to override the builtin extractors, or at least to add defaults for other types

	private final BeanResolver beanResolver;
	private final TypePatternMatcherFactory typePatternMatcherFactory = new TypePatternMatcherFactory();
	private final FirstMatchingExtractorContributor firstMatchingExtractorContributor =
			new FirstMatchingExtractorContributor();
	@SuppressWarnings("rawtypes") // Checks are implemented using reflection
	private Map<Class<? extends ContainerValueExtractor>, ExtractorContributor> extractorContributorCache =
			new HashMap<>();

	public ContainerValueExtractorBinder(BuildContext buildContext) {
		this.beanResolver = buildContext.getServiceManager().getBeanResolver();
		addDefaultExtractor( MapValueExtractor.class );
		addDefaultExtractor( CollectionElementExtractor.class );
		addDefaultExtractor( IterableElementExtractor.class );
		addDefaultExtractor( OptionalValueExtractor.class );
		addDefaultExtractor( OptionalIntValueExtractor.class );
		addDefaultExtractor( OptionalLongValueExtractor.class );
		addDefaultExtractor( OptionalDoubleValueExtractor.class );
		addDefaultExtractor( ArrayElementExtractor.class );
	}

	@SuppressWarnings("unchecked") // Checks are implemented using reflection
	public <C> Optional<BoundContainerValueExtractor<? super C, ?>> tryBind(
			PojoBootstrapIntrospector introspector, PojoGenericTypeModel<C> sourceType,
			ContainerValueExtractorPath extractorPath) {
		ExtractorResolutionState<C> state = new ExtractorResolutionState<>( introspector, sourceType );
		boolean bound = false;

		if ( extractorPath.isDefault() ) {
			bound = firstMatchingExtractorContributor.tryAppend( state );
		}
		else {
			for ( Class<? extends ContainerValueExtractor> extractorClass
					: extractorPath.getExplicitExtractorClasses() ) {
				ExtractorContributor extractorContributor = getExtractorContributorForClass( extractorClass );
				if ( extractorContributor.tryAppend( state ) ) {
					bound = true;
				}
				else {
					/*
					 * Assume failure, even if a previous extractor was applied successfully:
					 * we want either every extractor to be applied, or none.
					 */
					bound = false;
					break;
				}
			}
		}

		if ( bound ) {
			return Optional.of( state.build() );
		}
		else {
			return Optional.empty();
		}
	}

	@SuppressWarnings("unchecked") // Checks are implemented using reflection
	public <C> BoundContainerValueExtractor<? super C, ?> bind(
			PojoBootstrapIntrospector introspector, PojoGenericTypeModel<C> sourceType,
			ContainerValueExtractorPath extractorPath) {
		ExtractorResolutionState<C> state = new ExtractorResolutionState<>( introspector, sourceType );
		if ( extractorPath.isDefault() ) {
			if ( !firstMatchingExtractorContributor.tryAppend( state ) ) {
				throw log.couldNotFindMatchingDefaultContainerValueExtractorForType( state.extractedType );
			}
		}
		else if ( extractorPath.getExplicitExtractorClasses().isEmpty() ) {
			throw new AssertionFailure(
					"Received a request to apply extractors, but the extractor class list was empty."
					+ " There is probably a bug in Hibernate Search."
			);
		}
		else {
			for ( Class<? extends ContainerValueExtractor> extractorClass
					: extractorPath.getExplicitExtractorClasses() ) {
				ExtractorContributor extractorContributor = getExtractorContributorForClass( extractorClass );
				if ( !extractorContributor.tryAppend( state ) ) {
					throw log.invalidContainerValueExtractorForType( extractorClass, state.extractedType );
				}
			}
		}
		return state.build();
	}

	@SuppressWarnings( "rawtypes" ) // Checks are implemented using reflection
	private void addDefaultExtractor(Class<? extends ContainerValueExtractor> extractorClass) {
		ExtractorContributor extractorContributor = getExtractorContributorForClass( extractorClass );
		firstMatchingExtractorContributor.addCandidate( extractorContributor );
	}

	@SuppressWarnings( "rawtypes" ) // Checks are implemented using reflection
	private ExtractorContributor getExtractorContributorForClass(
			Class<? extends ContainerValueExtractor> extractorClass) {
		return extractorContributorCache.computeIfAbsent( extractorClass, this::createExtractorContributorForClass );
	}

	@SuppressWarnings( "rawtypes" ) // Checks are implemented using reflection
	private ExtractorContributor createExtractorContributorForClass(
			Class<? extends ContainerValueExtractor> extractorClass) {
		GenericTypeContext typeContext = new GenericTypeContext( extractorClass );
		Type typeToMatch = typeContext.resolveTypeArgument( ContainerValueExtractor.class, 0 ).get();
		Type resultType = typeContext.resolveTypeArgument( ContainerValueExtractor.class, 1 ).get();
		TypePatternMatcher typePatternMatcher;
		try {
			typePatternMatcher = typePatternMatcherFactory.create( typeToMatch, resultType );
		}
		catch (UnsupportedOperationException e) {
			throw log.couldNotInferContainerValueExtractorClassTypePattern( extractorClass );
		}
		return new SingleExtractorContributor( typePatternMatcher, extractorClass );
	}

	private interface ExtractorContributor {

		/**
		 * @param state The state to append an extractor to
		 * @return {@code true} if the current type was accepted by this contributor and an extractor was added,
		 * {@code false} if the type was rejected and no extractor was added.
		 */
		boolean tryAppend(ExtractorResolutionState<?> state);

	}

	@SuppressWarnings( "rawtypes" ) // Checks are implemented using reflection
	private class SingleExtractorContributor implements ExtractorContributor {
		private final TypePatternMatcher typePatternMatcher;
		private final Class<? extends ContainerValueExtractor> extractorClass;

		SingleExtractorContributor(TypePatternMatcher typePatternMatcher,
				Class<? extends ContainerValueExtractor> extractorClass) {
			this.typePatternMatcher = typePatternMatcher;
			this.extractorClass = extractorClass;
		}

		@Override
		public boolean tryAppend(ExtractorResolutionState<?> state) {
			Optional<? extends PojoGenericTypeModel<?>> resultTypeOptional =
					typePatternMatcher.match( state.introspector, state.extractedType );
			if ( resultTypeOptional.isPresent() ) {
				ContainerValueExtractor<?, ?> extractor =
						beanResolver.resolve( extractorClass, ContainerValueExtractor.class );
				state.append( extractor, resultTypeOptional.get() );
				return true;
			}
			else {
				return false;
			}
		}
	}

	private static class FirstMatchingExtractorContributor implements ExtractorContributor {
		private final List<ExtractorContributor> candidates = new ArrayList<>();

		void addCandidate(ExtractorContributor contributor) {
			candidates.add( contributor );
		}

		@Override
		public boolean tryAppend(ExtractorResolutionState<?> state) {
			for ( ExtractorContributor extractorContributor : candidates ) {
				if ( extractorContributor.tryAppend( state ) ) {
					// Recurse as much as possible
					tryAppend( state );
					return true;
				}
			}
			return false;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" }) // Checks are implemented using reflection
	private static class ExtractorResolutionState<C> {

		private final PojoBootstrapIntrospector introspector;
		private ContainerValueExtractor<? super C, ?> extractor;
		private PojoGenericTypeModel<?> extractedType;

		ExtractorResolutionState(PojoBootstrapIntrospector introspector, PojoGenericTypeModel<?> extractedType) {
			this.introspector = introspector;
			this.extractedType = extractedType;
		}

		void append(ContainerValueExtractor<?, ?> extractor, PojoGenericTypeModel<?> extractedType) {
			this.extractedType = extractedType;
			if ( this.extractor == null ) {
				// Initial calls: T == ? super C
				this.extractor = (ContainerValueExtractor) extractor;
			}
			else {
				this.extractor = new ChainingContainerValueExtractor( this.extractor, extractor );
			}
		}

		BoundContainerValueExtractor<? super C, ?> build() {
			return new BoundContainerValueExtractor( extractor, extractedType );
		}

	}
}
