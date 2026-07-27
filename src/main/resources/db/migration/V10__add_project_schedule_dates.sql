ALTER TABLE projects
    ADD COLUMN start_date DATE,
    ADD COLUMN delivery_date DATE;

UPDATE projects
SET start_date = created_at::date
WHERE start_date IS NULL;

ALTER TABLE projects
    ALTER COLUMN start_date SET NOT NULL;
