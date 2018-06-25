/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.document.model.dsl;

import java.time.LocalDate;

import org.hibernate.search.v6poc.backend.document.IndexFieldAccessor;
import org.hibernate.search.v6poc.backend.document.model.dsl.spi.FieldModelExtension;
import org.hibernate.search.v6poc.spatial.GeoPoint;


/**
 * @author Yoann Rodiere
 */
public interface IndexSchemaFieldContext {

	<T> IndexSchemaFieldTypedContext<? extends IndexFieldAccessor<T>> as(Class<T> inputType);

	IndexSchemaFieldTypedContext<? extends IndexFieldAccessor<String>> asString();

	IndexSchemaFieldTypedContext<? extends IndexFieldAccessor<Integer>> asInteger();

	IndexSchemaFieldTypedContext<? extends IndexFieldAccessor<LocalDate>> asLocalDate();

	IndexSchemaFieldTypedContext<? extends IndexFieldAccessor<GeoPoint>> asGeoPoint();

	// TODO NumericBridgeProvider
	// TODO JavaTimeBridgeProvider
	// TODO BasicJDKTypesBridgeProvider

	default <T> T withExtension(FieldModelExtension<T> extension) {
		return extension.extendOrFail( this );
	}

}
