/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.integrationtest.showcase.library.dao;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.persistence.EntityManager;

import org.hibernate.search.v6poc.entity.orm.Search;
import org.hibernate.search.v6poc.entity.orm.jpa.FullTextEntityManager;
import org.hibernate.search.v6poc.entity.orm.jpa.FullTextQuery;
import org.hibernate.search.v6poc.integrationtest.showcase.library.model.Library;
import org.hibernate.search.v6poc.integrationtest.showcase.library.model.LibraryService;

public class LibraryDao {

	private final FullTextEntityManager entityManager;

	public LibraryDao(EntityManager entityManager) {
		this.entityManager = Search.getFullTextEntityManager( entityManager );
	}

	public Library create(int id, String name, double latitude, double longitude, LibraryService ... services) {
		Library library = new Library();
		library.setId( id );
		library.setName( name );
		library.setLatitude( latitude );
		library.setLongitude( longitude );
		library.setServices( Arrays.asList( services ) );
		entityManager.persist( library );
		return library;
	}

	public List<Library> search(String terms, int offset, int limit) {
		if ( terms == null || terms.isEmpty() ) {
			return Collections.emptyList();
		}
		FullTextQuery<Library> query = entityManager.search( Library.class ).query()
				.asEntities()
				.predicate().match().onField( "name" ).matching( terms )
				.sort().byField( "name_sort" ).end()
				.build();

		query.setFirstResult( offset );
		query.setMaxResults( limit );

		return query.getResultList();
	}

}
