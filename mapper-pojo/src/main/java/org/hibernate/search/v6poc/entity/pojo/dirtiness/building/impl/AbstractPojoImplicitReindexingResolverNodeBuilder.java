/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.dirtiness.building.impl;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.hibernate.search.v6poc.entity.pojo.dirtiness.impl.PojoImplicitReindexingResolver;
import org.hibernate.search.v6poc.entity.pojo.dirtiness.impl.PojoImplicitReindexingResolverDirtinessFilterNode;
import org.hibernate.search.v6poc.entity.pojo.model.path.PojoModelPathValueNode;
import org.hibernate.search.v6poc.entity.pojo.model.path.impl.BoundPojoModelPath;
import org.hibernate.search.v6poc.util.AssertionFailure;
import org.hibernate.search.v6poc.util.impl.common.Contracts;

abstract class AbstractPojoImplicitReindexingResolverNodeBuilder<T> {

	final PojoImplicitReindexingResolverBuildingHelper buildingHelper;

	private boolean frozen = false;
	// Use a LinkedHashSet for deterministic iteration
	private final Set<PojoModelPathValueNode> dirtyPathsTriggeringReindexingIncludingNestedNodes = new LinkedHashSet<>();

	AbstractPojoImplicitReindexingResolverNodeBuilder(PojoImplicitReindexingResolverBuildingHelper buildingHelper) {
		this.buildingHelper = buildingHelper;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + getModelPath() + "]";
	}

	abstract BoundPojoModelPath getModelPath();

	/**
	 * Freeze the builder, signaling that no mutating method will be called anymore
	 * and that derived data can be safely computed.
	 */
	final void freeze() {
		if ( !frozen ) {
			frozen = true;
			onFreeze( dirtyPathsTriggeringReindexingIncludingNestedNodes );
		}
	}

	abstract void onFreeze(Set<PojoModelPathValueNode> dirtyPathsTriggeringReindexingCollector);

	final void checkNotFrozen() {
		if ( frozen ) {
			throw new AssertionFailure(
					"A mutating method was called on " + this + " after it was frozen."
					+ " There is a bug in Hibernate Search, please report it."
			);
		}
	}

	final void checkFrozen() {
		if ( !frozen ) {
			throw new AssertionFailure(
					"A method was called on " + this + " before it was frozen, but a preliminary freeze is required."
					+ " There is a bug in Hibernate Search, please report it."
			);
		}
	}

	final Set<PojoModelPathValueNode> getDirtyPathsTriggeringReindexingIncludingNestedNodes() {
		checkFrozen();
		return dirtyPathsTriggeringReindexingIncludingNestedNodes;
	}

	/**
	 * @param allPotentialDirtyPaths A comprehensive list of all paths that may be dirty
	 * when the built resolver will be called. {@code null} if unknown.
	 */
	final Optional<PojoImplicitReindexingResolver<T>> build(Set<PojoModelPathValueNode> allPotentialDirtyPaths) {
		freeze();

		Set<PojoModelPathValueNode> immutableDirtyPathsAcceptedByFilter =
				getDirtyPathsTriggeringReindexingIncludingNestedNodes();

		if ( allPotentialDirtyPaths == null
				|| !immutableDirtyPathsAcceptedByFilter.containsAll( allPotentialDirtyPaths ) ) {
			/*
			 * The resolver may be called even when none of the dirty properties we are interested in is dirty.
			 * We need to add our own dirty check.
			 * Note that as a consequence, on contrary to the "else" branch below,
			 * we take care to call doBuild() passing as an argument
			 * the set of properties that the filter will allow.
			 */
			return doBuild( immutableDirtyPathsAcceptedByFilter )
					.map( resolver -> wrapWithFilter(
							resolver, immutableDirtyPathsAcceptedByFilter
					) );
		}
		else {
			/*
			 * The resolver will only be called when at least one of the dirty properties we are interested in is dirty.
			 * No need to add our own dirty check.
			 * Note that as a consequence, on contrary to the "if" branch above,
			 * we take care to call doBuild() passing as an argument
			 * the set of properties that the caller declared as "all potentially dirty properties".
			 */
			return doBuild( allPotentialDirtyPaths );
		}
	}

	abstract Optional<PojoImplicitReindexingResolver<T>> doBuild(Set<PojoModelPathValueNode> allPotentialDirtyPaths);

	private PojoImplicitReindexingResolver<T> wrapWithFilter(PojoImplicitReindexingResolver<T> resolver,
			Set<PojoModelPathValueNode> immutableDirtyPathsTriggeringReindexing) {
		return new PojoImplicitReindexingResolverDirtinessFilterNode<>(
				immutableDirtyPathsTriggeringReindexing, resolver
		);
	}

}
