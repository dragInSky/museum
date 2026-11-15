# Музей

Тема №17. Java 17, Spring Boot 3.5, Spring Data JPA, Hibernate, PostgreSQL 17.

## Запуск

Нужен Docker. Из папки проекта:

```bash
docker compose up --build
```

Результаты будут в консоли. После `Демо завершено` можно нажать Ctrl+C.
Данные сохраняются, при повторном запуске не дублируются.

## Проверка задания

1. **Тема:** №17 «Музей», в выданном Excel была свободна. Фамилию в общей таблице нужно вписать отдельно.
2. **Сущности:** `Hall`, `Exhibit`, `CatalogCard`, `Tour`, `TourStop`. Проверить блоки в консоли:
   - `1:N`: «Археология» — амфора и монета, «Живопись» — пейзаж. В коде — `Hall.exhibits` с `mappedBy` и `Exhibit.hall` с `@ManyToOne` и `@JoinColumn`.
   - `N:M`: две экскурсии по две остановки, амфора в обеих. `TourStop` содержит две `@ManyToOne` и номер остановки `position`.
   - `1:1`: три экспоната с разными карточками `M-001`, `M-002`, `M-003`. Связь — `Exhibit.card` с `@OneToOne`.
3. **Repository:** у каждой сущности свой `JpaRepository`. В `ExhibitRepository`:
   - `findByNameContainingIgnoreCaseOrderById` — запрос из имени метода, блок «Запрос из имени метода: мон» дает монету.
   - `findByHall` — JPQL, блок «JPQL: Археология» дает амфору и монету.
   - `findSince` — SQL с `nativeQuery = true`, блок «SQL: год от 1600» дает монету и пейзаж.
4. **Service:** транзакции на методах `MuseumService`. В `search` — Criteria API (`CriteriaBuilder`, `Root`, `Predicate`). Пустые фильтры пропускаются. В демо без фильтров будет три экспоната, с залом «Археология» и годом от 1000 — монета.
5. **Демонстрация:** все блоки запускает `DemoRunner`. Тесты находятся в `MuseumTest`.

Свои фильтры — любой можно убрать. В блоке `Criteria: свои фильтры` будет монета:

```bash
docker compose run --rm app --name=мон --hall=Археология --year-from=1600
```

Тесты проверяют связи, запросы, пустые фильтры и звёздочку:

```bash
docker compose run --rm tests
```

Ожидается `Tests run: 8, Failures: 0, Errors: 0`.

## Звёздочка

Выполнена. В строке `Звёздочка: MappingCheck[...]` все значения должны быть `true`.
Проверка использует временные записи и удаляет их после завершения.

| Пункт | Где проверить |
| --- | --- |
| Все связи `LAZY` | `Hall`, `Exhibit`, `TourStop` |
| Загрузка связей | `@EntityGraph` в репозиториях, `fetch` в `search` |
| Каскады без `ALL` | `Exhibit.card`: `PERSIST`, `MERGE`; `cascade=true` в демо |
| `orphanRemoval` | `Exhibit.card`; `orphanRemoval=true` в демо |
| Обе стороны связи | `Hall.addExhibit`, тест `movingExhibit` |
| `SEQUENCE` | `BaseEntity`, `museum_seq`, тест `databaseSchema` |
| `@Version` | `Exhibit.version`, конфликт правок в тесте `optimisticLock` |
| `equals/hashCode` | `BaseEntity`: тип и id, стабильный хеш; `equality=true` в демо |
| DTO | `MuseumService` возвращает `ExhibitView`, `MappingCheck` и строки |
| `open-in-view: false` | `application.yaml` |
| `readOnly = true` | Методы чтения в `MuseumService` |
| Flyway | `db/migration/V1__museum.sql`, `ddl-auto: validate`, тест `databaseSchema` |

Остановить контейнеры: `docker compose down`.
