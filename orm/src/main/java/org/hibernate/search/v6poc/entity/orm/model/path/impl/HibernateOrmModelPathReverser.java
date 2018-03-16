/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.orm.model.path.impl;

import java.util.List;
import java.util.Optional;

import org.hibernate.search.v6poc.entity.orm.model.impl.AbstractHibernateOrmTypeModel;
import org.hibernate.search.v6poc.entity.orm.model.impl.HibernateOrmPropertyModel;
import org.hibernate.search.v6poc.entity.pojo.extractor.ContainerValueExtractor;
import org.hibernate.search.v6poc.entity.pojo.extractor.builtin.ArrayElementExtractor;
import org.hibernate.search.v6poc.entity.pojo.extractor.builtin.CollectionElementExtractor;
import org.hibernate.search.v6poc.entity.pojo.extractor.builtin.MapValueExtractor;
import org.hibernate.search.v6poc.entity.pojo.model.path.spi.PojoModelPathReverser;
import org.hibernate.search.v6poc.entity.pojo.model.path.spi.PojoModelPathValueNodeSelector;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoPropertyModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoTypeModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PropertyHandle;

public class HibernateOrmModelPathReverser implements PojoModelPathReverser {

	@Override
	public <T> Optional<T> reversePropertyAndExtractors(PojoModelPathValueNodeSelector<T> valueNodeSelector,
			PojoPropertyModel<?> propertyToReverse, List<ContainerValueExtractor<?, ?>> extractorsToReverse) {
		if ( extractorsToReverse.size() > 1 ) {
			/*
			 * Hibernate ORM associations can only use one level of container nesting, at most.
			 * For instance, a Map<?, List<?>> cannot host a bidirectional association
			 * (it could host a unidirectional association on the map keys,
			 * but we don't care about unidirectional associations).
			 */
			return Optional.empty();
		}
		else if ( !extractorsToReverse.isEmpty() ) {
			ContainerValueExtractor<?, ?> extractor = extractorsToReverse.iterator().next();
			if ( !( extractor instanceof MapValueExtractor || extractor instanceof CollectionElementExtractor
					|| extractor instanceof ArrayElementExtractor ) ) {
				/*
				 * Hibernate ORM associations can only be bidirectional if associated elements
				 * are stored in collection elements, array elements or map values.
				 * In particular, keys of a map can host an association (using @MapKeyJoinColumn),
				 * but it's not possible to declare an reverse association mapped by this "key association".
				 */
				return Optional.empty();
			}
		}

		Optional<? extends PojoPropertyModel<?>> reversePropertyModelOptional =
				getPropertyReverseOf( propertyToReverse, valueNodeSelector.getType() );
		if ( !reversePropertyModelOptional.isPresent() ) {
			// Some associations are unidirectional, there's nothing we can do
			return Optional.empty();
		}

		PropertyHandle reversePropertyHandle = reversePropertyModelOptional.get().getHandle();

		/*
		 * Use the default extractor for associations. We know it's one side of a bidirectional associations,
		 * so there's not much choice really:
		 *  - if it's a map, we want the values (keys cannot be part of a bidirectional association)
		 *  - if it's a collection, we want the elements
		 *  - if it's an array, we want the elements
		 *  - otherwise, we want the value itself
		 */

		Optional<T> reverseWithExtractor =
				valueNodeSelector.property( reversePropertyHandle, MapValueExtractor.class );
		if ( reverseWithExtractor.isPresent() ) {
			return reverseWithExtractor;
		}

		reverseWithExtractor = valueNodeSelector.property( reversePropertyHandle, CollectionElementExtractor.class );
		if ( reverseWithExtractor.isPresent() ) {
			return reverseWithExtractor;
		}

		reverseWithExtractor = valueNodeSelector.property( reversePropertyHandle, ArrayElementExtractor.class );
		if ( reverseWithExtractor.isPresent() ) {
			return reverseWithExtractor;
		}

		return Optional.of( valueNodeSelector.property( reversePropertyHandle ) );
	}

	private Optional<? extends PojoPropertyModel<?>> getPropertyReverseOf(
			PojoPropertyModel<?> propertyToReverse, PojoTypeModel<?> reverseHolderType) {
		HibernateOrmPropertyModel<?> propertyModel =
				(HibernateOrmPropertyModel<?>) propertyToReverse.getRawProperty();

		AbstractHibernateOrmTypeModel<?> reverseTypeModel = (AbstractHibernateOrmTypeModel<?>) reverseHolderType.getRawType();

		String mappedBy = propertyModel.getMappedByOrNull();
		if ( mappedBy != null ) {
			return Optional.of( reverseTypeModel.getProperty( mappedBy ) );
		}
		else {
			String reverseMappedBy = propertyModel.getName();
			return reverseTypeModel.getAscendingSuperTypes()
					.flatMap( AbstractHibernateOrmTypeModel::getDeclaredProperties )
					.filter( property -> reverseMappedBy.equals( property.getMappedByOrNull() ) )
					.findFirst();
		}
	}

}
