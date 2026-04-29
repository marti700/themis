# Load environment variables from .env
ifneq (,$(wildcard ./.env))
    include .env
    export
endif

# Construct the URL. Use default values if the .env variables aren't set.
DB_URL ?= postgres://$(DB_USER):$(DB_PASSWORD)@$(DB_HOST):$(DB_PORT)/$(DB_NAME)?sslmode=disable

# Directory paths
MIGRATIONS_DIR=backend/database/migrations
QUERIES_DIR=backend/database/queries

.PHONY: migrate-up migrate-down sqlc generate help

## help: print this help message
help:
	@echo 'Usage:'
	@sed -n 's/^##//p' ${MAKEFILE_LIST} | column -t -s ':' |  sed -e 's/^/ /'

## migrate-up: apply all forward migrations
migrate-up:
	goose -dir $(MIGRATIONS_DIR) postgres "$(DB_URL)" up

## migrate-down: rollback the last migration
migrate-down:
	goose -dir $(MIGRATIONS_DIR) postgres "$(DB_URL)" down

## migrate-create name=...: create a new sql migration
migrate-create:
	goose -dir $(MIGRATIONS_DIR) postgres "$(DB_URL)" create $(name) sql

## sqlc: generate Go code from SQL
sqlc:
	sqlc -f backend/sqlc.yaml generate

## generate: run both migrations (to sync DB) and sqlc
generate: migrate-up sqlc
	@echo "Schema and Go queries are now in sync!"

## build: compile the backend
build:
	go build -o bin/api main.go