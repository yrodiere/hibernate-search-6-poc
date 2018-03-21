/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.model.tree.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.hibernate.search.v6poc.entity.pojo.extractor.impl.BoundContainerValueExtractorPath;
import org.hibernate.search.v6poc.entity.pojo.extractor.impl.ContainerValueExtractorPath;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoPropertyModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PropertyHandle;

/**
 * @param <H> The property holder type, i.e. the type from which the property is retrieved.
 * @param <P> The property type.
 */
public class PojoModelTreePropertyNode<H, P> extends PojoModelTreeNode {

	private final PojoModelTreeTypeNode<H> parent;
	private final PropertyHandle propertyHandle;
	private final PojoPropertyModel<P> propertyModel;

	private final Map<ContainerValueExtractorPath, PojoModelTreeValueNode<H, P, ?>> values = new LinkedHashMap<>();
	private final Collection<PojoModelTreeValueNode<H, P, ?>> children =
			Collections.unmodifiableCollection( values.values() );

	PojoModelTreePropertyNode(PojoModelTreeTypeNode<H> parent, PropertyHandle propertyHandle,
			PojoPropertyModel<P> propertyModel) {
		this.parent = parent;
		this.propertyHandle = propertyHandle;
		this.propertyModel = propertyModel;
	}

	@Override
	public Optional<PojoModelTreeTypeNode<H>> getParentOptional() {
		return Optional.of( parent );
	}

	@Override
	public Collection<PojoModelTreeValueNode<H, P, ?>> getChildren() {
		return children;
	}

	public PojoModelTreeTypeNode<H> getParent() {
		return parent;
	}

	public PropertyHandle getPropertyHandle() {
		return propertyHandle;
	}

	public PojoPropertyModel<P> getPropertyModel() {
		return propertyModel;
	}

	public <T> PojoModelTreeValueNode<H, P, T> getOrCreateValue(
			BoundContainerValueExtractorPath<P, T> boundExtractorPath) {
		ContainerValueExtractorPath extractorPath = boundExtractorPath.getExtractorPath();
		PojoModelTreeValueNode<H, P, ?> valueNode = values.get( extractorPath );
		if ( valueNode == null ) {
			valueNode = new PojoModelTreeValueNode<>(
					this, extractorPath, boundExtractorPath.getExtractedType()
			);
			values.put( extractorPath, valueNode );
		}
		return (PojoModelTreeValueNode<H, P, T>) valueNode;
	}

	public Optional<PojoModelTreeValueNode<H, P, ?>> getValue(ContainerValueExtractorPath extractorPath) {
		return Optional.ofNullable( values.get( extractorPath ) );
	}

	@Override
	void appendSelfPath(StringBuilder builder) {
		builder.append( "." ).append( propertyHandle.getName() );
	}
}
