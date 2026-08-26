create table if not exists orders
(
    id uuid primary key,
    customer_id uuid,
    status varchar,
    total_amount numeric,
    created_at timestamp
);