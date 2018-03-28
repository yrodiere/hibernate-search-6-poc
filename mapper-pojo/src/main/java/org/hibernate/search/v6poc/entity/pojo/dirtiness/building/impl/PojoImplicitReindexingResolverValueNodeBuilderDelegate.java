/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.dirtiness.building.impl;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.hibernate.search.v6poc.entity.pojo.dirtiness.impl.PojoImplicitReindexingResolver;
import org.hibernate.search.v6poc.entity.pojo.logging.impl.Log;
import org.hibernate.search.v6poc.entity.pojo.model.path.impl.BoundPojoModelPathValueNode;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoRawTypeModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoTypeModel;
import org.hibernate.search.v6poc.util.AssertionFailure;
import org.hibernate.search.v6poc.util.impl.common.LoggerFactory;

class PojoImplicitReindexingResolverValueNodeBuilderDelegate<V> {

	private static final Log log = LoggerFactory.make( Log.class, MethodHandles.lookup() );

	private final BoundPojoModelPathValueNode<?, ?, V> modelPath;
	private final PojoImplicitReindexingResolverBuildingHelper buildingHelper;

	private PojoImplicitReindexingResolverOriginalTypeNodeBuilder<V> typeNodeBuilder;
	private final Map<PojoRawTypeModel<?>, PojoImplicitReindexingResolverCastedTypeNodeBuilder<V, ?>>
			conditionalTypeNodeBuilders = new HashMap<>();

	PojoImplicitReindexingResolverValueNodeBuilderDelegate(BoundPojoModelPathValueNode<?, ?, V> modelPath,
			PojoImplicitReindexingResolverBuildingHelper buildingHelper) {
		this.modelPath = modelPath;
		this.buildingHelper = buildingHelper;
	}

	PojoTypeModel<V> getTypeModel() {
		return modelPath.type().getTypeModel();
	}

	<U> AbstractPojoImplicitReindexingResolverTypeNodeBuilder<V, ?> type(PojoRawTypeModel<U> targetTypeModel) {
		PojoRawTypeModel<? super V> valueRawTypeModel = getTypeModel().getRawType();
		if ( valueRawTypeModel.isSubTypeOf( targetTypeModel ) ) {
			// No need to cast, we're already satisfying the requirements
			return type();
		}
		else if ( targetTypeModel.isSubTypeOf( valueRawTypeModel ) ) {
			// Need to downcast
			return getOrCreateConditionalTypeBuilder( targetTypeModel );
		}
		else {
			/*
			 * Types are incompatible; this problem should have already been detected and reported
			 * by the caller, so we just throw an assertion failure here.
			 */
			throw new AssertionFailure(
					"Error while building the automatic reindexing resolver at path " + modelPath
					+ ": attempt to convert a reindexing resolver builder to an incorrect type; "
					+ " got " + targetTypeModel + ", but a subtype of " + valueRawTypeModel
					+ " was expected."
					+ " This is very probably a bug in Hibernate Search, please report it."
			);
		}
	}

	Collection<PojoImplicitReindexingResolver<V>> buildTypeNodes() {
		Collection<PojoImplicitReindexingResolver<V>> immutableTypeNodes = new ArrayList<>();
		if ( typeNodeBuilder != null ) {
			typeNodeBuilder.build().ifPresent( immutableTypeNodes::add );
		}
		conditionalTypeNodeBuilders.values().stream()
				.map( PojoImplicitReindexingResolverCastedTypeNodeBuilder::build )
				.filter( Optional::isPresent )
				.map( Optional::get )
				.forEach( immutableTypeNodes::add );

		return immutableTypeNodes;
	}

	private PojoImplicitReindexingResolverOriginalTypeNodeBuilder<V> type() {
		if ( typeNodeBuilder == null ) {
			typeNodeBuilder = new PojoImplicitReindexingResolverOriginalTypeNodeBuilder<>( modelPath.type(), buildingHelper );
		}
		return typeNodeBuilder;
	}

	@SuppressWarnings("unchecked") // We know builders have this exact type, by construction
	private <U> PojoImplicitReindexingResolverCastedTypeNodeBuilder<V, U> getOrCreateConditionalTypeBuilder(
			PojoRawTypeModel<U> targetTypeModel) {
		return (PojoImplicitReindexingResolverCastedTypeNodeBuilder<V, U>)
				conditionalTypeNodeBuilders.computeIfAbsent( targetTypeModel, this::createConditionalTypeBuilder );
	}

	private <U> PojoImplicitReindexingResolverCastedTypeNodeBuilder<V, U> createConditionalTypeBuilder(
			PojoRawTypeModel<U> targetTypeModel) {
		return new PojoImplicitReindexingResolverCastedTypeNodeBuilder<>(
				modelPath.castedType( targetTypeModel ), buildingHelper
		);
	}

}
