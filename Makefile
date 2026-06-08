.PHONY: help format lint test test-all coverage coverage-check mutation-test integration-test ci local-up local-down otel-up otel-down

help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

format: ## Format code with ktlint
	./gradlew ktlintFormat

lint: ## Run ktlint check
	./gradlew ktlintCheck

test: ## Run unit tests
	./gradlew test

test-all: ## Run all tests (unit + integration)
	./gradlew test integrationTest

coverage: ## Run tests with coverage report
	./gradlew test koverHtmlReport koverXmlReport

coverage-check: ## Verify coverage meets minimum thresholds
	./gradlew test koverVerify

mutation-test: ## Run mutation tests with pitest
	./gradlew pitest

integration-test: ## Run integration tests
	./gradlew integrationTest

ci: lint test-all coverage-check ## Run CI pipeline (lint + test + coverage)

local-up: ## Start local infrastructure (postgres + redis)
	docker compose up -d

local-down: ## Stop local infrastructure
	docker compose down

otel-up: ## Start OpenTelemetry collector stack
	docker compose -f docker-compose.yaml -f docker-compose.otel.yaml up -d

otel-down: ## Stop OpenTelemetry collector stack
	docker compose -f docker-compose.yaml -f docker-compose.otel.yaml down
