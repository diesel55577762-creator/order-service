create table if not exists outbox
(
    id uuid primary key,
    aggregate_id uuid,
    aggregate_type varchar,
    event_type varchar,
    payload varchar,
    status_order_event varchar,
    created_at timestamp
);