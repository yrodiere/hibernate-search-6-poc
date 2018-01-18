/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.index.spi;

import org.hibernate.search.v6poc.backend.document.DocumentElement;
import org.hibernate.search.v6poc.backend.document.model.spi.IndexSchemaCollector;

/**
 * @author Yoann Rodiere
 */
public interface IndexManagerBuilder<D extends DocumentElement> {

	IndexSchemaCollector getSchemaCollector();

	IndexManager<D> build();

}
