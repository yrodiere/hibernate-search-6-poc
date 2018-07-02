/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation.impl;

import java.lang.annotation.Annotation;

import org.hibernate.search.v6poc.entity.pojo.mapping.building.spi.PojoMappingCollectorPropertyNode;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.spi.PojoMappingCollectorTypeNode;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.spi.PojoPropertyMetadataContributor;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.spi.PojoTypeMetadataContributor;
import org.hibernate.search.v6poc.entity.pojo.model.additionalmetadata.building.spi.PojoAdditionalMetadataCollectorPropertyNode;
import org.hibernate.search.v6poc.entity.pojo.model.additionalmetadata.building.spi.PojoAdditionalMetadataCollectorTypeNode;

abstract class AnnotationPojoTypeMetadataContributor<A extends Annotation>
		implements PojoTypeMetadataContributor {
	final A annotation;

	AnnotationPojoTypeMetadataContributor(A annotation) {
		this.annotation = annotation;
	}

	@Override
	public void contributeModel(PojoAdditionalMetadataCollectorTypeNode collector) {
		// TODO add a try/catch block to add some context (the annotation in particular) to thrown exceptions
		doContributeModel( collector );
	}

	@Override
	public void contributeMapping(PojoMappingCollectorTypeNode collector) {
		// TODO add a try/catch block to add some context (the annotation in particular) to thrown exceptions
		doContributeMapping( collector );
	}

	void doContributeModel(PojoAdditionalMetadataCollectorTypeNode collector) {
		// Do nothing by default
	}

	void doContributeMapping(PojoMappingCollectorTypeNode collector) {
		// Do nothing by default
	}
}
