/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.model.tree.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.hibernate.search.v6poc.entity.pojo.extractor.impl.ContainerValueExtractorPath;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoGenericTypeModel;

/**
 * @param <H> The property holder type, i.e. the type from which the property is retrieved.
 * @param <P> The property type, i.e. the type of the property from which the value is extracted.
 * @param <V> The value type, i.e. the type of values extracted from the property.
 */
public class PojoModelTreeValueNode<H, P, V> extends PojoModelTreeNode {

	private final PojoModelTreePropertyNode<H, P> parent;
	private final ContainerValueExtractorPath extractorPath;
	private final PojoGenericTypeModel<V> extractedTypeModel;

	private PojoModelTreeTypeNode<V> extractedTypeNode;
	private Collection<PojoModelTreeTypeNode<V>> children = Collections.emptyList();

	PojoModelTreeValueNode(PojoModelTreePropertyNode<H, P> parent,
			ContainerValueExtractorPath extractorPath, PojoGenericTypeModel<V> extractedTypeModel) {
		this.parent = parent;
		this.extractorPath = extractorPath;
		this.extractedTypeModel = extractedTypeModel;
	}

	@Override
	public Optional<PojoModelTreePropertyNode<H, P>> getParentOptional() {
		return Optional.of( parent );
	}

	@Override
	public Collection<PojoModelTreeTypeNode<V>> getChildren() {
		return children;
	}

	public PojoModelTreePropertyNode<H, P> getParent() {
		return parent;
	}

	public ContainerValueExtractorPath getExtractorPath() {
		return extractorPath;
	}

	public PojoGenericTypeModel<V> getExtractedTypeModel() {
		return extractedTypeModel;
	}

	public PojoModelTreeTypeNode<V> getOrCreateChild() {
		if ( extractedTypeNode == null ) {
			extractedTypeNode = new PojoModelTreeTypeNode<>( this, extractedTypeModel );
			children = Collections.singleton( extractedTypeNode );
		}
		return extractedTypeNode;
	}

	public Optional<PojoModelTreeTypeNode<V>> getChild() {
		return Optional.ofNullable( extractedTypeNode );
	}

	@Override
	void appendSelfPath(StringBuilder builder) {
		builder.append( "[" ).append( extractorPath ).append( "]" );
	}
}
