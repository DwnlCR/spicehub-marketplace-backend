# SpiceHub Marketplace Backend

Backend do SpiceHub, um marketplace para comercialização de ervas, temperos e produtos agrícolas.

O projeto está em desenvolvimento e tem como objetivo construir uma API completa para gerenciamento de usuários, vendedores, produtos, catálogo, carrinho, pedidos e demais operações necessárias para o funcionamento do marketplace.

Atualmente, o desenvolvimento está concentrado na infraestrutura base e no contexto de Identity e Authentication.

---

## Tecnologias

### Atualmente utilizadas

- Java 21
- Spring Boot 4.1.1
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
- Mockito
- MockMvc
- Resend

### Planejadas

Novas tecnologias serão definidas conforme a evolução dos demais contextos do marketplace.

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
    Entidades, Value Objects, regras e invariantes de negócio.

application
    Casos de uso e contratos necessários para execução das operações.

infrastructure
    Persistência, Redis, segurança, criptografia e integrações externas.

presentation
    Controllers HTTP, requests, responses e tratamento de erros.
```

As dependências seguem, sempre que possível, a direção:

```text
Presentation
      ↓
Application
      ↓
Domain

Infrastructure → implementação das portas necessárias
```

O domínio permanece independente dos detalhes de persistência e dos modelos JPA.

Novos bounded contexts serão adicionados conforme o marketplace evoluir.

---

## Funcionalidades

### Identity e Authentication

Status: Em desenvolvimento avançado

Implementado:

- [x] Cadastro de usuário
- [x] Normalização de e-mail
- [x] E-mail case-insensitive
- [x] Validação de provedores de e-mail permitidos
- [x] Senhas armazenadas com BCrypt
- [x] Roles `USER` e `ADMIN`
- [x] Login
- [x] Bloqueio de login para e-mail não verificado
- [x] Access token JWT
- [x] Spring Security OAuth2 Resource Server
- [x] Refresh token opaco
- [x] Sessões de refresh armazenadas no Redis
- [x] Refresh token em cookie HttpOnly
- [x] Rotação de refresh tokens
- [x] Consumo atômico do refresh token
- [x] Proteção contra reutilização do refresh token
- [x] Logout
- [x] Revogação da sessão
- [x] Proteção CSRF
- [x] CORS
- [x] Tratamento padronizado de erros com `ProblemDetail`
- [x] Identificação segura do usuário autenticado
- [x] Endpoint `/users/me`
- [x] Verificação de e-mail
- [x] Código de verificação de 6 dígitos
- [x] Código de verificação com expiração
- [x] Código de verificação de uso único
- [x] Invalidação do código anterior após reenvio
- [x] Envio de e-mail através do Resend
- [x] Reenvio de código de verificação
- [x] Cooldown para reenvio de código
- [x] Limite de tentativas de validação do código
- [x] Testes unitários
- [x] Testes de integração
- [x] Testes com PostgreSQL e Redis reais através do Testcontainers
- [x] Teste de concorrência do refresh token

Endpoints atuais:

```http
POST /auth/register
POST /auth/login
POST /auth/refresh
POST /auth/logout
POST /auth/verify-email
POST /auth/resend-verification

GET /auth/csrf
GET /users/me
```

---

## Verificação de e-mail

O cadastro utiliza verificação de propriedade do endereço de e-mail.

Fluxo atual:

```text
Cadastro
   ↓
Usuário criado com emailVerified = false
   ↓
Código criptograficamente aleatório de 6 dígitos
   ↓
Hash armazenado no Redis com TTL
   ↓
Código enviado por e-mail através do Resend
   ↓
POST /auth/verify-email
   ↓
Validação e consumo atômico do código
   ↓
emailVerified = true
   ↓
Login liberado
```

O código possui duração configurável, atualmente definida em:

```yaml
security:
  email-verification:
    code-ttl: 10m
```

Somente o hash do código é armazenado no Redis.

Quando um novo código é emitido, o código anterior deixa de ser válido.

Após uma verificação bem-sucedida, o código é removido e não pode ser reutilizado.

O reenvio possui cooldown controlado pelo backend através do Redis. A aquisição do cooldown é realizada atomicamente para impedir múltiplos envios simultâneos.

O fluxo também limita a quantidade de tentativas de validação de um mesmo código.

Atualmente são aceitos endereços dos seguintes provedores:

```text
gmail.com
hotmail.com
outlook.com
yahoo.com
```

A validação do provedor não substitui a verificação de propriedade do endereço. O usuário precisa confirmar o código recebido antes de poder realizar login.

---

## Autenticação

A autenticação utiliza access tokens JWT e refresh tokens opacos.

### Access Token

O access token utiliza JWT e possui curta duração.

Configuração atual:

```text
15 minutos
```

É enviado nas requisições protegidas através do header:

```http
Authorization: Bearer <access_token>
```

A validação é realizada pelo Spring Security OAuth2 Resource Server.

O token contém a identificação do usuário e suas roles.

A aplicação não utiliza identificadores enviados pelo cliente para determinar a identidade do usuário autenticado.

---

### Refresh Token

O refresh token é opaco e armazenado no navegador através de cookie HttpOnly.

As sessões correspondentes são armazenadas no Redis.

Configuração atual:

```text
7 dias
```

A aplicação implementa rotação de refresh tokens.

Após um refresh token ser utilizado com sucesso:

```text
token atual
    ↓
consumido atomicamente
    ↓
invalidado
    ↓
novo refresh token emitido
```

O consumo é realizado atomicamente no Redis para impedir reutilização sequencial e consumo concorrente do mesmo token.

O frontend não precisa e não deve acessar diretamente o valor do refresh token.

---

## Usuário autenticado

A identidade de um usuário autenticado é obtida a partir do JWT validado pelo Spring Security.

Recursos pertencentes a um usuário não devem confiar em `userId` enviado pelo frontend para determinar propriedade.

Endpoint atual:

```http
GET /users/me
```

Retorna os dados do usuário correspondente ao token autenticado.

Essa abordagem será reutilizada nos futuros recursos pertencentes ao usuário, como endereços, carrinho, pedidos e outros dados privados.

---

## Banco de dados

### PostgreSQL

Status: Implementado

O PostgreSQL é utilizado como banco de dados relacional principal.

O schema é versionado exclusivamente através do Flyway.

Hibernate é utilizado para validação do schema:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

O Open Session in View permanece desabilitado:

```yaml
spring:
  jpa:
    open-in-view: false
```

Estrutura atual:

```text
users
roles
user_roles
flyway_schema_history
```

A tabela `users` também mantém o estado de verificação do e-mail.

Novas estruturas serão adicionadas exclusivamente através de migrations.

---

### Redis

Status: Implementado

Atualmente utilizado para:

- sessões de refresh token;
- rotação de refresh token;
- consumo atômico de refresh tokens;
- códigos de verificação de e-mail;
- expiração dos códigos de verificação;
- consumo de uso único dos códigos;
- cooldown de reenvio;
- controle de tentativas de verificação.

O Redis é utilizado principalmente para dados temporários e operações que exigem TTL ou atomicidade.

---

## Segurança

Implementado:

- [x] Spring Security
- [x] BCrypt
- [x] JWT assinado
- [x] Validação de issuer
- [x] OAuth2 Resource Server
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
- [x] Identidade obtida pelo contexto autenticado
- [x] Verificação de propriedade do e-mail
- [x] Códigos de verificação com TTL
- [x] Uso único dos códigos de verificação
- [x] Cooldown de reenvio
- [x] Limite de tentativas de verificação
- [x] Respostas anti-enumeração no reenvio de código

Planejado:

- [ ] Recuperação de senha
- [ ] Rate limiting global
- [ ] Proteções adicionais de infraestrutura
- [ ] Autorização específica dos futuros recursos do marketplace

---

## Tratamento de erros

Status: Implementado

A API utiliza `ProblemDetail` para padronização das respostas de erro HTTP.

Exemplo:

```json
{
  "title": "Invalid refresh token",
  "status": 401,
  "detail": "Invalid or expired refresh token",
  "instance": "/auth/refresh",
  "timestamp": "..."
}
```

Erros de autenticação, autorização, validação, regras de domínio e operações de segurança são convertidos para respostas HTTP apropriadas.

Informações sensíveis ou detalhes internos da infraestrutura não são expostos ao cliente.

---

## Testes

Os testes utilizam JUnit 5, Mockito, MockMvc e Testcontainers.

PostgreSQL e Redis reais são inicializados em containers durante os testes de integração.

### Identity e Authentication

Cenários atualmente cobertos incluem:

- [x] Registro de usuário
- [x] E-mail duplicado
- [x] E-mail case-insensitive
- [x] Provedores de e-mail permitidos
- [x] Login
- [x] Bloqueio de login antes da verificação do e-mail
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
- [x] CSRF
- [x] Concorrência no consumo de refresh token
- [x] Reenvio de código de verificação
- [x] Cooldown de reenvio
- [x] Expiração do cooldown no Redis
- [x] Usuário já verificado
- [x] Reenvio para usuário inexistente
- [x] Limite de tentativas do código de verificação

Os testes de integração não dependem dos containers PostgreSQL e Redis utilizados no ambiente de desenvolvimento.

O Docker Engine precisa estar disponível para execução dos Testcontainers.

---

## Contextos futuros

### Usuários

Status: Em desenvolvimento

Planejado:

- [ ] Evolução do perfil do usuário
- [ ] Endereços
- [ ] Recursos privados associados ao usuário

### Vendedores

Status: Planejado

- [ ] A definir

### Produtos

Status: Planejado

- [ ] A definir

### Categorias

Status: Planejado

- [ ] A definir

### Catálogo

Status: Planejado

- [ ] A definir

### Carrinho

Status: Planejado

- [ ] A definir

### Pedidos

Status: Planejado

- [ ] A definir

### Pagamentos

Status: Planejado

- [ ] A definir

### Avaliações

Status: Planejado

- [ ] A definir

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

Em ambientes que utilizam o comando legado do Compose:

```bash
docker-compose up -d
```

Configure as variáveis necessárias para integrações externas.

Exemplo:

```bash
export RESEND_API_KEY='<resend-api-key>'
```

Não armazene chaves reais no repositório.

Execute a aplicação:

```bash
./gradlew bootRun
```

Portas utilizadas no ambiente de desenvolvimento:

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

Para executar uma classe específica:

```bash
./gradlew test --tests "*NomeDaClasseDeTeste"
```

Os testes de integração utilizam Testcontainers.

Os containers PostgreSQL e Redis do ambiente de desenvolvimento não precisam estar em execução, mas o Docker Engine deve estar disponível.

---

## Configuração

Os arquivos de configuração são separados por ambiente:

```text
src/main/resources/
├── application.yml
├── application-dev.yml
└── application-prod.yml
```

Configurações sensíveis devem ser fornecidas através de variáveis de ambiente.

Exemplos incluem:

```text
JWT_SECRET
RESEND_API_KEY
DB_URL
DB_USERNAME
DB_PASSWORD
REDIS_HOST
REDIS_PORT
REDIS_PASSWORD
FRONTEND_URL
```

Segredos não devem ser versionados no Git.

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
- [x] CORS
- [x] Usuário autenticado
- [x] Verificação de e-mail
- [x] Envio de código por e-mail
- [x] Reenvio de código
- [x] Cooldown de reenvio
- [x] Limite de tentativas
- [x] Testes unitários
- [x] Testes de integração
- [ ] Recuperação de senha
- [ ] Rate limiting global
- [ ] Hardening adicional de produção

### Marketplace

- [ ] Usuários
- [ ] Vendedores
- [ ] Produtos
- [ ] Categorias
- [ ] Catálogo
- [ ] Carrinho
- [ ] Pedidos
- [ ] Pagamentos
- [ ] Avaliações

---

## Status

Projeto em desenvolvimento.

A infraestrutura base e o contexto de Identity e Authentication constituem a primeira etapa do backend.

O fluxo de cadastro, verificação de e-mail, autenticação, renovação de sessão, identificação do usuário autenticado e logout já está funcional.

Os próximos contextos serão adicionados progressivamente conforme a evolução do marketplace.

---

## Autor

Daniel Rodrigues

Engenharia de Software - Universidade Federal do Ceará (UFC)

GitHub: `DwnlCR`
