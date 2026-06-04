-- +goose Up
-- +goose StatementBegin
ALTER TABLE customers ALTER COLUMN birthday DROP NOT NULL;
-- +goose StatementEnd

-- +goose Down
-- +goose StatementBegin
ALTER TABLE customers ALTER COLUMN birthday SET NOT NULL;
-- +goose StatementEnd
