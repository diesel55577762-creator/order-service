create table if not exists order_item
(
    id uuid primary key,
    order_id uuid,
    product_id uuid,
    quantity bigint
);