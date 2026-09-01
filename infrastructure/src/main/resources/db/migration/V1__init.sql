create table categories (
    id   serial primary key,
    name varchar(100) not null
);

create table products (
    id            serial primary key,
    name          varchar(150) not null,
    description   varchar(1000),
    category_id   int not null references categories (id),
    unit          varchar(20) not null,
    price         numeric(18, 2) not null,
    stock_qty     int not null,
    is_available  boolean not null,
    image_url     varchar(500)
);
create index ix_products_name on products (name);

create table customers (
    id                    serial primary key,
    name                  varchar(150) not null,
    phone                 varchar(30) not null,
    email                 varchar(256) not null unique,
    password_hash         varchar(512) not null,
    is_admin              boolean not null default false,
    photo_url             varchar(500),
    must_change_password  boolean not null default false
);

create table admins (
    id             serial primary key,
    name           varchar(150) not null,
    email          varchar(256) not null unique,
    password_hash  varchar(512) not null,
    photo_url      varchar(500),
    created_at     timestamptz not null
);

create table orders (
    id                serial primary key,
    customer_id       int not null references customers (id),
    status            varchar(30) not null,
    delivery_address  varchar(500) not null,
    phone             varchar(30) not null,
    delivery_note     varchar(500),
    subtotal          numeric(18, 2) not null,
    delivery_fee      numeric(18, 2) not null,
    total             numeric(18, 2) not null,
    created_at        timestamptz not null
);
create index ix_orders_customer_id on orders (customer_id);

create table order_items (
    id                     serial primary key,
    order_id               int not null references orders (id) on delete cascade,
    product_id             int not null references products (id),
    quantity               int not null,
    unit_price_at_order    numeric(18, 2) not null
);
create index ix_order_items_order_id on order_items (order_id);

create table payments (
    id            serial primary key,
    order_id      int not null unique references orders (id) on delete cascade,
    provider      varchar(50) not null,
    reference     varchar(100) not null,
    status        varchar(20) not null,
    amount        numeric(18, 2) not null,
    paid_at       timestamptz,
    refunded_at   timestamptz,
    admin_note    varchar(1000)
);
create unique index ix_payments_reference on payments (reference);

create table profile_change_requests (
    id                serial primary key,
    customer_id       int not null references customers (id),
    field             varchar(20) not null,
    current_value     varchar(256) not null,
    requested_value   varchar(256) not null,
    status            varchar(20) not null,
    created_at        timestamptz not null,
    resolved_at       timestamptz,
    admin_note        varchar(1000)
);

create table chat_messages (
    id            serial primary key,
    customer_id   int not null references customers (id),
    sender_name   varchar(150) not null,
    content       varchar(2000) not null,
    created_at    timestamptz not null
);

create table support_tickets (
    id            serial primary key,
    customer_id   int not null references customers (id),
    subject       varchar(100) not null,
    status        varchar(20) not null,
    created_at    timestamptz not null
);

create table support_messages (
    id           serial primary key,
    ticket_id    int not null references support_tickets (id) on delete cascade,
    sender       varchar(20) not null,
    content      varchar(2000) not null,
    created_at   timestamptz not null
);
