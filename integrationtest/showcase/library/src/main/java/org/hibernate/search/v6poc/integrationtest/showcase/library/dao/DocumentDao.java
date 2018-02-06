/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.integrationtest.showcase.library.dao;

import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;

import org.hibernate.search.v6poc.backend.spatial.GeoPoint;
import org.hibernate.search.v6poc.entity.orm.Search;
import org.hibernate.search.v6poc.entity.orm.hibernate.FullTextSession;
import org.hibernate.search.v6poc.entity.orm.jpa.FullTextEntityManager;
import org.hibernate.search.v6poc.entity.orm.jpa.FullTextQuery;
import org.hibernate.search.v6poc.integrationtest.showcase.library.model.Book;
import org.hibernate.search.v6poc.integrationtest.showcase.library.model.BookCopy;
import org.hibernate.search.v6poc.integrationtest.showcase.library.model.BookMedium;
import org.hibernate.search.v6poc.integrationtest.showcase.library.model.Document;
import org.hibernate.search.v6poc.integrationtest.showcase.library.model.ISBN;
import org.hibernate.search.v6poc.integrationtest.showcase.library.model.Library;
import org.hibernate.search.v6poc.integrationtest.showcase.library.model.LibraryService;
import org.hibernate.search.v6poc.integrationtest.showcase.library.model.Video;
import org.hibernate.search.v6poc.integrationtest.showcase.library.model.VideoCopy;
import org.hibernate.search.v6poc.integrationtest.showcase.library.model.VideoMedium;

public class DocumentDao {

	// Hack to deal with Document<?> instead of raw Document
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static final Class<Document<?>> DOCUMENT_CLASS = (Class<Document<?>>) (Class) Document.class;

	private final FullTextEntityManager entityManager;

	public DocumentDao(EntityManager entityManager) {
		this.entityManager = Search.getFullTextEntityManager( entityManager );
	}

	public Book createBook(int id, ISBN isbn, String title, String summary, String tags) {
		Book book = new Book();
		book.setId( id );
		book.setIsbn( isbn );
		book.setTitle( title );
		book.setSummary( summary );
		book.setTags( tags );
		entityManager.persist( book );
		return book;
	}

	public Video createVideo(int id, String title, String summary, String tags) {
		Video video = new Video();
		video.setId( id );
		video.setTitle( title );
		video.setSummary( summary );
		video.setTags( tags );
		entityManager.persist( video );
		return video;
	}

	public BookCopy createCopy(Library library, Book document, BookMedium medium) {
		BookCopy copy = new BookCopy();
		copy.setLibrary( library );
		copy.setDocument( document );
		document.getCopies().add( copy );
		copy.setMedium( medium );
		entityManager.persist( copy );
		return copy;
	}

	public VideoCopy createCopy(Library library, Video document, VideoMedium medium) {
		VideoCopy copy = new VideoCopy();
		copy.setLibrary( library );
		copy.setDocument( document );
		document.getCopies().add( copy );
		copy.setMedium( medium );
		entityManager.persist( copy );
		return copy;
	}

	public Optional<Book> getByIsbn(String isbnAsString) {
		if ( isbnAsString == null ) {
			return Optional.empty();
		}
		// Must use Hibernate ORM types (as opposed to JPA types) to benefit from query.uniqueResult()
		org.hibernate.search.v6poc.entity.orm.hibernate.FullTextQuery<Book> query =
				entityManager.unwrap( FullTextSession.class ).search( Book.class ).query()
				.asEntities()
				.predicate().match().onField( "isbn" ).matching( isbnAsString )
				.build();

		return Optional.ofNullable( query.uniqueResult() );
	}

	public List<Document<?>> searchAroundMe(String terms, String tags,
			GeoPoint myLocation, Double maxDistanceInKilometers,
			List<LibraryService> libraryServices,
			int offset, int limit) {
		FullTextQuery<Document<?>> query = entityManager.search( DOCUMENT_CLASS ).query()
				.asEntities()
				.predicate().bool( b -> {
					// Match query
					b.must( ctx -> {
						if ( terms != null && !terms.isEmpty() ) {
							ctx.match()
									.onField( "title" ).boostedTo( 2.0f )
									.orField( "summary" )
									.matching( terms );
						}
					} );
					// Bridged query with complex bridge: TODO rely on the bridge to split the String
					b.must( ctx -> {
						String[] splitTags = tags == null ? null : tags.split( "," );
						if ( splitTags != null && splitTags.length > 0 ) {
							ctx.bool().must( c2 -> {
								for ( String tag : splitTags ) {
									c2.match()
											.onField( "tags" )
											.matching( tag );
								}
							} );
						}
					} );
					// Spatial query
					// TODO spatial query
					/*
					b.must( ctx -> {
						if ( myLocation != null && maxDistanceInKilometers != null ) {
							ctx.spatial()
									.onField( "copies.library.location" )
									.within( maxDistanceInKilometers, DistanceUnit.KM )
									.of( myLocation );
						}
					} );
					*/
					// Nested query + must loop
					b.must( ctx -> {
						if ( libraryServices != null && !libraryServices.isEmpty() ) {
							ctx.nested().onObjectField( "copies" )
									.bool().must( c2 -> {
								for ( LibraryService service : libraryServices ) {
									c2.match()
											.onField( "copies.library.services" )
											.matching( service );
								}
							} );
						}
					} );
				} )
				// TODO facets (tag, medium, library in particular)
				.sort().byScore().end()
				.build();

		query.setFirstResult( offset );
		query.setMaxResults( limit );

		return query.getResultList();
	}

}
