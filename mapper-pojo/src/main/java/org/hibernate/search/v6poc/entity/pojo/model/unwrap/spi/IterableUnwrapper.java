/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.model.unwrap.spi;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class IterableUnwrapper<T> implements Unwrapper<Iterable<T>, T> {
	@Override
	public Stream<T> unwrap(Iterable<T> wrapped) {
		return StreamSupport.stream( wrapped.spliterator(), false );
	}
}
