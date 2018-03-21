/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.dirtiness.building.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hibernate.search.v6poc.entity.pojo.dirtiness.impl.PojoImplicitReindexingResolverPropertyNode;
import org.hibernate.search.v6poc.entity.pojo.dirtiness.impl.PojoImplicitReindexingResolverTypeNode;
import org.hibernate.search.v6poc.entity.pojo.extractor.ContainerValueExtractor;
import org.hibernate.search.v6poc.entity.pojo.extractor.impl.ContainerValueExtractorPath;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoIndexModelBinder;
import org.hibernate.search.v6poc.entity.pojo.model.path.impl.PojoModelPathTypeNode;
import org.hibernate.search.v6poc.entity.pojo.model.path.impl.PojoModelPathValueNodeSelector;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoTypeModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PropertyHandle;

class PojoImplicitReindexingResolverTypeNodeBuilder<T>
		extends AbstractPojoImplicitReindexingResolverNodeBuilder
		implements PojoModelPathValueNodeSelector<PojoImplicitReindexingResolverValueNodeBuilderDelegate<?>> {

	private final PojoModelPathTypeNode<T> modelPath;
	private final Map<PropertyHandle, PojoImplicitReindexingResolverPropertyNodeBuilder<T, ?>> propertyNodeBuilders =
			new HashMap<>();

	PojoImplicitReindexingResolverTypeNodeBuilder(PojoModelPathTypeNode<T> modelPath,
			PojoIndexModelBinder indexModelBinder) {
		super( indexModelBinder );
		this.modelPath = modelPath;
	}

	@Override
	public PojoTypeModel<?> getType() {
		return modelPath.getTypeModel();
	}

	@Override
	public PojoImplicitReindexingResolverValueNodeBuilderDelegate<?> property(PropertyHandle propertyHandle,
			ContainerValueExtractorPath extractorPath) {
		return getOrCreatePropertyBuilder( propertyHandle ).value( extractorPath );
	}

	@Override
	PojoModelPathTypeNode<T> getModelPath() {
		return modelPath;
	}

	Optional<PojoImplicitReindexingResolverTypeNode<T>> build() {
		Collection<PojoImplicitReindexingResolverPropertyNode<? super T, ?>> immutablePropertyNodes =
				propertyNodeBuilders.isEmpty() ? Collections.emptyList() : new ArrayList<>( propertyNodeBuilders.size() );
		propertyNodeBuilders.values().stream()
				.map( PojoImplicitReindexingResolverPropertyNodeBuilder::build )
				.filter( Optional::isPresent )
				.map( Optional::get )
				.forEach( immutablePropertyNodes::add );

		if ( immutablePropertyNodes.isEmpty() ) {
			/*
			 * If this resolver doesn't resolve to anything,
			 * then it is useless and we don't need to build it
			 */
			return Optional.empty();
		}
		else {
			return Optional.of( new PojoImplicitReindexingResolverTypeNode<>( immutablePropertyNodes ) );
		}
	}

	private PojoImplicitReindexingResolverPropertyNodeBuilder<T, ?> getOrCreatePropertyBuilder(PropertyHandle propertyHandle) {
		return propertyNodeBuilders.computeIfAbsent( propertyHandle, this::createPropertyBuilder );
	}

	private PojoImplicitReindexingResolverPropertyNodeBuilder<T, ?> createPropertyBuilder(PropertyHandle propertyHandle) {
		return new PojoImplicitReindexingResolverPropertyNodeBuilder<>(
				modelPath.property( propertyHandle ), indexModelBinder
		);
	}

}
