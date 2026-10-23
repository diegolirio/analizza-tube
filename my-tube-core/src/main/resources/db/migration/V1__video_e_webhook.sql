create table video (
    id         uuid        primary key,
    created_at timestamptz not null
);

create table webhook_receipt (
    delivery_id varchar(100) primary key,
    event_type  varchar(50)  not null,
    video_id    uuid,
    received_at timestamptz  not null
);
