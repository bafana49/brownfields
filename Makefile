SHELL := bash
.PHONY: compile package test test-local acceptance-test set-release-version tag release dev docker-build docker-run docker-stop docker-clean docker-acceptance-test unit-test verify

MVN := mvn
LOCAL_SERVER_SCRIPT := $(CURDIR)/scripts/run_local_server.sh
RUN_TESTS_HELPER := $(CURDIR)/scripts/run_tests_against_server.sh

VERSION := $(shell $(MVN) help:evaluate -Dexpression=project.version -q -DforceStdout)
RELEASE_VERSION := $(shell echo $(VERSION) | sed 's/-SNAPSHOT$$//')
RELEASE_TAG := release-$(RELEASE_VERSION)

DOCKER_IMAGE := gitlab.wethinkco.de:5050/leletscc-g-025/brownfields_robot_worlds_3
DOCKER_TAG := $(RELEASE_VERSION)

compile:
	$(MVN) compile

package:
	$(MVN) package -DskipTests

test:
	$(MVN) test

verify:
	$(MVN) verify

unit-test:
	$(MVN) test -Dtest="!za.co.wethinkcode.robots.acceptance.**"

set-release-version:
	@echo "Setting release version to $(RELEASE_VERSION)"
	$(MVN) versions:set -DnewVersion=$(RELEASE_VERSION)

test-local:
	@echo "Running all tests against local server..."
	@bash "$(RUN_TESTS_HELPER)" "$(LOCAL_SERVER_SCRIPT)" ""

acceptance-test:
	@echo "Running acceptance tests against local server..."
	@bash "$(RUN_TESTS_HELPER)" "$(LOCAL_SERVER_SCRIPT)" ""

tag:
	@echo "Tagging git release: $(RELEASE_TAG)"
	git tag -a $(RELEASE_TAG) -m "Release $(RELEASE_VERSION)"

release: set-release-version acceptance-test package tag
	@echo "Release build complete: $(RELEASE_TAG)"

dev: compile unit-test
	@echo "Development build complete"

# Docker targets
# Usage: make docker-build
# Or manually: docker build -t robot-worlds-server:$(VERSION) -t robot-worlds-server:latest .
docker-build:
	@echo "Building Docker image..."
	docker build -t $(DOCKER_IMAGE):$(DOCKER_TAG) -t $(DOCKER_IMAGE):latest .

# Usage: make docker-run
# Or manually: docker run -d -p 5050:5050 -p 8080:8080 --name robot-world robot-worlds-server:latest
docker-run:
	@echo "Running Docker container on ports 5050 (socket) and 8080 (Web API)..."
	docker run -d -p 6000:6000 -p 7000:7000 --name robot-worldb $(DOCKER_IMAGE):latest

# Usage: make docker-stop
# Or manually: docker stop robot-world && docker rm robot-world
docker-stop:
	@echo "Stopping Docker container..."
	docker stop robot-world || true
	docker rm robot-world || true

# Usage: make docker-clean
# Or manually: docker stop robot-world && docker rm robot-world && docker rmi robot-worlds-server:$(VERSION) && docker rmi robot-worlds-server:latest
docker-clean:
	@echo "Cleaning up Docker resources..."
	docker stop robot-world || true
	docker rm robot-world || true
	docker rmi $(DOCKER_IMAGE):$(DOCKER_TAG) || true
	docker rmi $(DOCKER_IMAGE):latest || true

docker-acceptance-test: docker-build docker-run
	@echo "Running acceptance tests against Docker container..."
	@sleep 5
	@mvn test -Dtest="za.co.wethinkcode.robots.acceptance.**"
	@$(MAKE) docker-stop
