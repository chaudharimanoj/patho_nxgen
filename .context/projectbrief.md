# Project Brief: Patho NXGen Platform

## Overview
Patho NXGen Platform is a multi-tenant SaaS platform for pathology laboratories. It is built as a modular monolith backend with Spring Boot 3.5 / Java 25 and a modern frontend with Angular 19 / TypeScript / Nx.

## Primary Goals
- Provide multi-tenant SaaS capabilities for pathology labs using a **schema-per-tenant** approach.
- Support complete sample lifecycle tracking (Collection -> Barcode -> Dispatch -> Processing -> QC -> Verification -> Approval -> Report Delivery).
- Enable keycloak realm-per-tenant authentication & fine-grained role-based access control (RBAC).
- Offer comprehensive pathology module capability: Patient Management, Laboratory, Billing, Inventory, CRM, Finance, Reporting, Notifications, Scheduler, and Tenant Administration.

## Core Tech Stack
- **Backend**: Spring Boot 3.5, Java 25, JPA/Hibernate, Flyway migrations (public & per-tenant schemas), PostgreSQL 16.
- **Frontend**: Angular 19, TypeScript, Nx Workspace, Angular Material, AG Grid, NgRx Signal Stores, ECharts.
- **Infrastructure & Messaging**: Docker Compose, PostgreSQL, Redis, RabbitMQ, MinIO (S3 compatible), Keycloak 25 (OIDC/OAuth2).
