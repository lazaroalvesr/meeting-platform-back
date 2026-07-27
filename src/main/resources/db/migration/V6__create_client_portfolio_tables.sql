CREATE TABLE clients (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    name VARCHAR(120) NOT NULL,
    company_name VARCHAR(160),
    email VARCHAR(255),
    phone VARCHAR(30),
    document VARCHAR(30),
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_clients_owner
        FOREIGN KEY (owner_id) REFERENCES users (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_clients_owner_id ON clients (owner_id);

CREATE TABLE projects (
    id UUID PRIMARY KEY,
    client_id UUID NOT NULL,
    owner_id UUID NOT NULL,
    name VARCHAR(160) NOT NULL,
    project_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    scope TEXT,
    total_value NUMERIC(12, 2),
    contract_status VARCHAR(30) NOT NULL,
    contract_url VARCHAR(2048),
    maintenance_active BOOLEAN NOT NULL DEFAULT FALSE,
    maintenance_monthly_value NUMERIC(12, 2),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_projects_client
        FOREIGN KEY (client_id) REFERENCES clients (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_projects_owner
        FOREIGN KEY (owner_id) REFERENCES users (id)
        ON DELETE CASCADE,
    CONSTRAINT ck_projects_type
        CHECK (project_type IN (
            'LANDING_PAGE', 'INSTITUTIONAL_WEBSITE', 'ECOMMERCE',
            'WEB_SYSTEM', 'MAINTENANCE', 'OTHER'
        )),
    CONSTRAINT ck_projects_status
        CHECK (status IN (
            'LEAD', 'PLANNING', 'DESIGN', 'DEVELOPMENT',
            'REVIEW', 'DELIVERED', 'MAINTENANCE', 'CANCELLED'
        )),
    CONSTRAINT ck_projects_contract_status
        CHECK (contract_status IN ('NOT_STARTED', 'DRAFT', 'SENT', 'SIGNED'))
);

CREATE INDEX idx_projects_client_id ON projects (client_id);
CREATE INDEX idx_projects_owner_id ON projects (owner_id);

CREATE TABLE payments (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    description VARCHAR(160) NOT NULL,
    payment_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    due_date DATE NOT NULL,
    paid_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_payments_project
        FOREIGN KEY (project_id) REFERENCES projects (id)
        ON DELETE CASCADE,
    CONSTRAINT ck_payments_type
        CHECK (payment_type IN ('PROJECT', 'MONTHLY_MAINTENANCE', 'OTHER')),
    CONSTRAINT ck_payments_status
        CHECK (status IN ('PENDING', 'PAID', 'OVERDUE', 'CANCELLED'))
);

CREATE INDEX idx_payments_project_id ON payments (project_id);
CREATE INDEX idx_payments_due_date ON payments (due_date);

ALTER TABLE rooms
    ADD COLUMN project_id UUID,
    ADD CONSTRAINT fk_rooms_project
        FOREIGN KEY (project_id) REFERENCES projects (id)
        ON DELETE SET NULL;
