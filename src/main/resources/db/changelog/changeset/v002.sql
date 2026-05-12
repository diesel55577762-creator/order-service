create table if not exists orders
(
    id uuid primary key,
    customer uuid,
    status varchar,
    total_amount timestamp,
    created_at timestamp
);