# PlacementIQ – AI Placement Preparation Platform

PlacementIQ is an AI-powered placement preparation platform designed to help students prepare for job opportunities. It combines resume management, ATS resume analysis, and secure user authentication in a modern web application.

## 🚀 Live Demo

**Frontend:** [PlacementIQ – Live Website](https://placementiq-frontend.onrender.com/login)

**Backend:** [PlacementIQ Backend](https://placementiq-backend-fp2d.onrender.com)

> **Note:** The application is hosted on Render's free tier. The first request after a period of inactivity may take longer while the service starts.

## 🛠️ Tech Stack

- **Backend:** Spring Boot 3, Java 17, Maven
- **Frontend:** React 18, React Router
- **Database:** PostgreSQL
- **Authentication:** JWT
- **AI Integration:** OpenAI Chat Completions API
- **Deployment:** Render

## ✨ Features

- **User Authentication:** Secure registration and login using JWT.
- **Resume Management:** Upload and manage resumes.
- **ATS Resume Analysis:** Analyze resumes with AI-powered feedback.
- **AI Integration:** Uses OpenAI Chat Completions for resume analysis.
- **Responsive Interface:** A refreshed user interface for navigating the platform.

## 📋 Prerequisites

For local development, install:

- Java 17
- Maven 3.9+
- Node.js 18+
- PostgreSQL 14+

## ⚙️ Local Setup

### 1. Clone the repository

```bash
git clone https://github.com/pratheeshetty367-Syntaxia/PlacementIQ.git
cd PlacementIQ
```

### 2. Configure the database

Create a PostgreSQL database:

```sql
CREATE DATABASE placementiq_db;
```

Configure the database credentials in `backend/src/main/resources/application.yml` or use the environment variables supported by the application.

**Important:** Do not commit production database passwords, JWT secrets, or API keys to GitHub.

### 3. Run the backend

Open a terminal:

```bash
cd backend
```

Set your OpenAI API key if you want to use ATS analysis.

**Windows PowerShell:**
```powershell
$env:OPENAI_API_KEY="your-openai-api-key"
```

**macOS/Linux:**
```bash
export OPENAI_API_KEY="your-openai-api-key"
```

Start Spring Boot:

```bash
mvn spring-boot:run
```

The backend runs locally at:

http://localhost:8080

Without an OpenAI API key, ATS analysis may be unavailable, while other functionality depends on the application's configuration.

### 4. Run the frontend

Open another terminal from the project root:

```bash
cd frontend
```

Create a local `.env` file using `.env.example` as a reference, then configure:

```env
REACT_APP_API_BASE_URL=http://localhost:8080/api
```

Install dependencies and start React:

```bash
npm install
npm start
```

The frontend runs locally at:

http://localhost:3000

## 🧪 How to Try PlacementIQ

1. Open the [live PlacementIQ website](https://placementiq-frontend.onrender.com/login).
2. Register an account or log in.
3. Navigate to the Resume page.
4. Upload a PDF or DOCX resume.
5. Open the ATS Analysis page and run an analysis.

Some features require a working database connection and, for AI-powered analysis, a valid OpenAI API key.

## 🌐 Deployment

The frontend and backend are deployed separately on Render. The frontend communicates with the backend through the configured API base URL.

- Frontend: https://placementiq-frontend.onrender.com
- Backend: https://placementiq-backend-fp2d.onrender.com

## 👩‍💻 Author

**Pratheeksha Shetty**

GitHub: [@pratheeshetty367-Syntaxia](https://github.com/pratheeshetty367-Syntaxia)
