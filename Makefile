forge := ./archforge

.PHONY: help init build up down infra-up infra-down db-update

help:
	@$(forge) --help

init:
	@$(forge) init --write

build:
	@$(forge) build

up:
	@$(forge) up

down:
	@$(forge) down

infra-up:
	@$(forge) infra up

infra-down:
	@$(forge) infra down

db-update:
	@$(forge) db update
