# SpiceHub Marketplace Backend

Backend do SpiceHub, um marketplace para comercialização de ervas, temperos e produtos agrícolas.

O projeto está em desenvolvimento e tem como objetivo construir uma API completa para gerenciamento de usuários, produtos, vendedores, catálogo, carrinho, pedidos e demais operações necessárias para o funcionamento do marketplace.

## Tecnologias

### Atualmente utilizadas

- Java 21
- Spring Boot 4
- Spring Security
- OAuth2 Resource Server
- JWT
- Spring Data JPA
- PostgreSQL
- Redis
- Flyway
- Gradle
- Docker
- Docker Compose
- Testcontainers
- JUnit 5
- MockMvc

### Planejadas

A definir conforme a evolução do projeto.

---

## Arquitetura

O projeto utiliza uma organização baseada em Domain-Driven Design (DDD), com separação entre domínio, aplicação, infraestrutura e apresentação.

Estrutura atual:

```text
src/main/java/br/com/dwnl/spicehub
│
├── identity
│   ├── domain
│   ├── application
│   ├── infrastructure
│   └── presentation
│
└── SpicehubMarketplaceApplication.java
```

As responsabilidades são divididas em:

```text
domain
    Regras e modelos de negócio.

application
    Casos de uso e contratos da aplicação.

infrastructure
    Persistência, segurança e integrações externas.

presentation
    Interface HTTP, controllers e DTOs.
```

A estrutura será expandida com novos contextos conforme o desenvolvimento do marketplace.

---

## Funcionalidades

### Identity e Authentication

Status: Implementado

O contexto de identidade é responsável atualmente pelo cadastro, autenticação e gerenciamento da sessão dos usuários.

Implementado:

- [x] Cadastro de usuário
- [x] E-mail normalizado e case-insensitive
- [x] Senhas armazenadas com BCrypt
- [x] Roles `USER` e `ADMIN`
- [x] Login
- [x] Access token JWT
- [x] Spring Security Resource Server
- [x] Refresh token opaco
- [x] Armazenamento das sessões de refresh no Redis
- [x] Refresh token em cookie HttpOnly
- [x] Rotação de refresh tokens
- [x] Consumo atômico do refresh token
- [x] Proteção contra reutilização do refresh token
- [x] Logout
- [x] Revogação da sessão
- [x] Proteção CSRF
- [x] CORS
- [x] Tratamento padronizado de erros
- [x] Endpoint de usuário autenticado
- [x] Testes de integração
- [x] Teste de concorrência do refresh token

Endpoints atuais:

```http
POST /auth/register
POST /auth/login
POST /auth/refresh
POST /auth/logout

GET /auth/csrf
GET /users/me
```

---

### Usuários

Status: Em desenvolvimento

Planejado:

- [ ] 
- [ ] 
- [ ] 

Endpoints:

```text
A definir.
```

---

### Vendedores

Status: Planejado

Planejado:

- [ ] 
- [ ] 
- [ ] 

Endpoints:

```text
A definir.
```

---

### Produtos

Status: Planejado

Planejado:

- [ ] 
- [ ] 
- [ ] 

Endpoints:

```text
A definir.
```

---

### Categorias

Status: Planejado

Planejado:

- [ ] 
- [ ] 

Endpoints:

```text
A definir.
```

---

### Catálogo

Status: Planejado

Planejado:

- [ ] 
- [ ] 
- [ ] 

Endpoints:

```text
A definir.
```

---

### Carrinho

Status: Planejado

Planejado:

- [ ] 
- [ ] 
- [ ] 

Endpoints:

```text
A definir.
```

---

### Pedidos

Status: Planejado

Planejado:

- [ ] 
- [ ] 
- [ ] 

Endpoints:

```text
A definir.
```

---

### Pagamentos

Status: Planejado

Planejado:

- [ ] 
- [ ] 
- [ ] 

Endpoints:

```text
A definir.
```

---

### Avaliações

Status: Planejado

Planejado:

- [ ] 
- [ ] 

Endpoints:

```text
A definir.
```

---

## Autenticação

A autenticação atual utiliza dois tipos de token.

### Access Token

O access token utiliza JWT e possui curta duração.

É enviado nas requisições através do header:

```http
Authorization: Bearer <access_token>
```

A validação é realizada pelo Spring Security OAuth2 Resource Server.

### Refresh Token

O refresh token é opaco e armazenado no cliente através de cookie HttpOnly.

As sessões correspondentes são armazenadas no Redis.

A aplicação implementa rotação de refresh tokens. Após um token ser utilizado com sucesso, ele é invalidado e um novo refresh token é emitido.

O consumo é realizado atomicamente no Redis para impedir que o mesmo token seja utilizado simultaneamente por múltiplas requisições.

---

## Banco de dados

### PostgreSQL

Status: Implementado

O PostgreSQL é utilizado como banco de dados relacional principal.

O schema é versionado através do Flyway.

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

Estrutura atual:

```text
users
roles
user_roles
flyway_schema_history
```

Novas tabelas serão adicionadas através de migrations conforme os novos contextos forem implementados.

### Redis

Status: Implementado

Atualmente utilizado para:

- sessões de refresh token;
- controle atômico do consumo de refresh tokens.

Usos futuros:

- [ ] 
- [ ] 
- [ ] 

---

## Segurança

Implementado:

- [x] Spring Security
- [x] BCrypt
- [x] JWT
- [x] Refresh tokens opacos
- [x] Cookies HttpOnly
- [x] SameSite
- [x] CSRF
- [x] CORS
- [x] Roles
- [x] Revogação de refresh tokens
- [x] Rotação de refresh tokens
- [x] Proteção contra replay sequencial
- [x] Proteção contra consumo concorrente do mesmo refresh token

Planejado:

- [ ] 
- [ ] 
- [ ] 

---

## Tratamento de erros

Status: Implementado

A API utiliza `ProblemDetail` para padronização das respostas de erro HTTP.

Exemplo:

```json
{
  "type": "about:blank",
  "title": "Invalid refresh token",
  "status": 401,
  "detail": "Invalid or expired refresh token",
  "instance": "/auth/refresh",
  "timestamp": "..."
}
```

---

## Testes

### Identity e Authentication

Status: Implementado

Os testes de integração utilizam PostgreSQL e Redis reais através do Testcontainers.

Cenários cobertos:

- [x] Registro de usuário
- [x] E-mail duplicado
- [x] E-mail case-insensitive
- [x] Login
- [x] Senha incorreta
- [x] Usuário inexistente
- [x] Usuário desabilitado
- [x] Access token JWT
- [x] JWT inválido
- [x] Acesso autenticado
- [x] Refresh token
- [x] Rotação de refresh token
- [x] Reutilização de refresh token
- [x] Logout
- [x] Revogação da sessão
- [x] CSRF no refresh
- [x] CSRF no logout
- [x] Concorrência no consumo de refresh token

### Demais contextos

- [ ] Testes de usuários
- [ ] Testes de vendedores
- [ ] Testes de produtos
- [ ] Testes de catálogo
- [ ] Testes de carrinho
- [ ] Testes de pedidos
- [ ] Testes de pagamentos
- [ ] Testes de avaliações

---

## Executando o projeto

### Pré-requisitos

- Java 21
- Docker
- Docker Compose

Clone o repositório:

```bash
git clone git@github.com:DwnlCR/spicehub-marketplace-backend.git
```

Entre no projeto:

```bash
cd spicehub-marketplace-backend
```

Inicie PostgreSQL e Redis:

```bash
docker compose up -d
```

Execute a aplicação:

```bash
./gradlew bootRun
```

A aplicação utiliza por padrão:

| Serviço | Porta |
|---|---:|
| Application | 8080 |
| PostgreSQL | 5435 |
| Redis | 6382 |

---

## Executando os testes

```bash
./gradlew clean test
```

Os testes de integração utilizam Testcontainers e não dependem dos containers PostgreSQL e Redis utilizados pelo ambiente de desenvolvimento.

O Docker Engine precisa estar disponível.

---

## Configuração

Os arquivos de configuração estão separados por ambiente:

```text
src/main/resources/
├── application.yml
├── application-dev.yml
└── application-prod.yml
```

Configurações sensíveis do ambiente de produção devem ser fornecidas através de variáveis de ambiente.

---

## Roadmap

### Identity e Authentication

- [x] Registro
- [x] Login
- [x] JWT
- [x] Refresh token
- [x] Rotação de refresh token
- [x] Logout
- [x] CSRF
- [x] Testes de integração

### Usuários

- [ ] 

### Vendedores

- [ ] 

### Produtos

- [ ] 

### Categorias

- [ ] 

### Catálogo

- [ ] 

### Carrinho

- [ ] 

### Pedidos

- [ ] 

### Pagamentos

- [ ] 

### Avaliações

- [ ] 

---

## Status

Projeto em desenvolvimento.

O contexto de Identity e Authentication constitui a primeira etapa implementada do backend. Os demais contextos serão definidos e desenvolvidos progressivamente.

---

## Autor

Daniel Rodrigues

Engenharia de Software - Universidade Federal do Ceará (UFC)

GitHub: `DwnlCR`
