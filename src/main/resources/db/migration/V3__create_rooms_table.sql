CREATE TABLE rooms (
                       id UUID PRIMARY KEY,
                       host_id UUID NOT NULL,
                       slug VARCHAR(16) NOT NULL,
                       title VARCHAR(120) NOT NULL,
                       status VARCHAR(20) NOT NULL,
                       created_at TIMESTAMPTZ NOT NULL,
                       closed_at TIMESTAMPTZ,

                       CONSTRAINT uk_rooms_slug UNIQUE (slug),

                       CONSTRAINT fk_rooms_host
                           FOREIGN KEY (host_id)
                               REFERENCES users (id),

                       CONSTRAINT ck_rooms_status
                           CHECK (status IN ('WAITING', 'ACTIVE', 'CLOSED'))
);

CREATE INDEX idx_rooms_host_id ON rooms (host_id);