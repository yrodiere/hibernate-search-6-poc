/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.processing.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import org.hibernate.search.v6poc.bridge.spi.FunctionBridge;
import org.hibernate.search.v6poc.bridge.spi.IdentifierBridge;
import org.hibernate.search.v6poc.engine.spi.BeanReference;
import org.hibernate.search.v6poc.entity.mapping.building.spi.FieldModelContributor;
import org.hibernate.search.v6poc.entity.mapping.building.spi.MappingIndexModelCollector;
import org.hibernate.search.v6poc.entity.mapping.building.spi.TypeMetadataContributorProvider;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.IdentifierMappingCollector;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoPropertyNodeMappingCollector;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoTypeNodeMetadataContributor;
import org.hibernate.search.v6poc.entity.pojo.model.impl.PojoIndexedTypeIdentifier;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoIntrospector;
import org.hibernate.search.v6poc.entity.pojo.model.spi.ReadableProperty;
import org.hibernate.search.v6poc.entity.processing.spi.ValueProcessor;

/**
 * @author Yoann Rodiere
 */
public class PojoPropertyNodeProcessorBuilder extends AbstractPojoProcessorBuilder
		implements PojoPropertyNodeMappingCollector {

	private final ReadableProperty property;

	private final Collection<PojoTypeNodeProcessor> indexedEmbeddedProcessors = new ArrayList<>();

	public PojoPropertyNodeProcessorBuilder(
			Class<?> propertyType, PojoIntrospector introspector,
			TypeMetadataContributorProvider<PojoTypeNodeMetadataContributor> contributorProvider,
			MappingIndexModelCollector indexModelCollector,
			IdentifierMappingCollector identifierMappingCollector,
			ReadableProperty property) {
		super( propertyType, introspector, contributorProvider, indexModelCollector,
				identifierMappingCollector);
		this.property = property;
	}

	@Override
	public void functionBridge(BeanReference<? extends FunctionBridge<?, ?>> reference,
			String fieldName, FieldModelContributor fieldModelContributor) {
		String defaultedFieldName = fieldName;
		if ( defaultedFieldName == null ) {
			defaultedFieldName = property.getName();
		}

		// TODO check that the bridge is suitable for the current node's type?
		ValueProcessor processor = indexModelCollector.addFunctionBridge(
				indexableModel, javaType, reference, defaultedFieldName, fieldModelContributor );
		processors.add( processor );
	}

	@Override
	public void identifierBridge(BeanReference<IdentifierBridge<?>> converterReference) {
		IdentifierBridge<?> bridge = indexModelCollector.createIdentifierBridge( javaType, converterReference );
		identifierBridgeCollector.collect( property, bridge );
	}

	@Override
	public void containedIn() {
		// FIXME implement ContainedIn
		// FIXME also bind containedIns to indexedEmbeddeds using the parent's metadata here, if possible?
		throw new UnsupportedOperationException( "Not implemented yet" );
	}

	@Override
	public void indexedEmbedded(String relativePrefix, int maxDepth, Set<String> pathFilters) {
		// TODO handle collections

		String defaultedRelativePrefix = relativePrefix;
		if ( defaultedRelativePrefix == null ) {
			defaultedRelativePrefix = property.getName() + ".";
		}

		PojoIndexedTypeIdentifier typeId = new PojoIndexedTypeIdentifier( javaType );

		Optional<MappingIndexModelCollector> nestedCollectorOptional = indexModelCollector.addIndexedEmbeddedIfIncluded(
				typeId, defaultedRelativePrefix, maxDepth, pathFilters );
		nestedCollectorOptional.ifPresent( nestedCollector -> {
			PojoTypeNodeProcessorBuilder nestedProcessorBuilder = new PojoTypeNodeProcessorBuilder(
					javaType, introspector, contributorProvider, nestedCollector,
					IdentifierMappingCollector.noOp() // Do NOT propagate the ID collector to IndexedEmbeddeds
					);
			contributorProvider.get( typeId ).forEach( c -> c.contributeMapping( nestedProcessorBuilder ) );
			indexedEmbeddedProcessors.add( nestedProcessorBuilder.build() );
		} );
	}

	public PojoPropertyNodeProcessor build() {
		return new PojoPropertyNodeProcessor( property, processors, indexedEmbeddedProcessors );
	}

}
