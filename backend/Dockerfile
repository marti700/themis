FROM golang:1.26-bookworm

RUN go install github.com/pressly/goose/v3/cmd/goose@latest

WORKDIR /workspace/backend

CMD ["bash", "-c", "goose -dir /workspace/backend/internal/database/migrations postgres $DB_DSN up && go run ."]
