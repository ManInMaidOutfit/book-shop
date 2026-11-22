package com.example.book_shop.config;

import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.example.book_shop.entity.Author;
import com.example.book_shop.entity.Book;
import com.example.book_shop.entity.Genre;
import com.example.book_shop.entity.Publisher;
import com.example.book_shop.repository.AuthorRepository;
import com.example.book_shop.repository.BookRepository;
import com.example.book_shop.repository.GenreRepository;
import com.example.book_shop.repository.PublisherRepository;

@Component 
@Order(1)
public class DataSeeder implements CommandLineRunner 
{
    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class.getName());
    private final PublisherRepository publisherRepository;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;
    private final BookRepository bookRepository;

    public DataSeeder(PublisherRepository p, AuthorRepository a,
                      GenreRepository g, BookRepository b) {
        this.publisherRepository = p;
        this.authorRepository = a;
        this.genreRepository = g;
        this.bookRepository = b;
    }

    @Override 
    public void run(String... args)
    {
        if(bookRepository.count() > 0)
        {
            logger.info("База не пуста — сид пропущен");
            return;
        }

        // === Издатели ===
        Publisher eksmo = new Publisher();
        eksmo.setName("Эксмо");
        eksmo.setCountry("Россия");
        publisherRepository.save(eksmo);

        Publisher ast = new Publisher();
        ast.setName("АСТ");
        ast.setCountry("Россия");
        publisherRepository.save(ast);

        Publisher alpin = new Publisher();
        alpin.setName("Альпина Паблишер");
        alpin.setCountry("Россия");
        publisherRepository.save(alpin);

        Publisher mif = new Publisher();
        mif.setName("Манн, Иванов и Фербер");
        mif.setCountry("Россия");
        publisherRepository.save(mif);

        Publisher harperCollins = new Publisher();
        harperCollins.setName("HarperCollins");
        harperCollins.setCountry("США");
        publisherRepository.save(harperCollins);

        Publisher penguin = new Publisher();
        penguin.setName("Penguin Books");
        penguin.setCountry("Великобритания");
        publisherRepository.save(penguin);

        // === Авторы ===
        Author arkady = new Author();
        arkady.setName("Аркадий");
        arkady.setSurname("Стругацкий");
        arkady.setYearOfBirth(1925);
        authorRepository.save(arkady);

        Author boris = new Author();
        boris.setName("Борис");
        boris.setSurname("Стругацкий");
        boris.setYearOfBirth(1933);
        authorRepository.save(boris);

        Author dostoevsky = new Author();
        dostoevsky.setName("Фёдор");
        dostoevsky.setSurname("Достоевский");
        dostoevsky.setYearOfBirth(1821);
        authorRepository.save(dostoevsky);

        Author kahneman = new Author();
        kahneman.setName("Даниэль");
        kahneman.setSurname("Канеман");
        kahneman.setYearOfBirth(1934);
        authorRepository.save(kahneman);

        Author pelevin = new Author();
        pelevin.setName("Виктор");
        pelevin.setSurname("Пелевин");
        pelevin.setYearOfBirth(1962);
        authorRepository.save(pelevin);

        Author tolkien = new Author();
        tolkien.setName("Джон");
        tolkien.setSurname("Толкин");
        tolkien.setYearOfBirth(1892);
        authorRepository.save(tolkien);

        Author doyle = new Author();
        doyle.setName("Артур");
        doyle.setSurname("Конан Дойл");
        doyle.setYearOfBirth(1859);
        authorRepository.save(doyle);

        Author orwell = new Author();
        orwell.setName("Джордж");
        orwell.setSurname("Оруэлл");
        orwell.setYearOfBirth(1903);
        authorRepository.save(orwell);

        Author goldratt = new Author();
        goldratt.setName("Элияху");
        goldratt.setSurname("Голдратт");
        goldratt.setYearOfBirth(1947);
        authorRepository.save(goldratt);

        Author cialdini = new Author();
        cialdini.setName("Роберт");
        cialdini.setSurname("Чалдини");
        cialdini.setYearOfBirth(1945);
        authorRepository.save(cialdini);

        // === Жанры ===
        Genre fantasy = new Genre();
        fantasy.setName("Фантастика");
        genreRepository.save(fantasy);

        Genre classic = new Genre();
        classic.setName("Классика");
        genreRepository.save(classic);

        Genre detective = new Genre();
        detective.setName("Детектив");
        genreRepository.save(detective);

        Genre nonfiction = new Genre();
        nonfiction.setName("Научпоп");
        genreRepository.save(nonfiction);

        Genre business = new Genre();
        business.setName("Бизнес");
        genreRepository.save(business);

        Genre highFantasy = new Genre();
        highFantasy.setName("Фэнтези");
        genreRepository.save(highFantasy);

        Genre dystopia = new Genre();
        dystopia.setName("Антиутопия");
        genreRepository.save(dystopia);

        Genre psychology = new Genre();
        psychology.setName("Психология");
        genreRepository.save(psychology);

        // === Книги (12 штук) ===

        Book roadsidePicnic = new Book();
        roadsidePicnic.setTitle("Пикник на обочине");
        roadsidePicnic.setPublishedAt(LocalDate.of(1972, 1, 1));
        roadsidePicnic.setISBN("978-5-17-090301-1");
        roadsidePicnic.setPublisher(eksmo);
        roadsidePicnic.addAuthor(arkady);
        roadsidePicnic.addAuthor(boris);
        roadsidePicnic.addGenre(fantasy);
        bookRepository.save(roadsidePicnic);

        Book lameFate = new Book();
        lameFate.setTitle("Хромая судьба");
        lameFate.setPublishedAt(LocalDate.of(1986, 1, 1));
        lameFate.setISBN("978-5-04-098379-7");
        lameFate.setPublisher(mif);
        lameFate.addAuthor(arkady);
        lameFate.addAuthor(boris);
        lameFate.addGenre(fantasy);
        bookRepository.save(lameFate);

        Book inhabitedIsland = new Book();
        inhabitedIsland.setTitle("Обитаемый остров");
        inhabitedIsland.setPublishedAt(LocalDate.of(1969, 1, 1));
        inhabitedIsland.setISBN("978-5-17-084652-3");
        inhabitedIsland.setPublisher(ast);
        inhabitedIsland.addAuthor(arkady);
        inhabitedIsland.addAuthor(boris);
        inhabitedIsland.addGenre(fantasy);
        bookRepository.save(inhabitedIsland);

        Book crimeAndPunishment = new Book();
        crimeAndPunishment.setTitle("Преступление и наказание");
        crimeAndPunishment.setPublishedAt(LocalDate.of(1866, 1, 1));
        crimeAndPunishment.setISBN("978-5-389-07633-4");
        crimeAndPunishment.setPublisher(ast);
        crimeAndPunishment.addAuthor(dostoevsky);
        crimeAndPunishment.addGenre(classic);
        crimeAndPunishment.addGenre(detective);
        bookRepository.save(crimeAndPunishment);

        Book brothersKaramazov = new Book();
        brothersKaramazov.setTitle("Братья Карамазовы");
        brothersKaramazov.setPublishedAt(LocalDate.of(1880, 1, 1));
        brothersKaramazov.setISBN("978-5-389-07634-1");
        brothersKaramazov.setPublisher(eksmo);
        brothersKaramazov.addAuthor(dostoevsky);
        brothersKaramazov.addGenre(classic);
        bookRepository.save(brothersKaramazov);

        Book thinkingFast = new Book();
        thinkingFast.setTitle("Думай медленно, решай быстро");
        thinkingFast.setPublishedAt(LocalDate.of(2011, 1, 1));
        thinkingFast.setISBN("978-5-17-080053-7");
        thinkingFast.setPublisher(alpin);
        thinkingFast.addAuthor(kahneman);
        thinkingFast.addGenre(nonfiction);
        thinkingFast.addGenre(business);
        thinkingFast.addGenre(psychology);
        bookRepository.save(thinkingFast);

        Book generationP = new Book();
        generationP.setTitle("Generation «П»");
        generationP.setPublishedAt(LocalDate.of(1999, 1, 1));
        generationP.setISBN("978-5-17-093440-8");
        generationP.setPublisher(eksmo);
        generationP.addAuthor(pelevin);
        generationP.addGenre(fantasy);
        generationP.addGenre(classic);
        bookRepository.save(generationP);

        Book lotr = new Book();
        lotr.setTitle("Властелин колец");
        lotr.setPublishedAt(LocalDate.of(1954, 1, 1));
        lotr.setISBN("978-5-17-084378-5");
        lotr.setPublisher(ast);
        lotr.addAuthor(tolkien);
        lotr.addGenre(highFantasy);
        bookRepository.save(lotr);

        Book sherlockHolmes = new Book();
        sherlockHolmes.setTitle("Приключения Шерлока Холмса");
        sherlockHolmes.setPublishedAt(LocalDate.of(1892, 1, 1));
        sherlockHolmes.setISBN("978-5-04-098380-3");
        sherlockHolmes.setPublisher(mif);
        sherlockHolmes.addAuthor(doyle);
        sherlockHolmes.addGenre(detective);
        sherlockHolmes.addGenre(classic);
        bookRepository.save(sherlockHolmes);

        Book nineteen84 = new Book();
        nineteen84.setTitle("1984");
        nineteen84.setPublishedAt(LocalDate.of(1949, 1, 1));
        nineteen84.setISBN("978-5-17-080262-8");
        nineteen84.setPublisher(alpin);
        nineteen84.addAuthor(orwell);
        nineteen84.addGenre(dystopia);
        nineteen84.addGenre(classic);
        bookRepository.save(nineteen84);

        Book theGoal = new Book();
        theGoal.setTitle("Цель");
        theGoal.setPublishedAt(LocalDate.of(1984, 1, 1));
        theGoal.setISBN("978-5-98124-520-5");
        theGoal.setPublisher(mif);
        theGoal.addAuthor(goldratt);
        theGoal.addGenre(business);
        bookRepository.save(theGoal);

        Book influence = new Book();
        influence.setTitle("Психология влияния");
        influence.setPublishedAt(LocalDate.of(1984, 1, 1));
        influence.setISBN("978-5-4461-0031-0");
        influence.setPublisher(alpin);
        influence.addAuthor(cialdini);
        influence.addGenre(psychology);
        influence.addGenre(nonfiction);
        bookRepository.save(influence);
        
        logger.info("Сидирование данных успешно завершено. Добавлено 12 книг.");
    }
}