# Tarefas — Parkyfi Backend

Objetivo: um CRUD simples das 4 entidades do banco (ver `bd_der/`), com o vínculo entre elas.

## Entidades

```
Tarifa (1) ── (N) TipoVeiculo (1) ── (N) Veiculo (1) ── (N) Ticket
```

| Entidade | Campos | Vínculo |
|---|---|---|
| `Tarifa` | id, valorHora | — |
| `TipoVeiculo` | id, descricao | pertence a uma `Tarifa` |
| `Veiculo` | id, placa, modelo, cor | pertence a um `TipoVeiculo` |
| `Ticket` | id, entrada, saida, valorTotal, status | pertence a um `Veiculo` |

## Tarefas

- [ ] **T1 — Banco:** criar as 4 tabelas no PostgreSQL (script SQL ou `spring.jpa.hibernate.ddl-auto=update`).
- [ ] **T2 — Tarifa:** entidade, repository, service e controller com CRUD em `/tarifas`.
- [ ] **T3 — TipoVeiculo:** entidade (`@ManyToOne` com `Tarifa`), repository, service e controller com CRUD em `/tipos-veiculo`.
- [ ] **T4 — Veiculo:** entidade (`@ManyToOne` com `TipoVeiculo`), repository, service e controller com CRUD em `/veiculos`.
- [ ] **T5 — Ticket:** entidade (`@ManyToOne` com `Veiculo`), repository, service e controller com CRUD em `/tickets`.
- [ ] **T6 — Teste:** testar todos os endpoints pelo Swagger (`http://localhost:8081/v1/api/swagger-ui.html`).

Cada CRUD tem os endpoints:

| Método | Rota | Ação |
|---|---|---|
| GET | `/recurso` | listar todos |
| GET | `/recurso/{id}` | buscar por id |
| POST | `/recurso` | criar |
| PUT | `/recurso/{id}` | atualizar |
| DELETE | `/recurso/{id}` | excluir |

Ordem sugerida: T1 → T2 → T3 → T4 → T5 → T6 (cada entidade depende da anterior por causa da chave estrangeira).
