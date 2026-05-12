create table if not exists customers
(
    id uuid primary key,
    name varchar,
    email varchar,
    company_name varchar,
    created_at timestamp
);

