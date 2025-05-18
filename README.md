# 🧠 Code Judge – LeetCode Clone

An online judge system inspired by LeetCode. Built from scratch to support real-time code submission and evaluation across multiple languages in a secure, scalable microservices architecture.

## 🌍 Live Demo

- Website: https://forecode.codejud.id.vn  
- GitHub: https://github.com/duyancol/ForgeCode  
- Admin dashboard available after login via Google

---

## 🛠️ Tech Stack

- **Backend:** Java, Spring Boot, REST API, JWT, Google OAuth
- **Frontend:** ReactJS, TailwindCSS, Vite
- **Infra:** Docker, Docker Compose, Nginx, Let's Encrypt
- **Messaging:** RabbitMQ (asynchronous job processing)
- **Database:** MySQL, Redis (caching)
- **Languages Supported:** Java, C, C++, Python, C#

---

## 🚀 Features

- Submit & evaluate code in real-time
- Secure code execution inside isolated Docker containers
- Async judging via RabbitMQ
- Admin panel to manage problems and submissions
- Google OAuth login for authentication
- Redis caching for performance
- Deployed on VPS with HTTPS via Certbot

---

## 🏠 System Architecture

```
Frontend (ReactJS)
      |
      ▼
 API Gateway (Nginx)
      |
 ┌────────────┬──────────────┬─────────────┬────────────┐
 | ProblemSvc | SubmissionSvc | JudgeSvc   | UserSvc    |
 └────────────┴──────────────┴─────────────┴────────────┘
      |               ▲              | 
      ▼               |              ▼
   MySQL           RabbitMQ        Docker
      ▲
      ▼
    Redis

```

---


---

## 🥪 How to Run Locally

### 1. Clone Repo

```bash
git clone https://github.com/duyancol/ForgeCode
cd ForgeCode
```

### 2. Start Backend

```bash
cd backend
docker-compose up --build
```

### 3. Start Frontend

```bash
cd frontend
npm install
npm run dev
```

---

## 🗓️ Development Timeline

- Duration: 6 weeks
- Total services: 5 (problem-service, submission-service, judge-service, user_service, API Gateway, notifier_service, difficult_service)
- Deploy: VPS (Ubuntu) with HTTPS (Certbot + Nginx)
- Architecture: Microservices + Docker + Message Queue

---

## 📄 License

MIT License

---

## 👤 Author

**Nguyễn Văn Tường Duy**  
📧 Email: nvtduy0901@gmail.com  
👁️ GitHub: [duyancol](https://github.com/duyancol)

