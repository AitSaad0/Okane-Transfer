# Contributing Guide — Okane-Transfer

## Branch Strategy

| Branch | Role |
|---|---|
| `main` | Production — protected. Merge via validated PR only. |
| `develop` | Integration — all features merge here first. |
| `feature/xxx` | One branch per feature. Created from `develop`. |
| `fix/xxx` | Bug fix. Created from `develop`. |
| `chore/xxx` | Maintenance, config, tooling. Created from `develop`. |
| `hotfix/xxx` | Urgent production fix. Created from `main`, merged into `main` + `develop`. |

---

## Branch Rules

### `main`
- Direct push **forbidden**
- Merge via PR only
- PR requires **1 approval minimum**
- CI must pass before merge
- Force push blocked

### `develop`
- Direct push **forbidden**
- Merge via PR only
- CI must pass before merge

---

## Workflow

### Feature / Fix / Chore

```bash
# 1. Always start from develop
git checkout develop
git pull origin develop

# 2. Create your branch
git checkout -b feature/auth-jwt
# or fix/login-error
# or chore/update-dependencies

# 3. Work, commit (see conventions below)
git add .
git commit -m "feat: add JWT authentication"

# 4. Push and open PR → develop
git push origin feature/auth-jwt
```

Then open a **Pull Request** targeting `develop` on GitHub.

### Hotfix

```bash
# 1. Start from main
git checkout main
git pull origin main
git checkout -b hotfix/fix-transfer-crash

# 2. Fix, commit, push
git commit -m "fix: resolve null pointer on transfer"
git push origin hotfix/fix-transfer-crash

# 3. Open PR → main
# 4. After merge, also merge main → develop
```

---

## Commit Conventions

Format: `type: short description`

| Type | Usage |
|---|---|
| `feat` | New feature |
| `fix` | Bug fix |
| `chore` | Maintenance, config, tooling |
| `ci` | CI/CD changes |
| `docs` | Documentation |
| `refactor` | Code refactor without behavior change |
| `test` | Add or update tests |

**Examples:**
```
feat: add user registration endpoint
fix: resolve NPE on empty transfer
chore: update dependencies
ci: add Java 21 CI pipeline
docs: add contributing guide
```

---

## CI Pipeline

Triggered on every **Pull Request** targeting `main` or `develop`.

Steps:
1. Checkout code
2. Set up Java 21
3. `mvn clean package -DskipTests`
4. `mvn test`

PR cannot be merged if CI fails.

---

## Project Structure

```
Okane-Transfer/
├── src/
│   └── main/
│       └── java/com/okane/
│           ├── config/
│           │   ├── AppConfig.java       # DataSource, Hibernate, transactions
│           │   ├── MvcConfig.java       # Spring MVC
│           │   ├── SecurityConfig.java  # Spring Security
│           │   └── WebConfig.java       # DispatcherServlet initializer
│           └── controller/
│               └── HelloController.java
├── .github/
│   └── workflows/
│       └── ci.yml                       # GitHub Actions CI
├── Dockerfile                           # Multi-stage build (Maven + Tomcat)
├── docker-compose.yml                   # App + PostgreSQL
├── .env                                 # Local secrets (gitignored)
├── .env.example                         # Env template to commit
└── pom.xml
```

---

## Environment Variables

Copy `.env.example` to `.env` and fill in your values:

```bash
cp .env.example .env
```

Never commit `.env` — it is listed in `.gitignore`.
