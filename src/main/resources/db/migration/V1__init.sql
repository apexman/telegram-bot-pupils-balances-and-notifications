alter user current_user set timezone = 'UTC';

create table if not exists parents
(
    id                  serial primary key,
    created_at          timestamptz not null,
    last_modified_at    timestamptz not null,
    public_id           varchar not null unique
);

create table if not exists students
(
    id                  serial primary key,
    created_at          timestamptz not null,
    last_modified_at    timestamptz not null,
    google_id           varchar not null unique,
    public_id           varchar not null unique,
    full_user_name      varchar not null,
    birthday            date not null,
    date_enrollment     date not null,
    class_num           integer not null,
    hostel              boolean not null,
    discount            numeric(24, 4) not null,
    price               numeric(24, 4) not null,
    currency_name       varchar not null,
    balance             integer not null,
    pause               boolean not null,
    alarm               boolean not null,
    penalty             numeric(24, 4) not null
);

create table if not exists parents_students_relations
(
    id               serial primary key,
    created_at       timestamptz not null default now(),
    last_modified_at timestamptz not null default now(),
    parent_id        integer not null constraint fk_relations_parents references parents,
    student_id         integer not null constraint fk_relations_students references students,
    unique(parent_id, student_id)
);

create table if not exists contacts
(
    id               serial primary key,
    created_at       timestamptz not null,
    last_modified_at timestamptz not null,
    student_id       integer not null constraint fk_contacts_students references students,
    contact_type     varchar not null,
    contact_value    varchar not null,
    unique(student_id, contact_type, contact_value)
);

create table if not exists comments
(
    id               serial primary key,
    created_at       timestamptz not null,
    last_modified_at timestamptz not null,
    student_id         integer not null constraint fk_comments_students references students,
    comment          varchar not null,
    commented_by     varchar
);

create table if not exists alarm_details
(
    id               serial primary key,
    created_at       timestamptz not null,
    last_modified_at timestamptz not null,
    student_id         integer not null constraint fk_alarm_details_students references students,
    details          varchar not null,
    alarmed_by       varchar,
    disabled_at      timestamptz,
    disabled_by      varchar
);

create table if not exists documents
(
    id               serial primary key,
    created_at       timestamptz not null,
    last_modified_at timestamptz not null,
    document_value   bytea not null,
    document_type    varchar not null
);

create table if not exists pending_balance_payments
(
    id               serial primary key,
    created_at       timestamptz not null,
    last_modified_at timestamptz not null,
    parent_id        integer not null constraint fk_pending_balance_payments_parents references parents,
    student_id         integer not null constraint fk_pending_balance_payments_students references students,
    document_id      integer constraint fk_pending_balance_payments_documents references documents
);

create table if not exists balance_payments
(
    id               serial primary key,
    created_at       timestamptz not null,
    last_modified_at timestamptz not null,
    parent_id        integer not null constraint fk_balance_payments_parents references parents,
    student_id         integer not null constraint fk_balance_payments_students references students,
    document_id      integer constraint fk_balance_payments_documents references documents,
    balance          integer not null,
    approved_by      varchar not null,
    comment          varchar
);

create table if not exists penalties
(
    id               serial primary key,
    created_at       timestamptz not null,
    last_modified_at timestamptz not null,
    student_id         integer not null constraint fk_penalties_students references students,
    amount           numeric(24, 4) not null,
    currency_name    varchar not null,
    created_by       varchar
);