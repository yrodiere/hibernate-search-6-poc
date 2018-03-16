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
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoTypeModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PropertyHandle;

public interface PojoModelPathValueNodeSelector<T> {

	PojoTypeModel<?> getType();

	T property(PropertyHandle propertyHandle);

	Optional<T> property(PropertyHandle propertyHandle, Class<? extends ContainerValueExtractor> extractorClass);

	Optional<T> property(PropertyHandle propertyHandle,
			List<? extends Class<? extends ContainerValueExtractor>> extractorClasses);

}
