/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.document.spi;

import java.lang.invoke.MethodHandles;

import org.hibernate.search.v6poc.backend.document.IndexFieldAccessor;
import org.hibernate.search.v6poc.backend.document.converter.FromIndexFieldValueConverter;
import org.hibernate.search.v6poc.backend.document.converter.ToIndexFieldValueConverter;
import org.hibernate.search.v6poc.backend.document.converter.spi.PassThroughToIndexFieldValueConverter;
import org.hibernate.search.v6poc.backend.document.model.dsl.spi.IndexSchemaContext;
import org.hibernate.search.v6poc.logging.impl.Log;
import org.hibernate.search.v6poc.util.impl.common.Contracts;
import org.hibernate.search.v6poc.util.impl.common.LoggerFactory;

/**
 * A helper for backends, making it easier to return accessors before they are completely defined,
 * and providing a helper to convert values when executing search queries.
 */
public final class IndexSchemaFieldDefinitionHelper<F> {

	private static final Log log = LoggerFactory.make( Log.class, MethodHandles.lookup() );

	private final IndexSchemaContext schemaContext;

	private final DeferredInitializationIndexFieldAccessor<F> rawAccessor =
			new DeferredInitializationIndexFieldAccessor<>();

	private ToIndexFieldValueConverter<?, ? extends F> toIndexConverter;
	private FromIndexFieldValueConverter<? super F, ?> fromIndexConverter;

	private boolean accessorCreated = false;

	public IndexSchemaFieldDefinitionHelper(IndexSchemaContext schemaContext,
			Class<F> indexFieldType) {
		this( schemaContext, new PassThroughToIndexFieldValueConverter<>( indexFieldType ) );
	}

	public IndexSchemaFieldDefinitionHelper(IndexSchemaContext schemaContext,
			ToIndexFieldValueConverter<F, ? extends F> identityToIndexConverter) {
		this.schemaContext = schemaContext;
		this.toIndexConverter = identityToIndexConverter;
		this.fromIndexConverter = null;
	}

	public IndexSchemaContext getSchemaContext() {
		return schemaContext;
	}

	/**
	 * @return A (potentially un-{@link #initialize(IndexFieldAccessor) initialized}) accessor
	 */
	public IndexFieldAccessor<F> createAccessor() {
		if ( accessorCreated ) {
			throw log.cannotCreateAccessorMultipleTimes( schemaContext.getEventContext() );
		}
		accessorCreated = true;
		return rawAccessor;
	}

	/**
	 * @return A (potentially un-{@link #initialize(IndexFieldAccessor) initialized}) accessor
	 */
	public <V, U> IndexFieldAccessor<V> createAccessor(ToIndexFieldValueConverter<V, ? extends F> toIndexConverter,
			FromIndexFieldValueConverter<? super F, U> fromIndexConverter) {
		Contracts.assertNotNull( toIndexConverter, "toIndexConverter" );
		this.toIndexConverter = toIndexConverter;
		this.fromIndexConverter = fromIndexConverter;
		return new ConvertingIndexFieldAccessor<>( createAccessor(), toIndexConverter );
	}

	/**
	 * @return The user-configured converter for this field definition.
	 * @see org.hibernate.search.v6poc.backend.document.model.dsl.IndexSchemaFieldTerminalContext#createAccessor(ToIndexFieldValueConverter, FromIndexFieldValueConverter)
	 */
	public UserIndexFieldConverter<F> createUserIndexFieldConverter() {
		checkAccessorCreated();
		return new UserIndexFieldConverter<>(
				toIndexConverter,
				fromIndexConverter
		);
	}

	/**
	 * Initialize the field definition, enabling writes to an underlying field.
	 * <p>
	 * This method may or may not be called during bootstrap; if it isn't called,
	 * writes triggered by the mapper through the accessor won't have any effect.
	 *
	 * @param delegate The delegate to use when writing to the accessor returned by {@link #createAccessor()}.
	 */
	public void initialize(IndexFieldAccessor<F> delegate) {
		checkAccessorCreated();
		rawAccessor.initialize( delegate );
	}

	private void checkAccessorCreated() {
		if ( !accessorCreated ) {
			throw log.incompleteFieldDefinition( schemaContext.getEventContext() );
		}
	}
}
