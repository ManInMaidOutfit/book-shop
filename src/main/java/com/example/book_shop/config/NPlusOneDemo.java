package com.example.book_shop.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.book_shop.entity.Book;
import com.example.book_shop.entity.Publisher;
import com.example.book_shop.repository.BookRepository;
import com.example.book_shop.repository.PublisherRepository;

@Component 
@Order(2) 
public class NPlusOneDemo implements CommandLineRunner
{
    private static final Logger logger = LoggerFactory.getLogger(NPlusOneDemo.class);

    public final PublisherRepository publisherRepository;
    public final BookRepository bookRepository;

    public NPlusOneDemo(PublisherRepository publisherRepository, BookRepository bookRepository)
    {
        this.publisherRepository = publisherRepository;
        this.bookRepository = bookRepository;
    }

    @Override 
    @Transactional 
    public void run(String...args)
    {
        List<Book> books = bookRepository.findAllWithAuthorsAndGenres();  
        logger.info("Найдено книг: {}", books.size());

        for(Book book : books)
        {
            int authors = book.getAuthors().size();
            int genres = book.getGenres().size();
            logger.info("\"{}\" — {} авторов, {} жанров", book.getTitle(), authors, genres);
        }
    }
}
