/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.model.path.spi;

import java.util.List;
import java.util.Optional;

import org.hibernate.search.v6poc.entity.pojo.extractor.ContainerValueExtractor;
import org.hibernate.search.v6poc.entity.pojo.model.path.impl.PojoModelPathPropertyNode;
import org.hibernate.search.v6poc.entity.pojo.model.path.impl.PojoModelPathTypeNode;
import org.hibernate.search.v6poc.entity.pojo.model.path.impl.PojoModelPathValueNode;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoPropertyModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoTypeModel;

public interface PojoModelPathReverser {

	<T> Optional<T> reversePropertyAndExtractors(
			PojoModelPathValueNodeSelector<T> valueNodeSelector,
			PojoPropertyModel<?> propertyToReverse,
			List<ContainerValueExtractor<?, ?>> extractorsToReverse);

}
