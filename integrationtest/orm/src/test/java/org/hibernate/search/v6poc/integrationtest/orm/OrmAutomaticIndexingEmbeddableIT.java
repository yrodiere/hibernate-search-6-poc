/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.integrationtest.orm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.persistence.AssociationOverride;
import javax.persistence.Basic;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.SessionFactoryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.search.v6poc.entity.orm.cfg.SearchOrmSettings;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation.DocumentId;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation.Field;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.hibernate.search.v6poc.integrationtest.util.common.rule.BackendMock;
import org.hibernate.search.v6poc.integrationtest.util.common.rule.StaticCounters;
import org.hibernate.search.v6poc.integrationtest.util.common.stub.backend.index.impl.StubBackendFactory;
import org.hibernate.search.v6poc.integrationtest.util.orm.OrmUtils;
import org.hibernate.service.ServiceRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test automatic indexing based on Hibernate ORM entity events when embeddable objects are involved.
 */
public class OrmAutomaticIndexingEmbeddableIT {

	private static final String PREFIX = SearchOrmSettings.PREFIX;

	@Rule
	public BackendMock backendMock = new BackendMock( "stubBackend" );

	@Rule
	public StaticCounters counters = new StaticCounters();

	private SessionFactory sessionFactory;

	@Before
	public void setup() {
		StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder()
				.applySetting( PREFIX + "backend.stubBackend.type", StubBackendFactory.class.getName() )
				.applySetting( PREFIX + "index.default.backend", "stubBackend" );

		ServiceRegistry serviceRegistry = registryBuilder.build();

		MetadataSources ms = new MetadataSources( serviceRegistry )
				.addAnnotatedClass( IndexedEntity.class )
				.addAnnotatedClass( ContainingEntity.class )
				.addAnnotatedClass( ContainedEntity.class );

		Metadata metadata = ms.buildMetadata();

		final SessionFactoryBuilder sfb = metadata.getSessionFactoryBuilder();

		backendMock.expectSchema( IndexedEntity.INDEX, b -> b
				.objectField( "child", b4 -> b4
						.objectField( "containedEmbeddedSingle", b2 -> b2
								.objectField( "containedSingle", b3 -> b3
										.field( "includedInEmbeddedSingle", String.class )
								)
						)
						.objectField( "containedEmbeddedList", b2 -> b2
								.objectField( "containedList", b3 -> b3
										.field( "includedInEmbeddedList", String.class )
								)
						)
						.objectField( "containedElementCollection", b2 -> b2
								.objectField( "containedSingle", b3 -> b3
										.field( "includedInElementCollection", String.class )
								)
						)
						.objectField( "containedBidirectionalEmbedded", b2 -> b2
								.objectField( "containedSingle", b3 -> b3
										.field( "includedInBidirectionalEmbedded", String.class )
								)
						)
				)
		);

		sessionFactory = sfb.build();
		backendMock.verifyExpectationsMet();
	}

	@After
	public void cleanup() {
		if ( sessionFactory != null ) {
			sessionFactory.close();
		}
	}

	@Test
	public void indirectEmbeddedUpdate_embeddedSingle() {
		OrmUtils.withinTransaction( sessionFactory, session -> {
			IndexedEntity entity1 = new IndexedEntity();
			entity1.setId( 1 );

			ContainingEntity containingEntity1 = new ContainingEntity();
			containingEntity1.setId( 2 );
			entity1.setChild( containingEntity1 );
			containingEntity1.setParent( entity1 );

			session.persist( containingEntity1 );
			session.persist( entity1 );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.add( "1", b -> b
							.objectField( "child", b2 -> { } )
					)
					.preparedThenExecuted();
		} );
		backendMock.verifyExpectationsMet();

		// Test adding a value
		OrmUtils.withinTransaction( sessionFactory, session -> {
			ContainedEntity containedEntity = new ContainedEntity();
			containedEntity.setId( 4 );
			containedEntity.setIncludedInEmbeddedSingle( "initialValue" );

			ContainingEntity containingEntity1 = session.get( ContainingEntity.class, 2 );
			containingEntity1.setContainedEmbeddedSingle( new SingleContainingEmbeddable( containedEntity ) );
			containedEntity.setContainingAsEmbeddedSingle( Arrays.asList( containingEntity1 ) );

			session.persist( containedEntity );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.update( "1", b -> b
							.objectField( "child", b2 -> b2
									.objectField( "containedEmbeddedSingle", b3 -> b3
											.objectField( "containedSingle", b4 -> b4
													.field( "includedInEmbeddedSingle", "initialValue" )
											)
									)
							)
					)
					.preparedThenExecuted();
		} );
		backendMock.verifyExpectationsMet();

		// Test updating a value
		OrmUtils.withinTransaction( sessionFactory, session -> {
			ContainedEntity containedEntity = new ContainedEntity();
			containedEntity.setId( 5 );
			containedEntity.setIncludedInEmbeddedSingle( "updatedValue" );

			ContainingEntity containingEntity1 = session.get( ContainingEntity.class, 2 );
			containingEntity1.getContainedEmbeddedSingle().getContainedSingle().getContainingAsEmbeddedSingle().clear();
			containingEntity1.setContainedEmbeddedSingle( new SingleContainingEmbeddable( containedEntity ) );
			containedEntity.setContainingAsEmbeddedSingle( Arrays.asList( containingEntity1 ) );

			session.persist( containedEntity );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.update( "1", b -> b
							.objectField( "child", b2 -> b2
									.objectField( "containedEmbeddedSingle", b3 -> b3
											.objectField( "containedSingle", b4 -> b4
													.field( "includedInEmbeddedSingle", "updatedValue" )
											)
									)
							)
					)
					.preparedThenExecuted();
		} );
		backendMock.verifyExpectationsMet();

		// Test removing a value
		OrmUtils.withinTransaction( sessionFactory, session -> {
			ContainingEntity containingEntity1 = session.get( ContainingEntity.class, 2 );
			containingEntity1.getContainedEmbeddedSingle().getContainedSingle().getContainingAsEmbeddedSingle().clear();
			containingEntity1.setContainedEmbeddedSingle( null );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.update( "1", b -> { } )
					.preparedThenExecuted();
		} );
		backendMock.verifyExpectationsMet();
	}

	@Test
	public void indirectAssociationUpdate_embeddedSingle() {
		OrmUtils.withinTransaction( sessionFactory, session -> {
			IndexedEntity entity1 = new IndexedEntity();
			entity1.setId( 1 );

			ContainingEntity containingEntity1 = new ContainingEntity();
			containingEntity1.setId( 2 );
			entity1.setChild( containingEntity1 );
			containingEntity1.setParent( entity1 );

			ContainedEntity containedEntity = new ContainedEntity();
			containedEntity.setId( 4 );
			containedEntity.setIncludedInEmbeddedSingle( "initialValue" );
			containingEntity1.setContainedEmbeddedSingle( new SingleContainingEmbeddable( containedEntity ) );
			containedEntity.setContainingAsEmbeddedSingle( Arrays.asList( containingEntity1 ) );

			session.persist( containedEntity );
			session.persist( containingEntity1 );
			session.persist( entity1 );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.add( "1", b -> b
							.objectField( "child", b2 -> b2
									.objectField( "containedEmbeddedSingle", b3 -> b3
											.objectField( "containedSingle", b4 -> b4
													.field( "includedInEmbeddedSingle", "initialValue" )
											)
									)
							)
					)
					.preparedThenExecuted();
		} );
		backendMock.verifyExpectationsMet();

		// Test updating a value
		OrmUtils.withinTransaction( sessionFactory, session -> {
			ContainedEntity containedEntity = new ContainedEntity();
			containedEntity.setId( 5 );
			containedEntity.setIncludedInEmbeddedSingle( "updatedValue" );

			ContainingEntity containingEntity1 = session.get( ContainingEntity.class, 2 );
			containingEntity1.getContainedEmbeddedSingle().getContainedSingle().getContainingAsEmbeddedSingle().clear();
			containingEntity1.getContainedEmbeddedSingle().setContainedSingle( containedEntity );
			containedEntity.setContainingAsEmbeddedSingle( Arrays.asList( containingEntity1 ) );

			session.persist( containedEntity );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.update( "1", b -> b
							.objectField( "child", b2 -> b2
									.objectField( "containedEmbeddedSingle", b3 -> b3
											.objectField( "containedSingle", b4 -> b4
													.field( "includedInEmbeddedSingle", "updatedValue" )
											)
									)
							)
					)
					.preparedThenExecuted();
		} );
		backendMock.verifyExpectationsMet();

		// Test removing a value
		OrmUtils.withinTransaction( sessionFactory, session -> {
			ContainingEntity containingEntity1 = session.get( ContainingEntity.class, 2 );
			containingEntity1.getContainedEmbeddedSingle().getContainedSingle().getContainingAsEmbeddedSingle().clear();
			containingEntity1.getContainedEmbeddedSingle().setContainedSingle( null );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.update( "1", b -> { } )
					.preparedThenExecuted();
		} );
		backendMock.verifyExpectationsMet();
	}

	@Test
	public void indirectValueUpdate_embeddedSingle() {
		OrmUtils.withinTransaction( sessionFactory, session -> {
			IndexedEntity entity1 = new IndexedEntity();
			entity1.setId( 1 );

			ContainingEntity containingEntity1 = new ContainingEntity();
			containingEntity1.setId( 2 );
			entity1.setChild( containingEntity1 );
			containingEntity1.setParent( entity1 );

			ContainedEntity containedEntity1 = new ContainedEntity();
			containedEntity1.setId( 4 );
			containedEntity1.setIncludedInEmbeddedSingle( "initialValue" );
			containingEntity1.setContainedEmbeddedSingle( new SingleContainingEmbeddable( containedEntity1 ) );
			containedEntity1.setContainingAsEmbeddedSingle( Arrays.asList( containingEntity1 ) );

			session.persist( containedEntity1 );
			session.persist( containingEntity1 );
			session.persist( entity1 );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.add( "1", b -> b
							.objectField( "child", b2 -> b2
									.objectField( "containedEmbeddedSingle", b3 -> b3
											.objectField( "containedSingle", b4 -> b4
													.field( "includedInEmbeddedSingle", "initialValue" )
											)
									)
							)
					)
					.preparedThenExecuted();
		} );
		backendMock.verifyExpectationsMet();

		// Test updating the value
		OrmUtils.withinTransaction( sessionFactory, session -> {
			ContainedEntity containedEntity = session.get( ContainedEntity.class, 4 );
			containedEntity.setIncludedInEmbeddedSingle( "updatedValue" );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.update( "1", b -> b
							.objectField( "child", b2 -> b2
									.objectField( "containedEmbeddedSingle", b3 -> b3
											.objectField( "containedSingle", b4 -> b4
													.field( "includedInEmbeddedSingle", "updatedValue" )
											)
									)
							)
					)
					.preparedThenExecuted();
		} );
		backendMock.verifyExpectationsMet();
	}

	@Test
	public void indirectEmbeddedUpdate_embeddedList() {
		OrmUtils.withinTransaction( sessionFactory, session -> {
			IndexedEntity entity1 = new IndexedEntity();
			entity1.setId( 1 );

			ContainingEntity containingEntity1 = new ContainingEntity();
			containingEntity1.setId( 2 );
			entity1.setChild( containingEntity1 );
			containingEntity1.setParent( entity1 );

			session.persist( containingEntity1 );
			session.persist( entity1 );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.add( "1", b -> b
							.objectField( "child", b2 -> { } )
					)
					.preparedThenExecuted();
		} );
		backendMock.verifyExpectationsMet();

		// Test adding a value
		OrmUtils.withinTransaction( sessionFactory, session -> {
			ContainedEntity containedEntity = new ContainedEntity();
			containedEntity.setId( 4 );
			containedEntity.setIncludedInEmbeddedList( "initialValue" );

			ContainingEntity containingEntity1 = session.get( ContainingEntity.class, 2 );
			containingEntity1.setContainedEmbeddedList( new ListContainingEmbeddable( containedEntity ) );
			containedEntity.setContainingAsEmbeddedList( Arrays.asList( containingEntity1 ) );

			session.persist( containedEntity );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.update( "1", b -> b
							.objectField( "child", b2 -> b2
									.objectField( "containedEmbeddedList", b3 -> b3
											.objectField( "containedList", b4 -> b4
													.field( "includedInEmbeddedList", "initialValue" )
											)
									)
							)
					)
					.preparedThenExecuted();
		} );
		backendMock.verifyExpectationsMet();

		// Test updating a value
		OrmUtils.withinTransaction( sessionFactory, session -> {
			ContainedEntity containedEntity = new ContainedEntity();
			containedEntity.setId( 5 );
			containedEntity.setIncludedInEmbeddedList( "updatedValue" );

			ContainingEntity containingEntity1 = session.get( ContainingEntity.class, 2 );
			containingEntity1.getContainedEmbeddedList().getContainedList().get( 0 ).getContainingAsEmbeddedList().clear();
			containingEntity1.setContainedEmbeddedList( new ListContainingEmbeddable( containedEntity ) );
			containedEntity.setContainingAsEmbeddedList( Arrays.asList( containingEntity1 ) );

			session.persist( containedEntity );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.update( "1", b -> b
							.objectField( "child", b2 -> b2
									.objectField( "containedEmbeddedList", b3 -> b3
											.objectField( "containedList", b4 -> b4
													.field( "includedInEmbeddedList", "updatedValue" )
											)
									)
							)
					)
					.preparedThenExecuted();
		} );
		backendMock.verifyExpectationsMet();

		// Test removing a value
		OrmUtils.withinTransaction( sessionFactory, session -> {
			ContainingEntity containingEntity1 = session.get( ContainingEntity.class, 2 );
			containingEntity1.getContainedEmbeddedList().getContainedList().get( 0 ).getContainingAsEmbeddedList().clear();
			containingEntity1.setContainedEmbeddedList( null );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.update( "1", b -> { } )
					.preparedThenExecuted();
		} );
		backendMock.verifyExpectationsMet();
	}

	@Test
	public void indirectAssociationUpdate_embeddedList() {
		OrmUtils.withinTransaction( sessionFactory, session -> {
			IndexedEntity entity1 = new IndexedEntity();
			entity1.setId( 1 );

			ContainingEntity containingEntity1 = new ContainingEntity();
			containingEntity1.setId( 2 );
			entity1.setChild( containingEntity1 );
			containingEntity1.setParent( entity1 );

			session.persist( containingEntity1 );
			session.persist( entity1 );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.add( "1", b -> b
							.objectField( "child", b2 -> { } )
					)
					.preparedThenExecuted();
		} );
		backendMock.verifyExpectationsMet();

		// Test adding a value
		OrmUtils.withinTransaction( sessionFactory, session -> {
			ContainedEntity containedEntity = new ContainedEntity();
			containedEntity.setId( 5 );
			containedEntity.setIncludedInEmbeddedList( "firstValue" );

			ContainingEntity containingEntity1 = session.get( ContainingEntity.class, 2 );
			containingEntity1.getContainedEmbeddedList().getContainedList().add( containedEntity );
			containedEntity.setContainingAsEmbeddedList( Arrays.asList( containingEntity1 ) );

			session.persist( containedEntity );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.update( "1", b -> b
							.objectField( "child", b2 -> b2
									.objectField( "containedEmbeddedList", b3 -> b3
											.objectField( "containedList", b4 -> b4
													.field( "includedInEmbeddedList", "firstValue" )
											)
									)
							)
					)
					.preparedThenExecuted();
		} );
		backendMock.verifyExpectationsMet();

		// Test adding another value
		OrmUtils.withinTransaction( sessionFactory, session -> {
			ContainedEntity containedEntity = new ContainedEntity();
			containedEntity.setId( 6 );
			containedEntity.setIncludedInEmbeddedList( "secondValue" );

			ContainingEntity containingEntity1 = session.get( ContainingEntity.class, 2 );
			containingEntity1.getContainedEmbeddedList().getContainedList().add( containedEntity );
			containedEntity.setContainingAsEmbeddedList( Arrays.asList( containingEntity1 ) );

			session.persist( containedEntity );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.update( "1", b -> b
							.objectField( "child", b2 -> b2
									.objectField( "containedEmbeddedList", b3 -> b3
											.objectField( "containedList", b4 -> b4
													.field( "includedInEmbeddedList", "firstValue" )
											)
									)
									.objectField( "containedEmbeddedList", b3 -> b3
											.objectField( "containedList", b4 -> b4
													.field( "includedInEmbeddedList", "secondValue" )
											)
									)
							)
					)
					.preparedThenExecuted();
		} );
		backendMock.verifyExpectationsMet();

		// Test removing a value
		OrmUtils.withinTransaction( sessionFactory, session -> {
			ContainingEntity containingEntity1 = session.get( ContainingEntity.class, 2 );
			ContainedEntity containedEntity = containingEntity1.getContainedEmbeddedList().getContainedList().get( 0 );
			containedEntity.getContainingAsEmbeddedList().clear();
			containingEntity1.getContainedEmbeddedList().getContainedList().remove( containedEntity );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.update( "1", b -> b
							.objectField( "child", b2 -> b2
									.objectField( "containedEmbeddedList", b3 -> b3
											.objectField( "containedList", b4 -> b4
													.field( "includedInEmbeddedList", "secondValue" )
											)
									)
							)
					)
					.preparedThenExecuted();
		} );
		backendMock.verifyExpectationsMet();
	}

	@Test
	public void indirectValueUpdate_embeddedList() {
		OrmUtils.withinTransaction( sessionFactory, session -> {
			IndexedEntity entity1 = new IndexedEntity();
			entity1.setId( 1 );

			ContainingEntity containingEntity1 = new ContainingEntity();
			containingEntity1.setId( 2 );
			entity1.setChild( containingEntity1 );
			containingEntity1.setParent( entity1 );

			ContainedEntity containedEntity1 = new ContainedEntity();
			containedEntity1.setId( 4 );
			containedEntity1.setIncludedInEmbeddedList( "initialValue" );
			containingEntity1.setContainedEmbeddedList( new ListContainingEmbeddable( containedEntity1 ) );
			containedEntity1.setContainingAsEmbeddedList( Arrays.asList( containingEntity1 ) );

			session.persist( containedEntity1 );
			session.persist( containingEntity1 );
			session.persist( entity1 );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.add( "1", b -> b
							.objectField( "child", b2 -> b2
									.objectField( "containedEmbeddedList", b3 -> b3
											.objectField( "containedList", b4 -> b4
													.field( "includedInEmbeddedList", "initialValue" )
											)
									)
							)
					)
					.preparedThenExecuted();
		} );
		backendMock.verifyExpectationsMet();

		// Test updating the value
		OrmUtils.withinTransaction( sessionFactory, session -> {
			ContainedEntity containedEntity = session.get( ContainedEntity.class, 4 );
			containedEntity.setIncludedInEmbeddedList( "updatedValue" );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.update( "1", b -> b
							.objectField( "child", b2 -> b2
									.objectField( "containedEmbeddedList", b3 -> b3
											.objectField( "containedList", b4 -> b4
													.field( "includedInEmbeddedList", "updatedValue" )
											)
									)
							)
					)
					.preparedThenExecuted();
		} );
		backendMock.verifyExpectationsMet();
	}

	@Test
	public void indirectEmbeddedUpdate_elementCollection() {
		OrmUtils.withinTransaction( sessionFactory, session -> {
			IndexedEntity entity1 = new IndexedEntity();
			entity1.setId( 1 );

			ContainingEntity containingEntity1 = new ContainingEntity();
			containingEntity1.setId( 2 );
			entity1.setChild( containingEntity1 );
			containingEntity1.setParent( entity1 );

			session.persist( containingEntity1 );
			session.persist( entity1 );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.add( "1", b -> b
							.objectField( "child", b2 -> { } )
					)
					.preparedThenExecuted();
		} );
		backendMock.verifyExpectationsMet();

		// Test adding a value
		OrmUtils.withinTransaction( sessionFactory, session -> {
			ContainedEntity containedEntity = new ContainedEntity();
			containedEntity.setId( 4 );
			containedEntity.setIncludedInElementCollection( "firstValue" );

			ContainingEntity containingEntity1 = session.get( ContainingEntity.class, 2 );
			containingEntity1.getContainedElementCollection().add( new SingleContainingEmbeddable( containedEntity ) );
			containedEntity.setContainingAsElementCollection( Arrays.asList( containingEntity1 ) );

			session.persist( containedEntity );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.update( "1", b -> b
							.objectField( "child", b2 -> b2
									.objectField( "containedElementCollection", b3 -> b3
											.objectField( "containedSingle", b4 -> b4
													.field( "includedInElementCollection", "firstValue" )
											)
									)
							)
					)
					.preparedThenExecuted();
		} );
		backendMock.verifyExpectationsMet();

		// Test adding another value
		OrmUtils.withinTransaction( sessionFactory, session -> {
			ContainedEntity containedEntity = new ContainedEntity();
			containedEntity.setId( 5 );
			containedEntity.setIncludedInElementCollection( "secondValue" );

			ContainingEntity containingEntity1 = session.get( ContainingEntity.class, 2 );
			containingEntity1.getContainedElementCollection().add( new SingleContainingEmbeddable( containedEntity ) );
			containedEntity.setContainingAsElementCollection( Arrays.asList( containingEntity1 ) );

			session.persist( containedEntity );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.update( "1", b -> b
							.objectField( "child", b2 -> b2
									.objectField( "containedElementCollection", b3 -> b3
											.objectField( "containedSingle", b4 -> b4
													.field( "includedInElementCollection", "firstValue" )
											)
									)
									.objectField( "containedElementCollection", b3 -> b3
											.objectField( "containedSingle", b4 -> b4
													.field( "includedInElementCollection", "secondValue" )
											)
									)
							)
					)
					.preparedThenExecuted();
		} );
		backendMock.verifyExpectationsMet();

		// Test removing a value
		OrmUtils.withinTransaction( sessionFactory, session -> {
			ContainingEntity containingEntity1 = session.get( ContainingEntity.class, 2 );
			SingleContainingEmbeddable embeddable = containingEntity1.getContainedElementCollection().get( 0 );
			embeddable.getContainedSingle().getContainingAsElementCollection().clear();
			containingEntity1.getContainedElementCollection().remove( embeddable );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.update( "1", b -> b
							.objectField( "child", b2 -> b2
									.objectField( "containedElementCollection", b3 -> b3
											.objectField( "containedSingle", b4 -> b4
													.field( "includedInElementCollection", "secondValue" )
											)
									)
							)
					)
					.preparedThenExecuted();
		} );
		backendMock.verifyExpectationsMet();
	}

	@Test
	public void indirectAssociationUpdate_elementCollection() {
		OrmUtils.withinTransaction( sessionFactory, session -> {
			IndexedEntity entity1 = new IndexedEntity();
			entity1.setId( 1 );

			ContainingEntity containingEntity1 = new ContainingEntity();
			containingEntity1.setId( 2 );
			entity1.setChild( containingEntity1 );
			containingEntity1.setParent( entity1 );

			ContainedEntity containedEntity = new ContainedEntity();
			containedEntity.setId( 4 );
			containedEntity.setIncludedInElementCollection( "initialValue" );
			containingEntity1.setContainedElementCollection( Arrays.asList( new SingleContainingEmbeddable() ) );
			containedEntity.setContainingAsElementCollection( Arrays.asList( containingEntity1 ) );

			session.persist( containedEntity );
			session.persist( containingEntity1 );
			session.persist( entity1 );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.add( "1", b -> b
							.objectField( "child", b2 -> b2
									.objectField( "containedElementCollection", b3 -> { } )
							)
					)
					.preparedThenExecuted();
		} );
		backendMock.verifyExpectationsMet();

		// Test adding a value
		OrmUtils.withinTransaction( sessionFactory, session -> {
			ContainedEntity containedEntity = new ContainedEntity();
			containedEntity.setId( 5 );
			containedEntity.setIncludedInElementCollection( "initialValue" );

			ContainingEntity containingEntity1 = session.get( ContainingEntity.class, 2 );
			/*
			 * Hibernate ORM automatically removes embeddable with only null attributes from the collection,
			 * so we must add the collection again.
			 * Ideally we should not need this line, but that's the way it is.
			 */
			containingEntity1.getContainedElementCollection().add( new SingleContainingEmbeddable() );
			containingEntity1.getContainedElementCollection().get( 0 ).setContainedSingle( containedEntity );
			containedEntity.setContainingAsElementCollection( Arrays.asList( containingEntity1 ) );

			session.persist( containedEntity );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.update( "1", b -> b
							.objectField( "child", b2 -> b2
									.objectField( "containedElementCollection", b3 -> b3
											.objectField( "containedSingle", b4 -> b4
													.field( "includedInElementCollection", "initialValue" )
											)
									)
							)
					)
					.preparedThenExecuted();
		} );
		backendMock.verifyExpectationsMet();

		// Test updating a value
		OrmUtils.withinTransaction( sessionFactory, session -> {
			ContainedEntity containedEntity = new ContainedEntity();
			containedEntity.setId( 6 );
			containedEntity.setIncludedInElementCollection( "updatedValue" );

			ContainingEntity containingEntity1 = session.get( ContainingEntity.class, 2 );
			SingleContainingEmbeddable embeddable = containingEntity1.getContainedElementCollection().get( 0 );
			embeddable.getContainedSingle().getContainingAsElementCollection().clear();
			embeddable.setContainedSingle( containedEntity );
			containedEntity.setContainingAsElementCollection( Arrays.asList( containingEntity1 ) );

			session.persist( containedEntity );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.update( "1", b -> b
							.objectField( "child", b2 -> b2
									.objectField( "containedElementCollection", b3 -> b3
											.objectField( "containedSingle", b4 -> b4
												.field( "includedInElementCollection", "updatedValue" )
											)
									)
							)
					)
					.preparedThenExecuted();
		} );
		backendMock.verifyExpectationsMet();

		// Test removing a value
		OrmUtils.withinTransaction( sessionFactory, session -> {
			ContainingEntity containingEntity1 = session.get( ContainingEntity.class, 2 );
			SingleContainingEmbeddable embeddable = containingEntity1.getContainedElementCollection().get( 0 );
			embeddable.getContainedSingle().getContainingAsElementCollection().clear();
			embeddable.setContainedSingle( null );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.update( "1", b -> { } )
					.preparedThenExecuted();
		} );
		backendMock.verifyExpectationsMet();
	}

	@Test
	public void indirectValueUpdate_elementCollection() {
		OrmUtils.withinTransaction( sessionFactory, session -> {
			IndexedEntity entity1 = new IndexedEntity();
			entity1.setId( 1 );

			ContainingEntity containingEntity1 = new ContainingEntity();
			containingEntity1.setId( 2 );
			entity1.setChild( containingEntity1 );
			containingEntity1.setParent( entity1 );

			ContainedEntity containedEntity1 = new ContainedEntity();
			containedEntity1.setId( 4 );
			containedEntity1.setIncludedInElementCollection( "initialValue" );
			containingEntity1.setContainedElementCollection(
					Arrays.asList( new SingleContainingEmbeddable( containedEntity1 ) )
			);
			containedEntity1.setContainingAsElementCollection( Arrays.asList( containingEntity1 ) );

			session.persist( containedEntity1 );
			session.persist( containingEntity1 );
			session.persist( entity1 );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.add( "1", b -> b
							.objectField( "child", b2 -> b2
									.objectField( "containedElementCollection", b3 -> b3
											.objectField( "containedSingle", b4 -> b4
													.field( "includedInElementCollection", "initialValue" )
											)
									)
							)
					)
					.preparedThenExecuted();
		} );
		backendMock.verifyExpectationsMet();

		// Test updating the value
		OrmUtils.withinTransaction( sessionFactory, session -> {
			ContainedEntity containedEntity = session.get( ContainedEntity.class, 4 );
			containedEntity.setIncludedInElementCollection( "updatedValue" );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.update( "1", b -> b
							.objectField( "child", b2 -> b2
									.objectField( "containedElementCollection", b3 -> b3
											.objectField( "containedSingle", b4 -> b4
													.field( "includedInElementCollection", "updatedValue" )
											)
									)
							)
					)
					.preparedThenExecuted();
		} );
		backendMock.verifyExpectationsMet();
	}

	@Test
	public void indirectEmbeddedUpdate_bidirectionalEmbedded() {
		OrmUtils.withinTransaction( sessionFactory, session -> {
			IndexedEntity entity1 = new IndexedEntity();
			entity1.setId( 1 );

			ContainingEntity containingEntity1 = new ContainingEntity();
			containingEntity1.setId( 2 );
			entity1.setChild( containingEntity1 );
			containingEntity1.setParent( entity1 );

			session.persist( containingEntity1 );
			session.persist( entity1 );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.add( "1", b -> b
							.objectField( "child", b2 -> { } )
					)
					.preparedThenExecuted();
		} );
		backendMock.verifyExpectationsMet();

		// Test adding a value
		OrmUtils.withinTransaction( sessionFactory, session -> {
			ContainedEntity containedEntity = new ContainedEntity();
			containedEntity.setId( 4 );
			containedEntity.setIncludedInBidirectionalEmbedded( "initialValue" );

			ContainingEntity containingEntity1 = session.get( ContainingEntity.class, 2 );
			containingEntity1.setContainedBidirectionalEmbedded( new BidirectionalEmbeddable( containedEntity ) );
			containedEntity.setContainingAsBidirectionalEmbedded( new InverseBidirectionalEmbeddable( containingEntity1 ) );

			session.persist( containedEntity );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.update( "1", b -> b
							.objectField( "child", b2 -> b2
									.objectField( "containedBidirectionalEmbedded", b3 -> b3
											.objectField( "containedSingle", b4 -> b4
													.field( "includedInBidirectionalEmbedded", "initialValue" )
											)
									)
							)
					)
					.preparedThenExecuted();
		} );
		backendMock.verifyExpectationsMet();

		// Test updating a value
		OrmUtils.withinTransaction( sessionFactory, session -> {
			ContainedEntity containedEntity = new ContainedEntity();
			containedEntity.setId( 5 );
			containedEntity.setIncludedInBidirectionalEmbedded( "updatedValue" );

			ContainingEntity containingEntity1 = session.get( ContainingEntity.class, 2 );
			containingEntity1.getContainedBidirectionalEmbedded().getContainedSingle()
					.getContainingAsBidirectionalEmbedded().setContainingAsSingle( null );
			containingEntity1.setContainedBidirectionalEmbedded( new BidirectionalEmbeddable( containedEntity ) );
			containedEntity.setContainingAsBidirectionalEmbedded( new InverseBidirectionalEmbeddable( containingEntity1 ) );

			session.persist( containedEntity );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.update( "1", b -> b
							.objectField( "child", b2 -> b2
									.objectField( "containedBidirectionalEmbedded", b3 -> b3
											.objectField( "containedSingle", b4 -> b4
													.field( "includedInBidirectionalEmbedded", "updatedValue" )
											)
									)
							)
					)
					.preparedThenExecuted();
		} );
		backendMock.verifyExpectationsMet();

		// Test removing a value
		OrmUtils.withinTransaction( sessionFactory, session -> {
			ContainingEntity containingEntity1 = session.get( ContainingEntity.class, 2 );
			containingEntity1.getContainedBidirectionalEmbedded().getContainedSingle()
					.getContainingAsBidirectionalEmbedded().setContainingAsSingle( null );
			containingEntity1.setContainedBidirectionalEmbedded( null );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.update( "1", b -> { } )
					.preparedThenExecuted();
		} );
		backendMock.verifyExpectationsMet();
	}

	@Test
	public void indirectAssociationUpdate_bidirectionalEmbedded() {
		OrmUtils.withinTransaction( sessionFactory, session -> {
			IndexedEntity entity1 = new IndexedEntity();
			entity1.setId( 1 );

			ContainingEntity containingEntity1 = new ContainingEntity();
			containingEntity1.setId( 2 );
			entity1.setChild( containingEntity1 );
			containingEntity1.setParent( entity1 );

			ContainedEntity containedEntity = new ContainedEntity();
			containedEntity.setId( 4 );
			containedEntity.setIncludedInBidirectionalEmbedded( "initialValue" );
			containingEntity1.setContainedBidirectionalEmbedded( new BidirectionalEmbeddable( containedEntity ) );
			containedEntity.setContainingAsBidirectionalEmbedded( new InverseBidirectionalEmbeddable( containingEntity1 ) );

			session.persist( containedEntity );
			session.persist( containingEntity1 );
			session.persist( entity1 );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.add( "1", b -> b
							.objectField( "child", b2 -> b2
									.objectField( "containedBidirectionalEmbedded", b3 -> b3
											.objectField( "containedSingle", b4 -> b4
													.field( "includedInBidirectionalEmbedded", "initialValue" )
											)
									)
							)
					)
					.preparedThenExecuted();
		} );
		backendMock.verifyExpectationsMet();

		// Test updating a value
		OrmUtils.withinTransaction( sessionFactory, session -> {
			ContainedEntity containedEntity = new ContainedEntity();
			containedEntity.setId( 5 );
			containedEntity.setIncludedInBidirectionalEmbedded( "updatedValue" );

			ContainingEntity containingEntity1 = session.get( ContainingEntity.class, 2 );
			containingEntity1.getContainedBidirectionalEmbedded().getContainedSingle()
					.getContainingAsBidirectionalEmbedded().setContainingAsSingle( null );
			containingEntity1.getContainedBidirectionalEmbedded().setContainedSingle( containedEntity );
			containedEntity.setContainingAsBidirectionalEmbedded( new InverseBidirectionalEmbeddable( containingEntity1 ) );

			session.persist( containedEntity );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.update( "1", b -> b
							.objectField( "child", b2 -> b2
									.objectField( "containedBidirectionalEmbedded", b3 -> b3
											.objectField( "containedSingle", b4 -> b4
													.field( "includedInBidirectionalEmbedded", "updatedValue" )
											)
									)
							)
					)
					.preparedThenExecuted();
		} );
		backendMock.verifyExpectationsMet();

		// Test removing a value
		OrmUtils.withinTransaction( sessionFactory, session -> {
			ContainingEntity containingEntity1 = session.get( ContainingEntity.class, 2 );
			containingEntity1.getContainedBidirectionalEmbedded().getContainedSingle()
					.getContainingAsBidirectionalEmbedded().setContainingAsSingle( null );
			containingEntity1.getContainedBidirectionalEmbedded().setContainedSingle( null );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.update( "1", b -> { } )
					.preparedThenExecuted();
		} );
		backendMock.verifyExpectationsMet();
	}

	@Test
	public void indirectValueUpdate_bidirectionalEmbedded() {
		OrmUtils.withinTransaction( sessionFactory, session -> {
			IndexedEntity entity1 = new IndexedEntity();
			entity1.setId( 1 );

			ContainingEntity containingEntity1 = new ContainingEntity();
			containingEntity1.setId( 2 );
			entity1.setChild( containingEntity1 );
			containingEntity1.setParent( entity1 );

			ContainedEntity containedEntity1 = new ContainedEntity();
			containedEntity1.setId( 4 );
			containedEntity1.setIncludedInBidirectionalEmbedded( "initialValue" );
			containingEntity1.setContainedBidirectionalEmbedded( new BidirectionalEmbeddable( containedEntity1 ) );
			containedEntity1.setContainingAsBidirectionalEmbedded( new InverseBidirectionalEmbeddable( containingEntity1 ) );

			session.persist( containedEntity1 );
			session.persist( containingEntity1 );
			session.persist( entity1 );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.add( "1", b -> b
							.objectField( "child", b2 -> b2
									.objectField( "containedBidirectionalEmbedded", b3 -> b3
											.objectField( "containedSingle", b4 -> b4
													.field( "includedInBidirectionalEmbedded", "initialValue" )
											)
									)
							)
					)
					.preparedThenExecuted();
		} );
		backendMock.verifyExpectationsMet();

		// Test updating the value
		OrmUtils.withinTransaction( sessionFactory, session -> {
			ContainedEntity containedEntity = session.get( ContainedEntity.class, 4 );
			containedEntity.setIncludedInBidirectionalEmbedded( "updatedValue" );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.update( "1", b -> b
							.objectField( "child", b2 -> b2
									.objectField( "containedBidirectionalEmbedded", b3 -> b3
											.objectField( "containedSingle", b4 -> b4
													.field( "includedInBidirectionalEmbedded", "updatedValue" )
											)
									)
							)
					)
					.preparedThenExecuted();
		} );
		backendMock.verifyExpectationsMet();
	}

	@Entity(name = "containing")
	public static class ContainingEntity {

		@Id
		@DocumentId
		private Integer id;

		@OneToOne
		private IndexedEntity parent;

		@Embedded
		@IndexedEmbedded(includePaths = "containedSingle.includedInEmbeddedSingle")
		private SingleContainingEmbeddable containedEmbeddedSingle;

		@Embedded
		@IndexedEmbedded(includePaths = "containedList.includedInEmbeddedList")
		@AssociationOverride(
				name = "containedList",
				joinTable = @JoinTable(name = "containing_embeddedList")
		)
		private ListContainingEmbeddable containedEmbeddedList;

		@ElementCollection
		@JoinTable(name = "containing_elementCollection")
		@AssociationOverride(
				name = "containedSingle",
				/*
				 * For some reason Hibernate ORM sets the column name to
				 * "containedElementCollection_collection&&element_containedSingle_id".
				 * Let's work around the problem...
				 */
				joinColumns = @JoinColumn(name = "elementCollection_containedSingle_id")
		)
		@IndexedEmbedded(includePaths = "containedSingle.includedInElementCollection")
		private List<SingleContainingEmbeddable> containedElementCollection;

		@Embedded
		@IndexedEmbedded(includePaths = "containedSingle.includedInBidirectionalEmbedded")
		private BidirectionalEmbeddable containedBidirectionalEmbedded;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public IndexedEntity getParent() {
			return parent;
		}

		public void setParent(IndexedEntity parent) {
			this.parent = parent;
		}

		public SingleContainingEmbeddable getContainedEmbeddedSingle() {
			return containedEmbeddedSingle;
		}

		public void setContainedEmbeddedSingle(SingleContainingEmbeddable containedEmbeddedSingle) {
			this.containedEmbeddedSingle = containedEmbeddedSingle;
		}

		public ListContainingEmbeddable getContainedEmbeddedList() {
			return containedEmbeddedList;
		}

		public void setContainedEmbeddedList(ListContainingEmbeddable containedEmbeddedList) {
			this.containedEmbeddedList = containedEmbeddedList;
		}

		public List<SingleContainingEmbeddable> getContainedElementCollection() {
			return containedElementCollection;
		}

		public void setContainedElementCollection(List<SingleContainingEmbeddable> containedElementCollection) {
			this.containedElementCollection = containedElementCollection;
		}

		public BidirectionalEmbeddable getContainedBidirectionalEmbedded() {
			return containedBidirectionalEmbedded;
		}

		public void setContainedBidirectionalEmbedded(BidirectionalEmbeddable containedBidirectionalEmbedded) {
			this.containedBidirectionalEmbedded = containedBidirectionalEmbedded;
		}
	}

	@Entity(name = "indexed")
	@Indexed(index = IndexedEntity.INDEX)
	public static final class IndexedEntity {

		static final String INDEX = "IndexedEntity";

		@Id
		@DocumentId
		private Integer id;

		@OneToOne(mappedBy = "parent")
		@IndexedEmbedded(includePaths = {
				"containedEmbeddedSingle.containedSingle.includedInEmbeddedSingle",
				"containedEmbeddedList.containedList.includedInEmbeddedList",
				"containedElementCollection.containedSingle.includedInElementCollection",
				"containedBidirectionalEmbedded.containedSingle.includedInBidirectionalEmbedded"
		})
		private ContainingEntity child;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public ContainingEntity getChild() {
			return child;
		}

		public void setChild(ContainingEntity child) {
			this.child = child;
		}
	}

	@Entity(name = "contained")
	public static class ContainedEntity {

		@Id
		@DocumentId
		private Integer id;

		@OneToMany(mappedBy = "containedEmbeddedSingle.containedSingle")
		@OrderBy("id asc") // Make sure the iteration order is predictable
		private List<ContainingEntity> containingAsEmbeddedSingle;

		@ManyToMany(mappedBy = "containedEmbeddedList.containedList")
		@OrderBy("id asc") // Make sure the iteration order is predictable
		private List<ContainingEntity> containingAsEmbeddedList;

		/*
		 * No mappedBy here: the other side is configured as a ManyToOne because the property is single-valued,
		 * but it is actually a ManyToMany: there are multiple elements in the elementCollection,
		 * meaning we can target multiple entities from a single entity.
		 * Anyway, we can't use mappedBy in this specific case.
		 */
		@ManyToMany
		@JoinTable(name = "contained_containingAsElementCollection")
		@OrderBy("id asc") // Make sure the iteration order is predictable
		private List<ContainingEntity> containingAsElementCollection;

		@Embedded
		private InverseBidirectionalEmbeddable containingAsBidirectionalEmbedded;

		@Basic
		@Field
		private String includedInEmbeddedSingle;

		@Basic
		@Field
		private String includedInEmbeddedList;

		@Basic
		@Field
		private String includedInElementCollection;

		@Basic
		@Field
		private String includedInBidirectionalEmbedded;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public List<ContainingEntity> getContainingAsEmbeddedSingle() {
			return containingAsEmbeddedSingle;
		}

		public void setContainingAsEmbeddedSingle(List<ContainingEntity> containingAsEmbeddedSingle) {
			this.containingAsEmbeddedSingle = containingAsEmbeddedSingle;
		}

		public List<ContainingEntity> getContainingAsEmbeddedList() {
			return containingAsEmbeddedList;
		}

		public void setContainingAsEmbeddedList(List<ContainingEntity> containingAsEmbeddedList) {
			this.containingAsEmbeddedList = containingAsEmbeddedList;
		}

		public List<ContainingEntity> getContainingAsElementCollection() {
			return containingAsElementCollection;
		}

		public void setContainingAsElementCollection(List<ContainingEntity> containingAsElementCollection) {
			this.containingAsElementCollection = containingAsElementCollection;
		}

		public InverseBidirectionalEmbeddable getContainingAsBidirectionalEmbedded() {
			return containingAsBidirectionalEmbedded;
		}

		public void setContainingAsBidirectionalEmbedded(InverseBidirectionalEmbeddable containingAsBidirectionalEmbedded) {
			this.containingAsBidirectionalEmbedded = containingAsBidirectionalEmbedded;
		}

		public String getIncludedInEmbeddedSingle() {
			return includedInEmbeddedSingle;
		}

		public void setIncludedInEmbeddedSingle(String includedInEmbeddedSingle) {
			this.includedInEmbeddedSingle = includedInEmbeddedSingle;
		}

		public String getIncludedInEmbeddedList() {
			return includedInEmbeddedList;
		}

		public void setIncludedInEmbeddedList(String includedInEmbeddedList) {
			this.includedInEmbeddedList = includedInEmbeddedList;
		}

		public String getIncludedInElementCollection() {
			return includedInElementCollection;
		}

		public void setIncludedInElementCollection(String includedInElementCollection) {
			this.includedInElementCollection = includedInElementCollection;
		}

		public String getIncludedInBidirectionalEmbedded() {
			return includedInBidirectionalEmbedded;
		}

		public void setIncludedInBidirectionalEmbedded(String includedInBidirectionalEmbedded) {
			this.includedInBidirectionalEmbedded = includedInBidirectionalEmbedded;
		}
	}

	@Embeddable
	public static class SingleContainingEmbeddable {

		@ManyToOne
		@IndexedEmbedded
		private ContainedEntity containedSingle;

		public SingleContainingEmbeddable() {
		}

		public SingleContainingEmbeddable(ContainedEntity containedSingle) {
			this.containedSingle = containedSingle;
		}

		public ContainedEntity getContainedSingle() {
			return containedSingle;
		}

		public void setContainedSingle(ContainedEntity containedSingle) {
			this.containedSingle = containedSingle;
		}
	}

	@Embeddable
	public static class ListContainingEmbeddable {

		@ManyToMany
		@IndexedEmbedded
		private List<ContainedEntity> containedList;

		public ListContainingEmbeddable() {
		}

		public ListContainingEmbeddable(ContainedEntity containedEntity) {
			this( new ArrayList<>() );
			containedList.add( containedEntity );
		}

		public ListContainingEmbeddable(List<ContainedEntity> containedList) {
			this.containedList = containedList;
		}

		public List<ContainedEntity> getContainedList() {
			return containedList;
		}

		public void setContainedList(List<ContainedEntity> containedList) {
			this.containedList = containedList;
		}
	}

	@Embeddable
	public static class BidirectionalEmbeddable {

		@OneToOne
		@IndexedEmbedded
		private ContainedEntity containedSingle;

		public BidirectionalEmbeddable() {
		}

		public BidirectionalEmbeddable(ContainedEntity containedSingle) {
			this.containedSingle = containedSingle;
		}

		public ContainedEntity getContainedSingle() {
			return containedSingle;
		}

		public void setContainedSingle(ContainedEntity containedSingle) {
			this.containedSingle = containedSingle;
		}
	}

	@Embeddable
	public static class InverseBidirectionalEmbeddable {

		@OneToOne(mappedBy = "containedBidirectionalEmbedded.containedSingle")
		private ContainingEntity containingAsSingle;

		public InverseBidirectionalEmbeddable() {
		}

		public InverseBidirectionalEmbeddable(ContainingEntity containingAsSingle) {
			this.containingAsSingle = containingAsSingle;
		}

		public ContainingEntity getContainingAsSingle() {
			return containingAsSingle;
		}

		public void setContainingAsSingle(ContainingEntity containingAsSingle) {
			this.containingAsSingle = containingAsSingle;
		}
	}

}
