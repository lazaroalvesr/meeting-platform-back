ALTER TABLE projects
    ADD COLUMN maintenance_start_date DATE;

ALTER TABLE payments
    ADD COLUMN reference_month DATE;

CREATE UNIQUE INDEX ux_payments_monthly_maintenance_reference
    ON payments (project_id, reference_month)
    WHERE payment_type = 'MONTHLY_MAINTENANCE'
      AND reference_month IS NOT NULL;
