# Pet Shop Management API (MVP)

API REST para **gestão operacional de um Pet Shop**, focada em atendimento presencial, com **regras de negócio explícitas**, **transições de estado controladas** e **preservação de histórico financeiro**.

Este projeto foi desenvolvido com foco em **portfólio back-end**, priorizando clareza de domínio, consistência de API e comportamento previsível sob regras reais — indo além de um CRUD básico.

Repositório:  
https://github.com/andressa-soares/pet-shop-management

---

## Stack e tecnologias

- **Java**
- **Spring Boot**
- **Spring Data JPA / Hibernate**
- **API REST**
- **Swagger / OpenAPI (springdoc)**
- **Docker & Docker Compose**
- **Bean Validation**
- **Controle de concorrência**
  - `@Version` (optimistic locking)
  - `PESSIMISTIC_WRITE` em operações críticas
- **Tratamento global de erros** (`@RestControllerAdvice`)

---

## Domínio e regras principais

### Entidades

- **Owner**  
  Cliente responsável pelo pet. Pode ser ativado ou inativado, respeitando regras de negócio.

- **Pet**  
  Vinculado a um Owner.  
  O **porte (size)** é imutável, pois impacta diretamente a precificação dos serviços.

- **Catalog**  
  Serviços oferecidos pelo pet shop.  
  Cada serviço possui **preço por porte** (SMALL, MEDIUM, LARGE).

- **Appointment**  
  Atendimento/agendamento.  
  Possui itens, total bruto e **workflow de estados** bem definido.

- **Payment**  
  Pagamento presencial do atendimento, com regras específicas por forma de pagamento.

---

## Workflow do Appointment

Estados possíveis:

- `SCHEDULED`
- `IN_PROGRESS`
- `WAITING_PAYMENT`
- `COMPLETED`
- `CANCELED`

Regras importantes:
- Não é possível alterar itens após `WAITING_PAYMENT`
- Atendimentos cancelados não podem ser retomados
- Pagamento só é permitido em `WAITING_PAYMENT`
- Cada atendimento possui **apenas um pagamento final**

---

## Padrão de API adotado

### Atualizações parciais
- `PATCH /resource/{id}`  
  Usado para atualização de campos comuns.

### Transições de estado (workflow)
- `POST /resource/{id}/actions`  

O corpo da requisição define a ação desejada:
```json
{ "action": "..." }
```

Esse padrão evita múltiplas rotas como `/start`, `/cancel`, `/activate`, garantindo **consistência e clareza**.

---

## Endpoints

### Owners
- `GET /owners`
- `GET /owners/{id}`
- `GET /owners/{cpf}`
- `POST /owners`
- `PATCH /owners/{cpf}`
- `POST /owners/{cpf}/actions`

Ações possíveis:
```json
{ "action": "ACTIVATE" }
```
```json
{ "action": "DEACTIVATE" }
```

---

### Pets
- `GET /pets?species=&breed=&ownerId=`
- `GET /pets/{id}`
- `GET /pets/breeds?species=DOG|CAT`
- `POST /pets`
- `PATCH /pets/{id}`
- `DELETE /pets/{id}`

Exemplo de PATCH:
```json
{
  "notes": "Pet agressivo com outros animais",
  "allergies": "Alergia a shampoo X"
}
```

---

### Catalog
- `GET /catalog?status=ACTIVE|INACTIVE`
- `GET /catalog/{id}`
- `POST /catalog`
- `POST /catalog/{id}/actions`
- `DELETE /catalog/{id}`

Ações:
```json
{ "action": "ACTIVATE" }
```
```json
{ "action": "DEACTIVATE" }
```

---

### Appointments
- `GET /appointments/{id}`
- `GET /appointments/future`
- `GET /appointments/history`
- `POST /appointments`
- `POST /appointments/{id}/items`
- `POST /appointments/{id}/actions`

Ações:
```json
{ "action": "START" }
```
```json
{ "action": "CLOSE_FOR_PAYMENT" }
```
```json
{ "action": "CANCEL" }
```

---

### Payments
- `POST /appointments/{appointmentId}/payments`

Exemplos:
```json
{ "method": "PIX" }
```
```json
{ "method": "CASH" }
```
```json
{ "method": "CARD", "installments": 3 }
```

---

## Tratamento de erros (HTTP)

- **400 Bad Request**  
  Entrada inválida (payload, parâmetros, pré-condições de request)

- **409 Conflict**  
  Violação de regra de negócio ou conflito de estado

- **404 Not Found**  
  Recurso inexistente

- **500 Internal Server Error**  
  Erro inesperado

Todas as respostas seguem um formato padronizado:
```json
{
  "status": 409,
  "error": "CONFLICT",
  "message": "Appointment is already waiting for payment.",
  "path": "/appointments/10/actions",
  "timestamp": "2026-01-18T14:32:00Z"
}
```

---

## Swagger / OpenAPI

A documentação interativa da API pode ser acessada em:

```
http://localhost:8080/swagger-ui/index.html
```

O OpenAPI JSON:
```
http://localhost:8080/v3/api-docs
```

---

## Executando com Docker

### Pré-requisitos
- Docker
- Docker Compose

### Subir a aplicação
```bash
docker compose up -d
```

### Derrubar e reconstruir
```bash
docker compose down -v
docker build --no-cache -t pet-shop-management .
docker compose up -d
```

---

## Considerações finais

Este projeto foi construído com foco em:
- Clareza de domínio
- Consistência de API
- Regras de negócio explícitas
- Evolução incremental

Ele continuará sendo aprimorado com novos ajustes arquiteturais e refinamentos de regras, mantendo sempre o compromisso com **simplicidade, previsibilidade e intenção técnica clara**.

