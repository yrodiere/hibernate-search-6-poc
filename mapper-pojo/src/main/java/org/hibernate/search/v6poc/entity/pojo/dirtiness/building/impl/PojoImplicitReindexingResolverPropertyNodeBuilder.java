/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.dirtiness.building.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.hibernate.search.v6poc.entity.pojo.dirtiness.impl.PojoImplicitReindexingResolver;
import org.hibernate.search.v6poc.entity.pojo.dirtiness.impl.PojoImplicitReindexingResolverPropertyNode;
import org.hibernate.search.v6poc.entity.pojo.dirtiness.impl.PojoImplicitReindexingResolverTypeNode;
import org.hibernate.search.v6poc.entity.pojo.extractor.ContainerValueExtractor;
import org.hibernate.search.v6poc.entity.pojo.extractor.impl.BoundContainerValueExtractorPath;
import org.hibernate.search.v6poc.entity.pojo.extractor.impl.ContainerValueExtractorPath;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoIndexModelBinder;
import org.hibernate.search.v6poc.entity.pojo.model.path.impl.PojoModelPathContainerElementNode;
import org.hibernate.search.v6poc.entity.pojo.model.path.impl.PojoModelPathPropertyNode;

class PojoImplicitReindexingResolverPropertyNodeBuilder<P, T> extends
		AbstractPojoImplicitReindexingResolverNodeBuilder {

	private final PojoModelPathPropertyNode<P, T> modelPath;
	private final PojoImplicitReindexingResolverValueNodeBuilderDelegate<T> valueWithoutExtractorsBuilderDelegate;
	private Map<ContainerValueExtractorPath, PojoImplicitReindexingResolverContainerElementNodeBuilder<? super T, ?>>
			containerElementNodeBuilders = new HashMap<>();

	PojoImplicitReindexingResolverPropertyNodeBuilder(PojoModelPathPropertyNode<P, T> modelPath,
			PojoIndexModelBinder indexModelBinder) {
		super( indexModelBinder );
		this.modelPath = modelPath;
		this.valueWithoutExtractorsBuilderDelegate =
				new PojoImplicitReindexingResolverValueNodeBuilderDelegate<>( modelPath, indexModelBinder );
	}

	@Override
	PojoModelPathPropertyNode<P, T> getModelPath() {
		return modelPath;
	}

	PojoImplicitReindexingResolverValueNodeBuilderDelegate<?> value(
			ContainerValueExtractorPath extractorPath) {
		if ( !extractorPath.isEmpty() ) {
			PojoImplicitReindexingResolverContainerElementNodeBuilder<? super T, ?> containerElementNodeBuilder =
					containerElementNodeBuilders.get( extractorPath );
			if ( containerElementNodeBuilder == null && !containerElementNodeBuilders.containsKey( extractorPath ) ) {
				BoundContainerValueExtractorPath<T, ?> boundExtractorPath =
						indexModelBinder.bindExtractorPath(
								modelPath.getPropertyModel().getTypeModel(), extractorPath
						);
				ContainerValueExtractorPath explicitExtractorPath = boundExtractorPath.getExtractorPath();
				if ( !explicitExtractorPath.isEmpty() ) {
					// Check whether the path was already encountered as an explicit path
					containerElementNodeBuilder = containerElementNodeBuilders.get( explicitExtractorPath );
					if ( containerElementNodeBuilder == null ) {
						containerElementNodeBuilder = createContainerBuilder( boundExtractorPath );
					}
				}
				containerElementNodeBuilders.put( explicitExtractorPath, containerElementNodeBuilder );
				containerElementNodeBuilders.put( extractorPath, containerElementNodeBuilder );
			}
			if ( containerElementNodeBuilder != null ) {
				return containerElementNodeBuilder.value();
			}
		}
		return valueWithoutExtractorsBuilderDelegate;
	}

	Optional<PojoImplicitReindexingResolverPropertyNode<P, T>> build() {
		boolean markForReindexing = valueWithoutExtractorsBuilderDelegate.isMarkForReindexing();
		Optional<? extends PojoImplicitReindexingResolverTypeNode<T>> valueWithoutExtractorTypeNode =
				valueWithoutExtractorsBuilderDelegate.buildTypeNode();
		Collection<PojoImplicitReindexingResolver<? super T>> immutableNestedNodes = new ArrayList<>();
		valueWithoutExtractorTypeNode.ifPresent( immutableNestedNodes::add );
		containerElementNodeBuilders.values().stream()
				.filter( Objects::nonNull )
				.map( PojoImplicitReindexingResolverContainerElementNodeBuilder::build )
				.filter( Optional::isPresent )
				.map( Optional::get )
				.forEach( immutableNestedNodes::add );

		if ( !markForReindexing && immutableNestedNodes.isEmpty() ) {
			/*
			 * If this resolver doesn't mark the value for reindexing and doesn't have any nested node,
			 * it is useless and we don't need to build it.
			 */
			return Optional.empty();
		}
		else {
			return Optional.of( new PojoImplicitReindexingResolverPropertyNode<>(
					modelPath.getPropertyHandle(), markForReindexing, immutableNestedNodes
			) );
		}
	}

	/*
	 * This generic method is necessary to make it clear to the compiler
	 * that the extracted type and extractor have compatible generic arguments.
	 */
	private <V> PojoImplicitReindexingResolverContainerElementNodeBuilder<? super T,V>
			createContainerBuilder(BoundContainerValueExtractorPath<T, V> boundExtractorPath) {
		ContainerValueExtractor<? super T, V> extractor = indexModelBinder
				.createExtractors( boundExtractorPath );
		PojoModelPathContainerElementNode<P, ? super T, V> containerElementPath = modelPath.containerElement(
				extractor, boundExtractorPath.getExtractedType()
		);
		return new PojoImplicitReindexingResolverContainerElementNodeBuilder<>(
				containerElementPath, indexModelBinder
		);
	}

}
