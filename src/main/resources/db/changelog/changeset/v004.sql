create table if not exists payments
(
    id uuid primary key,
    order_id uuid,
    amount bigint,
    status varchar,
    created_at timestamp
);