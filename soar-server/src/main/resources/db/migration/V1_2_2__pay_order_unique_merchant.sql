-- createOrder does select-then-insert on (app_id, merchant_order_id), which is not atomic (TOCTOU):
-- two concurrent creates can both pass the existence check and both insert. Promote the non-unique
-- lookup index to UNIQUE so the DB rejects the second insert; createOrder catches that and returns
-- the existing order (idempotent create).
DROP INDEX IF EXISTS idx_pay_order_app_merchant;
CREATE UNIQUE INDEX uk_pay_order_app_merchant ON pay_order (app_id, merchant_order_id);