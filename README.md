# Parkyfi Backend

API REST do **Parkyfi**, um sistema de gerenciamento de estacionamento.

## Sobre o projeto

Este projeto é uma atividade acadêmica da disciplina **Arquitetura de Software: Estilos Arquiteturais**.

### Arquitetura

A arquitetura escolhida pelo grupo foi **Cliente-Servidor**:

- **Servidor (este repositório):** API REST em Spring Boot, responsável pelas regras de negócio e pela persistência dos dados em PostgreSQL.
- **Cliente:** aplicação frontend que consome a API via HTTP — [parkyfi-frontend](https://github.com/AndreyMarucciAlves/parkyfi-frontend).

```
┌──────────────┐    HTTP/JSON    ┌──────────────┐      JDBC      ┌──────────────┐
│   Frontend   │ ──────────────► │   Backend    │ ─────────────► │  PostgreSQL  │
│  (Cliente)   │ ◄────────────── │  (Servidor)  │ ◄───────────── │              │
└──────────────┘                 └──────────────┘                └──────────────┘
```

## Grupo

- Andrey Marucci
- João Szczypior
- Henrique Cavalari
- Enzo Schmidt
- Josephy Rocha
- Eduardo Apolônio

## Tecnologias

| Tecnologia | Versão |
|---|---|
| Java | 21 |
| Spring Boot | 4.1.2 |
| Spring Web MVC | (gerenciado pelo Spring Boot) |
| Spring Data JPA / Hibernate | (gerenciado pelo Spring Boot) |
| springdoc-openapi (Swagger UI) | 3.2.0 |
| PostgreSQL | 16.9 (imagem Docker) |
| Lombok | (gerenciado pelo Spring Boot) |
| Maven (via Maven Wrapper) | 3.10.0 |
| Docker / Docker Compose | — |

## Como executar

### Pré-requisitos

- JDK 21
- Docker e Docker Compose

### 1. Configurar variáveis de ambiente

Dentro de `parkfy-backend/`, copie o template e preencha os valores:

```bash
cp parkfy-backend/.env.template parkfy-backend/.env
```

| Variável | Descrição |
|---|---|
| `CONTAINER_NAME` | Nome do container do PostgreSQL |
| `DB_NAME` | Nome do banco de dados |
| `DB_USERNAME` | Usuário do banco |
| `DB_PASSWORD` | Senha do banco |
| `DB_PORT` | Porta exposta do banco na máquina local |
| `DB_URL` | URL JDBC (ex.: `jdbc:postgresql://localhost:5432/nome_banco`) |
| `SPRING_PROFILES_ACTIVE` | Perfil ativo do Spring (ex.: `dev`) |

### 2. Subir o banco de dados

```bash
cd parkfy-backend && docker compose up -d
```

### 3. Rodar a aplicação

```bash
cd parkfy-backend && ./mvnw spring-boot:run
```

No Windows, use `mvnw.cmd spring-boot:run`.

A API sobe em `http://localhost:8081/v1/api`.

## Documentação da API

Com a aplicação rodando, a documentação Swagger fica disponível em:

- Swagger UI: `http://localhost:8081/v1/api/swagger-ui.html`
- OpenAPI (JSON): `http://localhost:8081/v1/api/v3/api-docs`

## Testes

```bash
cd parkfy-backend && ./mvnw test
```

## Links

- Frontend: https://github.com/AndreyMarucciAlves/parkyfi-frontend
