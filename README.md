# PlacementIQ - AI Placement Preparation Platform

Spring Boot 3 (Java 17) + PostgreSQL + JWT backend, React 18 frontend, OpenAI Chat Completions for ATS resume analysis.

Modules included: authentication (Part 1), resume management (Part 2), ATS resume analysis (Part 3), plus the refreshed UI.

## Prerequisites
Java 17, Maven 3.9+, Node.js 18+, PostgreSQL 14+.

## 1. Database
    CREATE DATABASE placementiq_db;
Default credentials in backend/src/main/resources/application.yml are postgres / postgres. Change them if yours differ. Tables are created automatically (ddl-auto: update).

## 2. Backend
    cd backend
    # Windows PowerShell:  $env:OPENAI_API_KEY="sk-..."
    # macOS / Linux:       export OPENAI_API_KEY="sk-..."
    mvn spring-boot:run
Runs on http://localhost:8080. Without OPENAI_API_KEY everything works except ATS analysis, which returns a clear 503.
Change placementiq.jwt.secret before any real deployment.

## 3. Frontend
    cd frontend
    cp .env.example .env
    npm install
    npm start
Opens  https://placementiq-frontend.onrender.com/login.

## Try it
Register, upload a PDF/DOCX resume on the Resume page, then run Analyze on the ATS Analysis page.
