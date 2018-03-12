/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.extractor.impl;

import org.hibernate.search.v6poc.entity.pojo.extractor.ContainerValueExtractor;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoGenericTypeModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoTypeModel;

/**
 * A {@link ContainerValueExtractor} bound to a given type.
 *
 * @param <C> The container type
 * @param <T> The extracted value type
 */
public class BoundContainerValueExtractor<C, T> {
	private final ContainerValueExtractor<C, T> extractor;
	private final PojoGenericTypeModel<T> extractedType;

	BoundContainerValueExtractor(ContainerValueExtractor<C, T> extractor, PojoGenericTypeModel<T> extractedType) {
		this.extractor = extractor;
		this.extractedType = extractedType;
	}

	public ContainerValueExtractor<C, T> getExtractor() {
		return extractor;
	}

	public PojoGenericTypeModel<T> getExtractedType() {
		return extractedType;
	}
}
