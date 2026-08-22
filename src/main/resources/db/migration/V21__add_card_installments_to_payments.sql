ALTER TABLE payments
    ADD COLUMN allows_card_installments BOOLEAN NOT NULL DEFAULT FALSE;
