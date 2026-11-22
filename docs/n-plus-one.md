# Лаборатория N+1

Учебный проект `book-shop`: одну и ту же выборку данных загружаем разными способами
и считаем, сколько SQL-запросов уходит в базу. Цель - увидеть проблему N+1 своими
глазами, освоить способы её решения и честно задокументировать варианты, которые ломаются.

## Теория: что такое N+1

Приложение делает 1 запрос за списком сущностей, а затем по одному запросу за коллекцию
каждой сущности - итого 1 + N запросов. На нашем стенде: 1 запрос за издателями +
6 запросов за книгами каждого издателя = 7 запросов.

## Стенд

| Компонент | Значение |
|---|---|
| Java | 17 |
| Spring Boot | 4.2.0 |
| Hibernate | 7.4.15.Final (сверь со строкой "Hibernate ORM core version" в логе запуска) |
| База | H2, in-memory, create-drop (пересоздаётся при каждом запуске) |
| SQL в консоль | show-sql=true, format_sql=true |
| Статистика | generate_statistics=true |
| OSIV | выключен (spring.jpa.open-in-view=false) |

Данные: 6 издателей - у Эксмо, АСТ, Альпина, МИФ по 3 книги (всего 12),
HarperCollins и Penguin без книг.
Демо: `NPlusOneDemo` (CommandLineRunner, @Transactional): `publisherRepository.findAll()`,
затем `getBooks().size()` у каждого.

## Baseline: ничего не предпринимаем - 7 запросов

Репозиторий пустой, коллекция LAZY (умолчание для @OneToMany):
```java
    public interface PublisherRepository extends JpaRepository<Publisher, Long> {
    }
```
select
        p1_0.id,
        p1_0.country,
        p1_0.name 
    from
        publisher p1_0
select
        b1_0.publisher_id,
        b1_0.id,
        b1_0.isbn,
        b1_0.published_at,
        b1_0.title 
    from
        book b1_0 
    where
        b1_0.publisher_id=?
select
        b1_0.publisher_id,
        b1_0.id,
        b1_0.isbn,
        b1_0.published_at,
        b1_0.title 
    from
        book b1_0 
    where
        b1_0.publisher_id=?

Разбор: 1 (издатели) + 6 (книги каждого издателя) = 7.
Интересно: 2 из 6 запросов ушли на HarperCollins и Penguin - издателей БЕЗ книг.
Пустая LAZY-коллекция тоже стоит SELECT: Hibernate обязан сходить в базу и убедиться, что там пусто.

Точка отсчёта в истории: коммит `2079b14`.

## Эксперимент 1: @EntityGraph - 7 -> 1
```java
    @Override
    @EntityGraph(attributePaths = "books")
    List<Publisher> findAll();
```

select
        p1_0.id,
        b1_0.publisher_id,
        b1_0.id,
        b1_0.isbn,
        b1_0.published_at,
        b1_0.title,
        p1_0.country,
        p1_0.name 
    from
        publisher p1_0 
    left join
        book b1_0 
            on p1_0.id=b1_0.publisher_id

Результат: 1 запрос на всю демо-фазу. Книги те же: 3, 3, 3, 3, 0, 0.

Наблюдения:
- join LEFT - издатели без книг не выпали из выборки;
- размножение строк: SQL вернул 14 строк (12 пар "издатель+книга" и 2 строки с NULL),
  Hibernate свернул их в 6 объектов Publisher;
- @EntityGraph - "eager по требованию": меняет один конкретный запрос, а не сущность.

Коммит: `72d4dd6`.

## Таблица результатов

| Вариант | Запросов (6 издателей) | Статус |
|---|---|---|
| Baseline | 7 | точка отсчёта |
| @EntityGraph | 1 | работает |

## Дорожная карта

- [ ] @Query + JOIN FETCH
- [ ] @BatchSize и hibernate.default_batch_fetch_size
- [ ] Ветки fail/*: eager-everything; JOIN FETCH двух List сразу (MultipleBagFetchException);
      JOIN FETCH + Pageable (HHH000104); EntityGraph + @Query
- [ ] DTO-проекции
- [ ] @DataJpaTest + Hibernate Statistics
- [ ] Этап 2: Testcontainers + PostgreSQL
```
