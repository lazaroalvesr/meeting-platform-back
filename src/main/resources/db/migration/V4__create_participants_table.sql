CREATE TABLE participants (
                              id UUID PRIMARY KEY,
                              room_id UUID NOT NULL,
                              display_name VARCHAR(80) NOT NULL,
                              role VARCHAR(20) NOT NULL,
                              joined_at TIMESTAMPTZ NOT NULL,
                              left_at TIMESTAMPTZ,

                              CONSTRAINT fk_participants_room
                                  FOREIGN KEY (room_id)
                                      REFERENCES rooms (id)
                                      ON DELETE CASCADE,

                              CONSTRAINT ck_participants_role
                                  CHECK (role IN ('HOST', 'GUEST'))
);

CREATE INDEX idx_participants_room_id
    ON participants (room_id);