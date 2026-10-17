.PHONY: help docs-check android-check

help:
	@echo "docs-check  Validate planning documents, links and backlog"
	@echo "android-check  Run domain tests, build debug APK and lint (JDK 17 / SDK 36)"

docs-check:
	python3 scripts/check_docs.py

android-check:
	./gradlew :core:domain:test :app:assembleDebug :app:lintDebug
