/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.model.augmented.building.spi;

import org.hibernate.search.v6poc.entity.pojo.model.path.PojoModelPathValueNode;

public interface PojoAugmentedModelCollectorValueNode extends PojoAugmentedModelCollector {

	void associationInverseSide(PojoModelPathValueNode inverseSidePath);

}
