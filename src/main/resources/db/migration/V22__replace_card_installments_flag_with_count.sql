ALTER TABLE payments
    DROP COLUMN allows_card_installments;

ALTER TABLE payments
    ADD COLUMN card_installment_count INTEGER;
