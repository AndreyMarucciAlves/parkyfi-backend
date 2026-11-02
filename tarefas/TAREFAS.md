# Tarefas — Parkyfi Backend

Objetivo: um CRUD simples das 3 entidades do banco (ver `bd_der/`), com o vínculo entre elas.

## Entidades

```
TipoVeiculo (1) ── (N) Veiculo (1) ── (N) Ticket
```

| Entidade | Campos | Vínculo |
|---|---|---|
| `TipoVeiculo` | id, descricao, valorHora | — |
| `Veiculo` | id, placa, modelo, cor | pertence a um `TipoVeiculo` |
| `Ticket` | id, entrada, saida, valorTotal, status | pertence a um `Veiculo` |

## Tarefas

- [ ] **T1 — Banco:** criar as 3 tabelas no PostgreSQL (script SQL ou `spring.jpa.hibernate.ddl-auto=update`).
- [ ] **T2 — TipoVeiculo:** entidade, repository, service e controller com CRUD em `/tipos-veiculo`.
- [ ] **T3 — Veiculo:** entidade (`@ManyToOne` com `TipoVeiculo`), repository, service e controller com CRUD em `/veiculos`.
- [ ] **T4 — Ticket:** entidade (`@ManyToOne` com `Veiculo`), repository, service e controller com CRUD em `/tickets`.
- [ ] **T5 — Teste:** testar todos os endpoints pelo Swagger (`http://localhost:8081/v1/api/swagger-ui.html`).

Cada CRUD tem os endpoints:

| Método | Rota | Ação |
|---|---|---|
| GET | `/recurso` | listar todos |
| GET | `/recurso/{id}` | buscar por id |
| POST | `/recurso` | criar |
| PUT | `/recurso/{id}` | atualizar |
| DELETE | `/recurso/{id}` | excluir |

Ordem sugerida: T1 → T2 → T3 → T4 → T5 (cada entidade depende da anterior por causa da chave estrangeira).
