/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.extractor.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.hibernate.search.v6poc.entity.pojo.extractor.ContainerValueExtractor;

public class ContainerValueExtractorPath {

	private static final ContainerValueExtractorPath DEFAULT = new ContainerValueExtractorPath(
			true, Collections.emptyList()
	);
	private static final ContainerValueExtractorPath NONE = new ContainerValueExtractorPath(
			false, Collections.emptyList()
	);

	public static ContainerValueExtractorPath defaultExtractors() {
		return DEFAULT;
	}

	public static ContainerValueExtractorPath noExtractors() {
		return NONE;
	}

	public static ContainerValueExtractorPath explicitExtractors(
			List<? extends Class<? extends ContainerValueExtractor>> extractorClasses) {
		if ( extractorClasses.isEmpty() ) {
			return noExtractors();
		}
		else {
			return new ContainerValueExtractorPath(
					false,
					Collections.unmodifiableList( new ArrayList<>( extractorClasses ) )
			);
		}
	}

	private final boolean applyDefaultExtractors;
	private final List<? extends Class<? extends ContainerValueExtractor>> explicitExtractorClasses;

	private ContainerValueExtractorPath(boolean applyDefaultExtractors,
			List<? extends Class<? extends ContainerValueExtractor>> explicitExtractorClasses) {
		this.applyDefaultExtractors = applyDefaultExtractors;
		this.explicitExtractorClasses = explicitExtractorClasses;
	}

	@Override
	public boolean equals(Object obj) {
		if ( ! ( obj instanceof ContainerValueExtractorPath ) ) {
			return false;
		}
		ContainerValueExtractorPath other = (ContainerValueExtractorPath) obj;
		return applyDefaultExtractors == other.applyDefaultExtractors
				&& explicitExtractorClasses.equals( other.explicitExtractorClasses );
	}

	@Override
	public int hashCode() {
		return Objects.hash( applyDefaultExtractors, explicitExtractorClasses );
	}

	public boolean isDefault() {
		return applyDefaultExtractors;
	}

	public boolean isEmpty() {
		return !isDefault() && explicitExtractorClasses.isEmpty();
	}

	public List<? extends Class<? extends ContainerValueExtractor>> getExplicitExtractorClasses() {
		return explicitExtractorClasses;
	}
}
