# Taiga Android Client

Android native client for Taiga built with Kotlin, Jetpack Compose, Hilt, Retrofit, and encrypted token storage.

## What it includes

- Taiga normal-login flow against `POST /api/v1/auth`
- Secure storage for `auth_token` and `refresh`
- Compose login, project list, and issue list screens
- Dynamic Taiga base URL support, normalized to `/api/v1/`
- Retrofit/OkHttp network layer ready for future project, issue, and sprint screens
- Automatic refresh-token retry when access tokens expire

## Default API behavior

- Base URL defaults to `https://api.taiga.io/api/v1/`
- Login uses `type=normal` with `username` and `password`
- Authenticated requests use `Authorization: Bearer <token>`
- Project list loads from `GET /api/v1/projects?slight=true`
- Issue list loads from `GET /api/v1/issues?project=<id>`

## Notes

This repository is scaffolded as a standard Gradle Android project. You can build it with the Gradle wrapper and any installed JDK 17, without Android Studio.
