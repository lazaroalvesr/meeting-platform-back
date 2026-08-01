ALTER TABLE clients
    ADD COLUMN asaas_customer_id VARCHAR(64);

ALTER TABLE projects
    ADD COLUMN asaas_subscription_id VARCHAR(64);

ALTER TABLE payments
    ADD COLUMN asaas_payment_id VARCHAR(64);

CREATE UNIQUE INDEX ux_clients_asaas_customer_id
    ON clients (asaas_customer_id)
    WHERE asaas_customer_id IS NOT NULL;

CREATE UNIQUE INDEX ux_projects_asaas_subscription_id
    ON projects (asaas_subscription_id)
    WHERE asaas_subscription_id IS NOT NULL;

CREATE UNIQUE INDEX ux_payments_asaas_payment_id
    ON payments (asaas_payment_id)
    WHERE asaas_payment_id IS NOT NULL;
