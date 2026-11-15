create sequence if not exists museum_seq start with 1 increment by 1;

create table if not exists hall (
    id bigint primary key,
    name varchar(255) not null unique
);

create table if not exists catalog_card (
    id bigint primary key,
    number varchar(255) not null unique
);

create table if not exists exhibit (
    id bigint primary key,
    name varchar(255) not null,
    year integer not null,
    hall_id bigint not null references hall(id),
    card_id bigint not null unique references catalog_card(id)
);

create table if not exists tour (
    id bigint primary key,
    name varchar(255) not null unique
);

create table if not exists tour_stop (
    id bigint primary key,
    tour_id bigint not null references tour(id),
    exhibit_id bigint not null references exhibit(id),
    position integer not null,
    unique (tour_id, exhibit_id),
    unique (tour_id, position)
);

alter table hall alter column id drop identity if exists;
alter table catalog_card alter column id drop identity if exists;
alter table exhibit alter column id drop identity if exists;
alter table tour alter column id drop identity if exists;
alter table tour_stop alter column id drop identity if exists;
alter table exhibit add column if not exists version bigint not null default 0;

select setval('museum_seq', greatest(coalesce(max(id), 0), 1), max(id) is not null)
from (
    select id from hall
    union all select id from catalog_card
    union all select id from exhibit
    union all select id from tour
    union all select id from tour_stop
) ids;
