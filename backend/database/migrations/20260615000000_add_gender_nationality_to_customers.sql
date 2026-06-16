-- +goose Up
-- +goose StatementBegin
CREATE TYPE gender AS ENUM ('femenin', 'masculin');

ALTER TABLE customers
    ADD COLUMN gender gender,
    ADD COLUMN nationality VARCHAR(100);
-- +goose StatementEnd

-- +goose Down
-- +goose StatementBegin
ALTER TABLE customers
    DROP COLUMN gender,
    DROP COLUMN nationality;

DROP TYPE IF EXISTS gender;
-- +goose StatementEnd
