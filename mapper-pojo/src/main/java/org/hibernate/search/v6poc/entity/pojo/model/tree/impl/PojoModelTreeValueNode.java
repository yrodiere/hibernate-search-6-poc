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
	private final PojoGenericTypeModel<V> extractedType;

	private PojoModelTreeTypeNode<V> extractedTypeNode;
	private Collection<PojoModelTreeTypeNode<V>> children = Collections.emptyList();

	PojoModelTreeValueNode(PojoModelTreePropertyNode<H, P> parent,
			ContainerValueExtractorPath extractorPath, PojoGenericTypeModel<V> extractedType) {
		this.parent = parent;
		this.extractorPath = extractorPath;
		this.extractedType = extractedType;
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

	public PojoModelTreeTypeNode<V> getExtractedType() {
		if ( extractedTypeNode == null ) {
			extractedTypeNode = new PojoModelTreeTypeNode<>( this, extractedType );
			children = Collections.singleton( extractedTypeNode );
		}
		return extractedTypeNode;
	}

	@Override
	void appendSelfPath(StringBuilder builder) {
		builder.append( "[" ).append( extractorPath ).append( "]" );
	}
}
