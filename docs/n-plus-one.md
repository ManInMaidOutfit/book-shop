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

Точка отсчёта в истории: коммит 2079b14.

Эксперимент 1: @EntityGraph - 7 -> 1
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

Коммит: 72d4dd6.

Эксперимент 2: @Query + JOIN FETCH - 7 -> 1
```java
@Query("SELECT DISTINCT p FROM Publisher p LEFT JOIN FETCH p.books")
List<Publisher> findAllWithJoinFetch();
```
Результат: 1 запрос на всю демо-фазу. Книги те же: 3, 3, 3, 3, 0, 0.

Наблюдения:
- JOIN FETCH явно указывает Hibernate инициализировать коллекцию в том же запросе.
- Ключевое слово DISTINCT критически важно: без него Hibernate может вернуть дубликаты корневых сущностей в списке (по одной на каждую дочернюю строку), которые затем будут отфильтрованы в памяти, что неэффективно.
- В отличие от @EntityGraph, этот подход позволяет гибко настраивать условия (например, добавить WHERE p.name = :name), но требует написания JPQL вручную.

Коммит: 34aa507.

Ветка fail/eager-everything: глобальный EAGER

Идея: решить все проблемы с ленивой загрузкой, пометив все связи как FetchType.EAGER
и добавив @Fetch(FetchMode.JOIN) для принудительной оптимизации.
```java
// Publisher.java
@OneToMany(mappedBy = "publisher", fetch = FetchType.EAGER)
@org.hibernate.annotations.Fetch(org.hibernate.annotations.FetchMode.JOIN)
private List<Book> books = new ArrayList<>();

// Book.java
@ManyToMany(fetch = FetchType.EAGER)
@JoinTable(name = "book_author", ...)
@org.hibernate.annotations.Fetch(org.hibernate.annotations.FetchMode.JOIN)
private List<Author> authors = new ArrayList<>();
```
Сценарий: publisherRepository.findAllWithJoinFetch(), затем итерация, затем bookRepository.findAll().

Результат: 1 запрос на издателей с книгами + 12 отдельных SELECT-запросов на авторов + 1 запрос на книги.
Итого: 14 запросов вместо ожидаемого 1-2.

-- Запрос 1: издатели + книги (JOIN FETCH сработал)
select distinct p1_0.id, ... from publisher p1_0 left join book b1_0 ...

-- Запросы 2-13: Hibernate проигнорировал @Fetch(JOIN) для authors
select a1_0.book_id, ... from book_author a1_0 join author a1_1 ... where a1_0.book_id=?
-- (повторяется 12 раз)

-- Запрос 14: книги (без JOIN на авторов, т.к. они уже в L1-кэше)
select b1_0.id, ... from book b1_0

Наблюдения:
- Hibernate молча проигнорировал @Fetch(JOIN) для вложенной коллекции Book.authors,
чтобы избежать декартова произведения трёх таблиц (Publisher × Book × Author);
- вместо ожидаемого одного большого JOIN получилась классическая N+1-атака на 12 запросов;
- последующий bookRepository.findAll() не породил новых запросов к авторам —
все данные уже были в Persistence Context (L1-кэш), что маскирует проблему;
- разработчик полностью теряет контроль над генерируемым SQL.

Вывод: FetchType.EAGER — антипаттерн. Он делает поведение непредсказуемым:
вместо гарантированного JOIN можно получить скрытый N+1, который трудно диагностировать.
Управление загрузкой должно быть явным и локальным (через @EntityGraph или JOIN FETCH
в конкретных методах), а не глобальным.

Коммит: `ff4daa0`.

Ветка fail/join-fetch-two-bags: краш при двух List-коллекциях

Идея: загрузить книгу сразу с авторами и жанрами через один JOIN FETCH-запрос.

```java
@Query("SELECT DISTINCT b FROM Book b " +
       "LEFT JOIN FETCH b.authors " +
       "LEFT JOIN FETCH b.genres")
List<Book> findAllWithAuthorsAndGenres();
```
Результат: приложение падает при запуске с исключением:
org.hibernate.loader.MultipleBagFetchException: cannot simultaneously fetch multiple bags: 
[com.example.book_shop.entity.Book.authors, com.example.book_shop.entity.Book.genres]

Наблюдения:
- Hibernate не может выполнить JOIN FETCH для двух List-коллекций одновременно;
- причина: List в Hibernate — это "bag" (неупорядоченная коллекция с дубликатами);
- JOIN двух bag-коллекций даёт декартово произведение: книга × авторы × жанры;
- Hibernate физически не может корректно распаковать результат обратно в два List;
- исключение выбрасывается на этапе создания запроса, до выполнения SQL.

Вывод: нельзя делать JOIN FETCH для нескольких List-коллекций. Решения:
Заменить List на Set (тогда Hibernate использует Set-семантику и может обработать дубликаты);
Делать два последовательных запроса: сначала загрузить книги с авторами, потом с жанрами;
Использовать @BatchSize для пакетной загрузки.

Коммит: `acc8df0`.

## Ветка fail/join-fetch-pagination: пагинация в памяти

**Идея:** получить страницу данных (например, 2 издателя) вместе с их книгами, используя `Pageable` и `JOIN FETCH`.

```java
@Query("SELECT DISTINCT p FROM Publisher p LEFT JOIN FETCH p.books")
Page<Publisher> findAllWithBooksPaged(Pageable pageable);

// Вызов:
publisherRepository.findAllWithBooksPaged(PageRequest.of(0, 2));
```
Результат: Запрошено 2 издателя, фактически получено 6 (все).

Наблюдения:
- В сгенерированном SQL-запросе для выборки данных отсутствует LIMIT или OFFSET.
- Hibernate намеренно игнорирует пагинацию на уровне базы данных при наличии collection fetch, чтобы не вернуть "обрезанные" коллекции (например, издателя только с частью его книг).
- Вместо этого данные загружаются целиком в память Java, где и применяется пагинация (или результат искажается из-за count-запроса).
- В старых версиях это сопровождалось предупреждением HHH000104: firstResult/maxResults specified with collection fetch; applying in memory!.

Вывод: Нельзя комбинировать JOIN FETCH коллекций с Pageable.
Правильное решение: делать два отдельных запроса (один для пагинированного списка ID/сущностей, второй для загрузки коллекций через @BatchSize или IN), либо использовать DTO-проекции.

Коммит: `a0b5623`.

```markdown
| Вариант | Запросов (6 издателей) | Статус |
| :--- | :--- | :--- |
| Baseline | 7 | точка отсчёта |
| @EntityGraph | 1 | работает |
| @Query + JOIN FETCH | 1 | работает |
| fail/eager-everything | 14 | провал: скрытый N+1 |
| fail/join-fetch-two-bags | Краш приложения | MultipleBagFetchException |
| fail/join-fetch-pagination | Краш/Неверный результат | Игнорирование LIMIT, загрузка в память |
```