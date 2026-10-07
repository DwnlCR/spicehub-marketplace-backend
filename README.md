# SpiceHub Marketplace Backend

Backend do SpiceHub, um marketplace para comercialização de ervas, temperos e produtos agrícolas.

O projeto está em desenvolvimento e tem como objetivo construir uma API completa para gerenciamento de usuários, catálogo de produtos, carrinho, pedidos, pagamentos e demais operações necessárias para o funcionamento do marketplace.

Atualmente, a infraestrutura base, o contexto de Identity e Authentication e a base do catálogo de produtos estão implementados.

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
├── catalog
│   ├── domain
│   ├── application
│   ├── infrastructure
│   └── presentation
│
├── shared
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

Funcionalidades compartilhadas entre os contextos, como o tratamento global de erros HTTP, permanecem no pacote `shared`.

Novos contextos serão adicionados conforme o marketplace evoluir.

---

# Funcionalidades

## Identity e Authentication

Status: Implementado em ambiente de desenvolvimento

### Cadastro e autenticação

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
- [x] Identificação segura do usuário autenticado
- [x] Endpoint `/users/me`

### Verificação de e-mail

- [x] Código de verificação de 6 dígitos
- [x] Expiração do código
- [x] Uso único
- [x] Invalidação do código anterior após reenvio
- [x] Envio através do Resend
- [x] Reenvio de código
- [x] Cooldown de reenvio
- [x] Limite de tentativas de validação

### Recuperação de senha

- [x] Solicitação de recuperação
- [x] Código temporário de 6 dígitos
- [x] Expiração do código
- [x] Uso único
- [x] Limite de tentativas
- [x] Cooldown entre solicitações
- [x] Alteração segura da senha
- [x] Revogação das sessões após alteração da senha

### Proteções adicionais

- [x] Rate limiting de login por e-mail e IP
- [x] Rate limiting de cadastro por IP
- [x] Tratamento padronizado de erros com `ProblemDetail`
- [x] Testes unitários
- [x] Testes de integração
- [x] PostgreSQL e Redis reais nos testes através do Testcontainers
- [x] Teste de concorrência do refresh token

### Endpoints

```http
POST /auth/register
POST /auth/login
POST /auth/refresh
POST /auth/logout
POST /auth/verify-email
POST /auth/resend-verification
POST /auth/request-password-reset
POST /auth/reset-password

GET /auth/csrf
GET /users/me
```

---

# Catálogo

Status: Base funcional implementada

O contexto de catálogo gerencia categorias, produtos e variantes de produtos.

O modelo foi estruturado separando o produto de suas apresentações comerciais.

Exemplo:

```text
Produto
└── Chá de Camomila
    ├── 50 GRAM
    ├── 100 GRAM
    └── 250 GRAM
```

Cada variante possui quantidade, unidade de medida, preço e disponibilidade próprios.

---

## Categorias

Implementado:

- [x] Criação de categorias
- [x] Consulta individual
- [x] Listagem
- [x] Renomeação
- [x] Ativação
- [x] Desativação
- [x] Nome único case-insensitive
- [x] Categorias inativas continuam consultáveis
- [x] Apenas administradores podem alterar categorias
- [x] Consultas públicas

Uma categoria inativa representa uma categoria temporariamente indisponível.

A desativação da categoria não altera diretamente o estado dos produtos ou variantes associados.

### Endpoints

```http
GET   /categories
GET   /categories/{categoryId}

POST  /categories
PUT   /categories/{categoryId}

PATCH /categories/{categoryId}/activate
PATCH /categories/{categoryId}/deactivate
```

As operações de escrita exigem role `ADMIN`.

---

## Produtos

Implementado:

- [x] Criação
- [x] Consulta individual
- [x] Listagem
- [x] Atualização
- [x] Ativação
- [x] Desativação
- [x] Associação com categoria
- [x] Busca por nome
- [x] Paginação
- [x] Ordenação alfabética crescente
- [x] Ordenação alfabética decrescente
- [x] Estado `ACTIVE`
- [x] Estado `INACTIVE`
- [x] Consultas públicas
- [x] Operações administrativas protegidas

Estados atuais:

```text
ACTIVE
    Produto ativo no catálogo.

INACTIVE
    Produto temporariamente indisponível,
    mas ainda visível no catálogo.
```

Ao desativar um produto, suas variantes são marcadas como `SOLD_OUT`.

Ao reativar um produto, suas variantes voltam para `AVAILABLE`.

Um produto não pode ser ativado quando sua categoria estiver inativa.

### Busca e paginação

A listagem suporta busca, ordenação e paginação.

Exemplo:

```http
GET /products?search=cha&sort=NAME_ASC&page=0&size=20
```

Ordenações disponíveis:

```text
NAME_ASC
NAME_DESC
```

### Endpoints

```http
GET   /products
GET   /products/{productId}

POST  /products
PUT   /products/{productId}

PATCH /products/{productId}/activate
PATCH /products/{productId}/deactivate
```

As consultas são públicas.

Criação e alterações exigem role `ADMIN`.

---

## Variantes de produto

As variantes representam diferentes apresentações comerciais de um mesmo produto.

Exemplo:

```text
Produto: Chá Verde

50 GRAM  → R$ ...
100 GRAM → R$ ...
250 GRAM → R$ ...
```

Unidades suportadas atualmente:

```text
GRAM
KILOGRAM
UNIT
```

Estados de disponibilidade:

```text
AVAILABLE
SOLD_OUT
```

Implementado:

- [x] Criação de variante
- [x] Consulta individual
- [x] Listagem por produto
- [x] Atualização
- [x] Alteração de preço
- [x] Alteração de quantidade/unidade
- [x] Marcação como disponível
- [x] Marcação como esgotada
- [x] Prevenção de variantes duplicadas
- [x] Validação da relação entre produto e variante
- [x] Consultas públicas
- [x] Operações administrativas protegidas

Uma combinação de:

```text
produto + quantidade + unidade
```

é única.

Exemplo: um mesmo produto não pode possuir duas variantes `100 GRAM`.

### Endpoints

```http
GET /products/{productId}/variants
GET /products/{productId}/variants/{variantId}

POST /products/{productId}/variants
PUT  /products/{productId}/variants/{variantId}

PATCH /products/{productId}/variants/{variantId}/available
PATCH /products/{productId}/variants/{variantId}/sold-out
```

As consultas são públicas.

Criação e alterações exigem role `ADMIN`.

---

# Regras de disponibilidade do catálogo

A disponibilidade comercial é determinada por três níveis:

```text
Categoria
   ↓
Produto
   ↓
Variante
```

Uma variante está efetivamente disponível para compra quando:

```text
categoria ativa
        &&
produto ACTIVE
        &&
variante AVAILABLE
```

A desativação de uma categoria funciona como uma barreira de disponibilidade para os produtos pertencentes a ela, sem alterar diretamente os estados internos desses produtos e variantes.

A desativação de um produto, por outro lado, marca suas variantes como `SOLD_OUT`.

---

# Verificação de e-mail

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

O reenvio possui cooldown controlado pelo backend através do Redis.

O fluxo também limita a quantidade de tentativas de validação de um mesmo código.

Atualmente são aceitos endereços dos seguintes provedores:

```text
gmail.com
hotmail.com
outlook.com
yahoo.com
```

A validação do provedor não substitui a verificação de propriedade do endereço.

---

# Recuperação de senha

A recuperação de senha utiliza códigos temporários enviados para o e-mail do usuário.

```text
Solicitação de recuperação
   ↓
Código aleatório de 6 dígitos
   ↓
Hash armazenado no Redis com TTL
   ↓
Código enviado por e-mail
   ↓
POST /auth/reset-password
   ↓
Validação e consumo do código
   ↓
Nova senha armazenada com BCrypt
   ↓
Sessões de refresh existentes revogadas
```

O código possui tempo de expiração, uso único e limite de tentativas.

A solicitação de novos códigos possui cooldown controlado pelo Redis.

Após uma alteração de senha bem-sucedida, todas as sessões de refresh token existentes do usuário são revogadas.

---

# Autenticação

A autenticação utiliza access tokens JWT e refresh tokens opacos.

## Access Token

O access token utiliza JWT e possui curta duração.

Configuração atual:

```text
15 minutos
```

É enviado através do header:

```http
Authorization: Bearer <access_token>
```

A validação é realizada pelo Spring Security OAuth2 Resource Server.

O token contém a identificação do usuário e suas roles.

A aplicação não utiliza identificadores enviados pelo cliente para determinar a identidade do usuário autenticado.

---

## Refresh Token

O refresh token é opaco e armazenado no navegador através de cookie HttpOnly.

As sessões correspondentes são armazenadas no Redis.

Configuração atual:

```text
7 dias
```

A aplicação implementa rotação de refresh tokens.

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

# Usuário autenticado

A identidade de um usuário autenticado é obtida a partir do JWT validado pelo Spring Security.

Recursos pertencentes a um usuário não devem confiar em `userId` enviado pelo frontend para determinar propriedade.

Endpoint:

```http
GET /users/me
```

Essa abordagem será reutilizada em recursos privados futuros, como endereços, carrinho e pedidos.

---

# Banco de dados

## PostgreSQL

Status: Implementado

O PostgreSQL é utilizado como banco de dados relacional principal.

O schema é versionado exclusivamente através do Flyway.

Hibernate é utilizado para validação:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

Open Session in View permanece desabilitado:

```yaml
spring:
  jpa:
    open-in-view: false
```

Estruturas atuais incluem:

```text
users
roles
user_roles

categories
products
product_variants

flyway_schema_history
```

As relações do catálogo são protegidas por constraints e foreign keys no banco.

As variantes possuem constraint de unicidade para:

```text
product_id + quantity + measurement_unit
```

Novas alterações estruturais são realizadas exclusivamente através de migrations Flyway.

---

## Redis

Status: Implementado

Atualmente utilizado para:

- sessões de refresh token;
- rotação de refresh token;
- consumo atômico de refresh tokens;
- códigos de verificação de e-mail;
- expiração dos códigos;
- consumo de uso único;
- cooldown de reenvio;
- controle de tentativas de verificação;
- códigos de recuperação de senha;
- cooldown de recuperação;
- controle de tentativas de recuperação;
- controle de tentativas de login;
- controle de tentativas de cadastro.

O Redis é utilizado principalmente para dados temporários e operações que exigem TTL ou atomicidade.

---

# Segurança

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
- [x] Roles `USER` e `ADMIN`
- [x] Revogação de refresh tokens
- [x] Rotação de refresh tokens
- [x] Proteção contra replay sequencial
- [x] Proteção contra consumo concorrente
- [x] Identidade obtida pelo contexto autenticado
- [x] Verificação de propriedade do e-mail
- [x] Códigos temporários com TTL
- [x] Uso único dos códigos
- [x] Cooldowns
- [x] Limites de tentativas
- [x] Recuperação segura de senha
- [x] Rate limiting de login
- [x] Rate limiting de cadastro
- [x] Leitura pública do catálogo
- [x] Gerenciamento do catálogo restrito a `ADMIN`

Planejado:

- [ ] Rate limiting global
- [ ] Hardening adicional de produção
- [ ] Proteções adicionais de infraestrutura

---

# Tratamento de erros

Status: Implementado

A API utiliza `ProblemDetail` para padronização das respostas HTTP de erro.

Exemplo:

```json
{
  "title": "Product not found",
  "status": 404,
  "detail": "Product not found",
  "instance": "/products/...",
  "timestamp": "..."
}
```

São tratados de forma padronizada:

- erros de autenticação;
- erros de autorização;
- erros de validação;
- recursos inexistentes;
- conflitos de negócio;
- categorias inativas;
- produtos inativos;
- variantes duplicadas;
- operações de segurança.

Informações sensíveis ou detalhes internos da infraestrutura não são expostos ao cliente.

---

# Testes

Os testes utilizam:

- JUnit 5
- Mockito
- MockMvc
- Spring Security Test
- Testcontainers

PostgreSQL e Redis reais são inicializados em containers durante os testes de integração.

## Identity e Authentication

Entre os cenários cobertos estão:

- [x] Registro
- [x] E-mail duplicado
- [x] E-mail case-insensitive
- [x] Login
- [x] Bloqueio antes da verificação
- [x] Senha incorreta
- [x] Usuário inexistente
- [x] Usuário desabilitado
- [x] Access token JWT
- [x] JWT inválido
- [x] Refresh token
- [x] Rotação
- [x] Reutilização
- [x] Logout
- [x] Revogação
- [x] CSRF
- [x] Concorrência no consumo do refresh token
- [x] Verificação de e-mail
- [x] Reenvio
- [x] Cooldown
- [x] Limite de tentativas
- [x] Recuperação de senha
- [x] Revogação após alteração de senha
- [x] Rate limiting de login
- [x] Rate limiting de cadastro

## Catálogo

A camada de aplicação possui testes focados nas principais regras de negócio.

Entre os cenários cobertos estão:

- [x] Criação de categoria
- [x] Prevenção de categoria duplicada
- [x] Renomeação de categoria
- [x] Alteração apenas de capitalização do nome
- [x] Criação de produto
- [x] Rejeição de criação em categoria inativa
- [x] Atualização de produto
- [x] Mudança de categoria
- [x] Ativação de produto
- [x] Desativação de produto
- [x] Propagação de disponibilidade para variantes
- [x] Criação de variante
- [x] Prevenção de variante duplicada
- [x] Atualização de variante
- [x] Disponibilização de variante
- [x] Rejeição de disponibilização quando o produto está inativo

Também existem testes HTTP de integração para:

- [x] API de categorias
- [x] API de produtos
- [x] API de variantes
- [x] Leitura pública do catálogo
- [x] Rejeição de operações administrativas sem autenticação
- [x] Rejeição de operações administrativas para `USER`
- [x] Operações administrativas para `ADMIN`
- [x] Validação de requests
- [x] Relações entre produtos e variantes

Os testes de integração não dependem dos containers PostgreSQL e Redis utilizados no ambiente de desenvolvimento.

O Docker Engine precisa estar disponível para execução dos Testcontainers.

---

# Contextos futuros

## Usuários

Status: Em desenvolvimento

Planejado:

- [ ] Evolução do perfil
- [ ] Endereços
- [ ] Recursos privados associados ao usuário

## Catálogo

Status: Em desenvolvimento

Base implementada:

- [x] Categorias
- [x] Produtos
- [x] Variantes
- [x] Estados de disponibilidade
- [x] Busca por nome
- [x] Ordenação
- [x] Paginação
- [x] API pública de consulta
- [x] Gerenciamento por `ADMIN`
- [x] Testes unitários
- [x] Testes HTTP de integração

Próximas evoluções:

- [ ] Filtro de produtos por categoria
- [ ] Integração real de imagens
- [ ] Evolução da API pública para consumo da vitrine
- [ ] Estado de arquivamento de produtos

## Carrinho

Status: Planejado

- [ ] A definir

## Pedidos

Status: Planejado

- [ ] A definir

## Pagamentos

Status: Planejado

- [ ] A definir

## Avaliações

Status: Planejado

- [ ] A definir

---

# Executando o projeto

## Pré-requisitos

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

Em ambientes com Compose legado:

```bash
docker-compose up -d
```

Configure as variáveis necessárias para integrações externas.

Exemplo:

```bash
export RESEND_API_KEY='<resend-api-key>'
```

Não armazene chaves reais no repositório.

Execute:

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

# Executando os testes

Execute toda a suíte:

```bash
./gradlew clean test
```

Para uma classe específica:

```bash
./gradlew test --tests "*NomeDaClasseDeTeste"
```

Os testes de integração utilizam Testcontainers.

Os containers PostgreSQL e Redis do ambiente de desenvolvimento não precisam estar em execução, mas o Docker Engine deve estar disponível.

---

# Configuração

Os arquivos de configuração são separados por ambiente:

```text
src/main/resources/
├── application.yml
├── application-dev.yml
└── application-prod.yml
```

Configurações sensíveis devem ser fornecidas através de variáveis de ambiente.

Exemplos:

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

# Roadmap

## Identity e Authentication

- [x] Registro
- [x] Login
- [x] JWT
- [x] Refresh token
- [x] Rotação
- [x] Logout
- [x] CSRF
- [x] CORS
- [x] Usuário autenticado
- [x] Verificação de e-mail
- [x] Envio de código
- [x] Reenvio
- [x] Cooldown
- [x] Limite de tentativas
- [x] Recuperação de senha
- [x] Rate limiting de login
- [x] Rate limiting de cadastro
- [x] Testes unitários
- [x] Testes de integração
- [ ] Rate limiting global
- [ ] Hardening adicional de produção

## Catálogo

- [x] Modelo de categorias
- [x] Modelo de produtos
- [x] Modelo de variantes
- [x] Persistência PostgreSQL
- [x] Migrations Flyway
- [x] API de categorias
- [x] API de produtos
- [x] API de variantes
- [x] Busca por nome
- [x] Ordenação
- [x] Paginação
- [x] Estados de produto
- [x] Estados de variante
- [x] Regras de ativação/desativação
- [x] Autorização administrativa
- [x] Consultas públicas
- [x] Testes unitários
- [x] Testes de integração
- [ ] Filtro por categoria
- [ ] Imagens de produtos
- [ ] API otimizada para vitrine
- [ ] Arquivamento de produtos

## Marketplace

- [ ] Perfil e endereços
- [ ] Carrinho
- [ ] Pedidos
- [ ] Pagamentos
- [ ] Avaliações

---

# Status

Projeto em desenvolvimento.

A infraestrutura base e o contexto de Identity e Authentication estão funcionais em ambiente de desenvolvimento.

O catálogo já possui suporte a categorias, produtos e variantes, incluindo persistência, regras de disponibilidade, busca, ordenação, paginação, autorização administrativa e endpoints públicos de consulta.

A suíte de testes cobre os principais fluxos de autenticação e as regras centrais do catálogo através de testes unitários e de integração.

O desenvolvimento seguirá de forma incremental, expandindo primeiro as funcionalidades necessárias para utilização do catálogo pela vitrine e posteriormente os demais contextos do marketplace.

---

# Autor

Daniel Rodrigues

Engenharia de Software - Universidade Federal do Ceará (UFC)

GitHub: `DwnlCR`
