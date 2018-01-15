/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.model.unwrap.spi;

import org.hibernate.search.v6poc.engine.spi.BuildContext;

public interface UnwrapperFactory {

	boolean supports(Class<?> inputType);

	<W, T> Unwrapper<W, T> create(BuildContext buildContext, Class<W> inputType, Class<T> outputType);

}
