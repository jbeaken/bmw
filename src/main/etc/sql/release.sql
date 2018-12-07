alter table category_categories change column category category_id bigint(20);
alter table category_categories change column categories categories_id bigint(20);

alter table order_line change column stock_item stock_item_id bigint(20);

alter table reading_list_stock_items change column reading_list reading_list_id bigint(20);
alter table reading_list_stock_items change column stock_items stock_items_id bigint(20);
