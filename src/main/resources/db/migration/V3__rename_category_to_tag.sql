ALTER TABLE category RENAME TO tag;

ALTER TABLE transaction_category RENAME TO transaction_tag;

ALTER TABLE transaction_tag RENAME COLUMN category_id TO tag_id;
