# Pet Shop Management API

## Descrição

Este projeto é uma **API REST para gestão de Pet Shop**, desenvolvida como **projeto de portfólio com foco em back-end e regras de negócio**, e não apenas em operações CRUD.

O sistema modela um fluxo **presencial e determinístico de atendimento**, cobrindo desde o cadastro básico até o fechamento financeiro, com preocupação explícita em:
- Estados de domínio
- Integridade de dados
- Rastreabilidade financeira
- Controle de concorrência

O projeto **não possui integrações externas de pagamento** e foi pensado como um MVP técnico para estudo e demonstração de conceitos de back-end.

---

## Principais Funcionalidades

### Cadastro e Gestão
- Clientes (Owners)
- Pets (vinculados a clientes)
- Catálogo de serviços com preços por porte do pet

### Atendimento (Appointment)
- Criação de atendimentos com data/hora agendada
- Inclusão de múltiplos serviços
- Controle explícito de status:
  - `SCHEDULED`
  - `IN_PROGRESS`
  - `WAITING_PAYMENT`
  - `COMPLETED`
  - `CANCELED`
- Bloqueio de alterações após fechamento para pagamento

### Pagamento
- Pagamento presencial
- Regras por método:
  - **PIX / CASH**: desconto fixo de 5%
  - **CARD**:
    - até 2 parcelas: sem juros
    - 3 a 6 parcelas: juros configuráveis por parcela adicional
- Pagamento aprovado conclui automaticamente o atendimento
- Valores financeiros arredondados com `HALF_UP`

---

## Tecnologias Utilizadas

- Java 17
- Spring Boot
- Spring Data JPA / Hibernate
- Bean Validation
- API REST
- Swagger / OpenAPI
- Docker
- Docker Compose
- Banco de dados relacional (via container)

---

## Arquitetura e Organização

O projeto segue uma separação clara de responsabilidades:

```
src
 ├── api
 │   ├── controller
 │   ├── dto
 │   └── exception
 ├── application
 │   ├── service
 │   ├── mapper
 │   └── exception
 ├── domain
 │   ├── entity
 │   ├── enums
 │   └── pricing
 ├── infrastructure
 │   ├── persistence
 │   └── config
 └── util
```

### Princípios adotados
- Regras de negócio concentradas no domínio e services
- Entidades com comportamento (não apenas setters/getters)
- Transações explícitas em operações críticas
- Uso de lock pessimista e controle de versão (`@Version`) para concorrência
- Tratamento global e padronizado de erros HTTP

---

## Endpoints Principais

### Owners (Clientes)
```
GET    /owners
GET    /owners/{id}
GET    /owners/cpf/{cpf}
POST   /owners
PATCH  /owners/{cpf}/activate
PATCH  /owners/{cpf}/deactivate
PATCH  /owners/{cpf}/update
```

### Pets
```
GET    /pets
GET    /pets/{id}
POST   /pets
PATCH  /pets/{id}
DELETE /pets/{id}
```

### Catálogo de Serviços
```
GET    /catalog
GET    /catalog/{id}
POST   /catalog
PATCH  /catalog/{id}/activate
PATCH  /catalog/{id}/deactivate
DELETE /catalog/{id}
```

### Atendimentos
```
GET    /appointments/{id}
GET    /appointments/future
GET    /appointments/history
POST   /appointments
POST   /appointments/{id}/items
PATCH  /appointments/{id}/start
PATCH  /appointments/{id}/close
PATCH  /appointments/{id}/cancel
```

### Pagamentos
```
POST /appointments/{appointmentId}/payments
```

---

## Documentação da API (Swagger)

Após subir a aplicação, a documentação completa dos endpoints pode ser acessada em:

```
http://localhost:8080/swagger-ui/index.html
```

---

## Como Executar o Projeto com Docker

### Pré-requisitos
- Docker
- Docker Compose

### Subir a aplicação

Na raiz do projeto:

```bash
docker compose down -v
docker build --no-cache -t pet-shop-management .
docker compose up -d
```

A aplicação ficará disponível em:

```
http://localhost:8080
```

---

## Tratamento de Erros

A API possui um **handler global de exceções**, retornando respostas padronizadas no formato:

```json
{
  "status": 409,
  "error": "CONFLICT",
  "message": "Business rule violation message",
  "path": "/appointments/1/close",
  "timestamp": "2026-01-01T12:00:00Z"
}
```

Códigos utilizados:
- `400` – Erro de validação / requisição inválida
- `404` – Recurso não encontrado
- `409` – Violação de regra de negócio
- `500` – Erro interno inesperado

---

## Limitações Conhecidas (MVP)

Este projeto é um MVP e possui limitações intencionais:
- Conflito de agenda validado apenas por data/hora exata
- Pagamento não integra com gateways externos
- Exclusões físicas ainda existentes para algumas entidades
- Idempotência de pagamento não implementada

Esses pontos foram mantidos fora do escopo para preservar simplicidade e foco didático.

---

## Objetivo do Projeto

Este projeto foi desenvolvido como **primeiro projeto back-end robusto**, com foco em:
- Modelagem de domínio
- Fluxo de negócio
- Consistência de dados
- Aprendizado prático de arquitetura back-end

Ele **não se propõe a ser um sistema pronto para produção**, mas sim um estudo aplicado e evolutivo.

---

## Autor

Projeto desenvolvido por Andressa Soares para fins de estudo e portfólio.
