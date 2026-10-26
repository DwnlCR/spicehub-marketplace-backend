
# SpiceHub Marketplace Backend

Backend do **SpiceHub**, um marketplace para comercialização de ervas, temperos e produtos agrícolas.

O projeto está em desenvolvimento e tem como objetivo construir uma API completa para gerenciamento de usuários, autenticação, catálogo de produtos, carrinho, pedidos, pagamentos e demais operações necessárias para o funcionamento do marketplace.

Atualmente, a infraestrutura base, os contextos de Identity e Authentication, o catálogo de produtos e o módulo de carrinho estão implementados em ambiente de desenvolvimento.

O catálogo inclui gerenciamento de categorias, produtos, variantes e imagens, com integração ao **Neon Object Storage**, processamento de imagens em WebP, arquivamento de produtos e controle de concorrência otimista.

O carrinho permite consultar, adicionar, atualizar e remover itens, com validação de disponibilidade junto ao catálogo.

---

## Tecnologias

### Backend

- Java 21
- Spring Boot 4.1.1
- Spring Security
- OAuth2 Resource Server
- JWT
- Spring Data JPA
- Gradle

### Persistência e infraestrutura

- PostgreSQL
- Redis
- Flyway
- Docker
- Docker Compose
- Neon Object Storage
- AWS SDK for Java (S3)

### Integrações

- Resend — envio de e-mails
- Armazenamento de objetos compatível com Amazon S3
- Processamento e conversão de imagens para WebP

### Testes

- JUnit 5
- Mockito
- MockMvc
- Spring Security Test
- Testcontainers

---

## Arquitetura

O projeto utiliza uma organização baseada em **Domain-Driven Design (DDD)**, com separação de responsabilidades entre domínio, aplicação, infraestrutura e apresentação.

### Estrutura atual

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
├── cart
│   ├── domain
│   ├── application
│   ├── infrastructure
│   └── presentation
│
├── shared
│   ├── infrastructure
│   └── presentation
│
└── SpicehubMarketplaceApplication.java
```

### Responsabilidades das camadas

| Camada | Responsabilidade |
|---|---|
| Domain | Entidades, Value Objects, regras e invariantes de negócio |
| Application | Casos de uso e contratos necessários para executar as operações |
| Infrastructure | Persistência, Redis, segurança, processamento de imagens e integrações externas |
| Presentation | Controllers HTTP, DTOs, requests, responses e tratamento de erros |

As dependências seguem, sempre que possível, a direção:

```text
Presentation
      ↓
Application
      ↓
Domain

Infrastructure → Implementações dos contratos
```

O domínio permanece independente dos detalhes de persistência e dos modelos JPA.

O catálogo utiliza contratos para operações de armazenamento e processamento de imagens, permitindo separar as regras da aplicação das implementações de infraestrutura.

O carrinho utiliza contratos para persistência e consulta de informações comerciais das variantes, mantendo as regras de negócio separadas do catálogo e dos detalhes de infraestrutura.

Funcionalidades compartilhadas, como configurações de infraestrutura e tratamento global de erros HTTP, permanecem no pacote `shared`.

---

# Funcionalidades

## Identity e Authentication

**Status:** Implementado em ambiente de desenvolvimento.

### Cadastro e autenticação

- [x] Cadastro de usuários
- [x] Normalização de e-mail
- [x] Comparação case-insensitive de e-mails
- [x] Validação de provedores de e-mail permitidos
- [x] Senhas armazenadas com BCrypt
- [x] Roles `USER` e `ADMIN`
- [x] Login
- [x] Bloqueio de login para e-mail não verificado
- [x] Autenticação automática após verificação bem-sucedida de e-mail
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
- [x] Configuração CORS
- [x] Identificação segura do usuário autenticado
- [x] Endpoint `/users/me`

### Verificação de e-mail

- [x] Código de verificação de seis dígitos
- [x] Expiração do código
- [x] Uso único
- [x] Invalidação do código anterior após reenvio
- [x] Envio através do Resend
- [x] Reenvio de código
- [x] Cooldown de reenvio
- [x] Limite de tentativas de validação
- [x] Emissão de access token após verificação
- [x] Emissão de refresh token após verificação
- [x] Autenticação automática do usuário verificado

### Recuperação de senha

- [x] Solicitação de recuperação
- [x] Código temporário de seis dígitos
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

**Status:** Base funcional implementada.

O contexto de catálogo gerencia categorias, produtos, variantes e imagens de produtos.

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

O produto também pode possuir uma imagem armazenada externamente, referenciada pelo campo `imageKey`.

---

## Categorias

### Funcionalidades implementadas

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

### Funcionalidades implementadas

- [x] Criação
- [x] Consulta individual
- [x] Listagem
- [x] Atualização
- [x] Ativação
- [x] Desativação
- [x] Arquivamento
- [x] Desarquivamento
- [x] Associação com categoria
- [x] Busca por nome
- [x] Filtro por categoria
- [x] Paginação
- [x] Ordenação alfabética crescente
- [x] Ordenação alfabética decrescente
- [x] Estado `ACTIVE`
- [x] Estado `INACTIVE`
- [x] Consultas públicas
- [x] Operações administrativas protegidas
- [x] Associação de imagem ao produto
- [x] Controle de concorrência otimista

### Estados

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

### Arquivamento

O arquivamento permite retirar um produto da vitrine pública sem necessariamente excluir seu registro do banco de dados.

Essa operação é diferente da desativação comercial.

O desarquivamento permite restaurar a visibilidade do produto conforme as regras implementadas no catálogo.

### Busca, ordenação e paginação

A listagem suporta busca, ordenação, filtro por categoria e paginação.

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

## Imagens de produtos

**Status:** Implementado e validado em ambiente de desenvolvimento.

O SpiceHub utiliza o **Neon Object Storage** para armazenamento das imagens dos produtos.

A integração é realizada através do **AWS SDK for Java**, utilizando a API compatível com Amazon S3 disponibilizada pelo serviço.

O armazenamento de objetos permanece separado do banco de dados relacional.

O PostgreSQL armazena apenas a referência da imagem, através do campo `imageKey`.

### Funcionalidades implementadas

- [x] Upload de imagens
- [x] Validação de arquivos enviados
- [x] Suporte a imagens JPEG, PNG e WebP
- [x] Conversão para WebP
- [x] Redimensionamento de imagens
- [x] Limites de tamanho e resolução
- [x] Armazenamento em bucket privado
- [x] Associação de imagem ao produto
- [x] Substituição de imagens
- [x] Exclusão da imagem anterior após substituição
- [x] Exclusão de imagens
- [x] Remoção da referência `imageKey`
- [x] Recuperação pública da imagem
- [x] Resposta HTTP com `Content-Type: image/webp`
- [x] Controle de concorrência otimista
- [x] Tratamento de falhas de processamento
- [x] Tratamento de falhas de armazenamento
- [x] Testes unitários
- [x] Testes HTTP de integração

### Arquitetura da funcionalidade

```text
catalog
│
├── application
│   └── usecase
│       ├── UploadProductImageUseCase
│       ├── GetProductImageUseCase
│       └── DeleteProductImageUseCase
│
├── domain
│   ├── image
│   │   └── ImageProcessor
│   │
│   └── storage
│       └── ImageStorage
│
├── infrastructure
│   ├── image
│   │   └── ProductImageProcessor
│   │
│   ├── storage
│   │   └── S3ImageStorage
│   │
│   └── exception
│       ├── ImageStorageException
│       ├── InvalidProductImageException
│       └── ProductImageProcessingException
│
└── presentation
    └── http
        └── controller
            └── ProductController
```

A configuração do cliente S3 permanece no módulo compartilhado:

```text
shared
└── infrastructure
    └── config
        └── S3ClientConfig
```

### Fluxo de upload

```text
Cliente HTTP
    ↓
PUT /products/{productId}/image
    ↓
ProductController
    ↓
UploadProductImageUseCase
    ↓
Validação do produto
    ↓
Processamento da imagem
    ↓
Conversão para WebP
    ↓
Armazenamento no Neon Object Storage
    ↓
Atualização do imageKey
    ↓
Resposta HTTP
```

O processamento das imagens é realizado antes do armazenamento.

A aplicação não depende do formato original do arquivo para disponibilizar a imagem posteriormente, pois os arquivos processados são padronizados em WebP.

### Armazenamento

O bucket utilizado no ambiente de desenvolvimento é privado.

A aplicação utiliza credenciais próprias para realizar operações de armazenamento e recuperação.

A referência ao objeto é mantida no produto através de `imageKey`.

Essa abordagem evita armazenar arquivos binários diretamente no PostgreSQL.

### Substituição de imagens

O endpoint de upload também permite substituir a imagem existente.

O fluxo contempla:

1. Recebimento da nova imagem.
2. Validação e processamento.
3. Armazenamento do novo arquivo.
4. Atualização da referência no produto.
5. Exclusão do arquivo anterior.

A substituição evita manter imagens antigas desnecessariamente após uma operação bem-sucedida.

### Exclusão de imagens

A exclusão remove o arquivo do armazenamento e limpa a referência associada ao produto.

Após a operação, o campo `imageKey` deixa de apontar para um arquivo.

### Recuperação pública

A API disponibiliza a recuperação da imagem por meio do endpoint:

```http
GET /products/{productId}/image
```

A resposta contém os bytes da imagem processada:

```http
HTTP/1.1 200 OK
Content-Type: image/webp
```

Esse endpoint é público e pode ser consumido diretamente por navegadores e aplicações frontend.

O bucket não precisa ser público para permitir a exibição das imagens.

O backend realiza a leitura do objeto e retorna seu conteúdo ao cliente.

Quando o produto não possui imagem, a API retorna `404 Not Found`.

### Endpoints

```http
PUT    /products/{productId}/image
GET    /products/{productId}/image
DELETE /products/{productId}/image
```

| Método | Endpoint | Finalidade | Acesso |
|---|---|---|---|
| PUT | `/products/{productId}/image` | Upload ou substituição | ADMIN |
| GET | `/products/{productId}/image` | Recuperação da imagem | Público |
| DELETE | `/products/{productId}/image` | Exclusão da imagem | ADMIN |

As operações administrativas seguem as regras de autenticação, autorização e proteção CSRF da aplicação.

### Exemplo de consumo no frontend

```jsx
function ProductImage({ product }) {
    const API_URL = "http://localhost:8080"

    return (
        <img
            src={`${API_URL}/products/${product.id}/image`}
            alt={product.name}
            loading="lazy"
        />
    )
}

export default ProductImage
```

Como a consulta é pública, não é necessário enviar um access token JWT para exibir a imagem.

### Controle de concorrência otimista

O produto possui versionamento na camada de persistência.

Esse mecanismo permite detectar atualizações concorrentes e evitar sobrescritas silenciosas durante operações que modificam o estado do produto.

A alteração do schema foi realizada através da migration:

```text
V4__add_product_version.sql
```

### Testes

A implementação possui testes para processamento de imagens, casos de uso e endpoints HTTP.

Entre os cenários validados estão:

- [x] Processamento de imagens
- [x] Upload de imagem
- [x] Recuperação de imagem
- [x] Produto sem imagem
- [x] Produto inexistente
- [x] Resposta HTTP com imagem WebP
- [x] Retorno de `404 Not Found`
- [x] Controle de concorrência otimista
- [x] Substituição de imagem no Neon
- [x] Remoção do arquivo anterior
- [x] Exclusão da imagem no Neon
- [x] Limpeza do `imageKey`
- [x] Acesso público à imagem pelo navegador

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

### Unidades suportadas

```text
GRAM
KILOGRAM
UNIT
```

### Estados de disponibilidade

```text
AVAILABLE
SOLD_OUT
```

### Funcionalidades implementadas

- [x] Criação de variante
- [x] Consulta individual
- [x] Listagem por produto
- [x] Atualização
- [x] Alteração de preço
- [x] Alteração de quantidade e unidade
- [x] Marcação como disponível
- [x] Marcação como esgotada
- [x] Prevenção de variantes duplicadas
- [x] Validação da relação entre produto e variante
- [x] Consultas públicas
- [x] Operações administrativas protegidas

A combinação de produto, quantidade e unidade é única.

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

Essas regras também são consideradas nas operações de carrinho que exigem disponibilidade comercial.

---

# Verificação de e-mail

O cadastro utiliza verificação de propriedade do endereço de e-mail.

### Fluxo

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
Geração do access token JWT
    ↓
Criação da sessão de refresh token
    ↓
Refresh token enviado em cookie HttpOnly
    ↓
Usuário autenticado automaticamente
```

Após uma verificação bem-sucedida, o backend retorna os dados de autenticação no mesmo formato utilizado pelo login.

O frontend não precisa realizar uma segunda requisição de login após a confirmação do código.

O código possui duração configurável, atualmente definida em:

```yaml
security:
  email-verification:
    code-ttl: 10m
```

Somente o hash do código é armazenado no Redis.

Quando um novo código é emitido, o anterior deixa de ser válido.

Após uma verificação bem-sucedida, o código é removido e não pode ser reutilizado.

O reenvio possui cooldown controlado pelo backend através do Redis.

O fluxo também limita a quantidade de tentativas de validação.

### Provedores aceitos

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

### Fluxo

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

## Refresh Token

O refresh token é opaco e armazenado no navegador através de cookie HttpOnly.

As sessões correspondentes são armazenadas no Redis.

Configuração atual:

```text
7 dias
```

A aplicação implementa rotação de refresh tokens.

```text
Token atual
    ↓
Consumido atomicamente
    ↓
Invalidado
    ↓
Novo refresh token emitido
```

O consumo é realizado atomicamente no Redis para impedir reutilização sequencial e consumo concorrente do mesmo token.

O frontend não precisa e não deve acessar diretamente o valor do refresh token.

## Resposta de autenticação

As operações de login e verificação de e-mail bem-sucedida retornam um access token e informações do usuário autenticado.

O objeto de usuário contém:

- `id`
- `name`
- `roles`

O e-mail não faz parte do objeto `user` retornado pela resposta de login.

O refresh token é enviado separadamente através de cookie HttpOnly.

---

# Usuário autenticado

A identidade de um usuário autenticado é obtida a partir do JWT validado pelo Spring Security.

Recursos pertencentes a um usuário não devem confiar em `userId` enviado pelo frontend para determinar propriedade.

Endpoint:

```http
GET /users/me
```

Essa abordagem também é utilizada pelo módulo de carrinho, garantindo que cada usuário acesse apenas os próprios itens.

---

# Carrinho de compras

**Status:** Implementado em ambiente de desenvolvimento.

O contexto de carrinho gerencia os itens selecionados pelo usuário autenticado, mantendo a separação entre regras de negócio, persistência e informações comerciais do catálogo.

O carrinho é associado ao usuário identificado pelo contexto de autenticação.

## Funcionalidades implementadas

- [x] Modelagem do domínio `Cart`
- [x] Modelagem do domínio `CartItem`
- [x] Associação do carrinho ao usuário autenticado
- [x] Persistência do carrinho
- [x] Consulta do carrinho
- [x] Adição de variantes
- [x] Acumulação de quantidades ao adicionar uma variante existente
- [x] Atualização da quantidade de um item
- [x] Remoção de um item
- [x] Limpeza de todos os itens
- [x] Validação de quantidades positivas
- [x] Integração com o catálogo para consulta de variantes
- [x] Validação de disponibilidade comercial
- [x] Casos de uso separados por operação
- [x] Endpoints REST
- [x] Testes unitários dos casos de uso

## Estrutura de domínio

```text
Cart
├── id
├── userId
├── items
├── createdAt
└── updatedAt

CartItem
├── id
├── variantId
└── quantity
```

O carrinho mantém os identificadores das variantes e as quantidades solicitadas.

As informações comerciais, como nome, preço e disponibilidade, pertencem ao catálogo e são consultadas através da integração entre os contextos.

## Regras de negócio

### Associação ao usuário

O carrinho pertence ao usuário autenticado.

O `userId` é obtido pelo backend através de `CurrentUserProvider`, não sendo enviado pelo frontend nas operações.

### Adição de itens

Ao adicionar uma variante:

1. O backend identifica o usuário autenticado.
2. Consulta as informações da variante no catálogo.
3. Valida as regras de disponibilidade.
4. Localiza ou cria o carrinho do usuário.
5. Adiciona a variante ou acumula sua quantidade.
6. Persiste a alteração.

Uma mesma variante não precisa gerar múltiplas linhas no carrinho.

### Quantidades

A quantidade de cada item deve ser maior que zero.

A atualização de quantidade utiliza uma operação específica e não deve ser confundida com a adição de novas unidades.

### Remoção

A remoção de um item utiliza o identificador da variante.

Quando o item não existe no carrinho, o domínio pode lançar `CartItemNotFoundException`.

### Limpeza do carrinho

A limpeza remove todos os itens do carrinho.

Quando o carrinho não existe ou já está vazio, a operação termina normalmente, sem realizar persistência desnecessária.

Esse comportamento permite que a operação seja idempotente.

## Endpoints

| Método | Endpoint | Finalidade | Sucesso |
|---|---|---|---|
| GET | `/cart` | Consultar carrinho | 200 OK |
| POST | `/cart/items` | Adicionar variante | 204 No Content |
| PATCH | `/cart/items/{variantId}` | Atualizar quantidade | 204 No Content |
| DELETE | `/cart/items/{variantId}` | Remover variante | 204 No Content |
| DELETE | `/cart/items` | Limpar carrinho | 204 No Content |

Todos os endpoints do carrinho exigem autenticação.

### Consultar carrinho

```http
GET /cart
Authorization: Bearer <access_token>
```

A resposta é construída através de `CartResponseMapper`, utilizando as informações do carrinho e os dados comerciais necessários à apresentação.

### Adicionar item

```http
POST /cart/items
Authorization: Bearer <access_token>
Content-Type: application/json
```

```json
{
  "variantId": "00000000-0000-0000-0000-000000000001",
  "quantity": 2
}
```

Resposta:

```http
HTTP/1.1 204 No Content
```

### Atualizar quantidade

```http
PATCH /cart/items/00000000-0000-0000-0000-000000000001
Authorization: Bearer <access_token>
Content-Type: application/json
```

```json
{
  "quantity": 3
}
```

Resposta:

```http
HTTP/1.1 204 No Content
```

### Remover item

```http
DELETE /cart/items/00000000-0000-0000-0000-000000000001
Authorization: Bearer <access_token>
```

Resposta:

```http
HTTP/1.1 204 No Content
```

### Limpar carrinho

```http
DELETE /cart/items
Authorization: Bearer <access_token>
```

Resposta:

```http
HTTP/1.1 204 No Content
```

O endpoint retorna `204 No Content` mesmo quando não existe carrinho para o usuário.

## Organização

O contexto utiliza a mesma separação arquitetural dos demais módulos.

```text
cart
│
├── domain
│   ├── model
│   │   ├── Cart
│   │   └── CartItem
│   ├── repository
│   │   └── CartRepository
│   └── exception
│
├── application
│   ├── usecase
│   │   ├── GetCartUseCase
│   │   ├── AddCartItemUseCase
│   │   ├── UpdateCartItemQuantityUseCase
│   │   ├── RemoveCartItemUseCase
│   │   └── ClearCartUseCase
│   └── exception
│
├── infrastructure
│
└── presentation
    ├── controller
    │   └── CartController
    ├── dto
    └── mapper
        └── CartResponseMapper
```

## Testes

Os testes dos casos de uso validam os principais comportamentos do carrinho.

Entre os cenários cobertos estão:

- [x] Consulta do carrinho
- [x] Adição de itens
- [x] Atualização de quantidades
- [x] Remoção de itens
- [x] Rejeição de item inexistente
- [x] Rejeição de carrinho inexistente na remoção individual
- [x] Limpeza de carrinho com itens
- [x] Limpeza de carrinho vazio
- [x] Limpeza de carrinho inexistente
- [x] Verificação de chamadas de persistência
- [ ] Validação completa dos endpoints HTTP com integração real

A validação HTTP completa do módulo de carrinho permanece como uma etapa de consolidação antes do desenvolvimento de pedidos.

---

# Banco de dados

## PostgreSQL

**Status:** Implementado.

O PostgreSQL é utilizado como banco de dados relacional principal.

O schema é versionado através do Flyway.

O Hibernate é utilizado para validação:

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

### Estruturas principais

```text
users
roles
user_roles

categories
products
product_variants

carts
cart_items

flyway_schema_history
```

As relações do catálogo são protegidas por constraints e foreign keys no banco.

As variantes possuem constraint de unicidade para:

```text
product_id + quantity + measurement_unit
```

O produto possui referência à imagem armazenada externamente e versionamento para controle de concorrência otimista.

O carrinho possui persistência própria, associada ao usuário e às variantes do catálogo.

### Migrations

As alterações estruturais são realizadas através de migrations Flyway.

Entre as alterações implementadas está:

```text
V4__add_product_version.sql
```

Essa migration adiciona suporte ao versionamento utilizado no controle de concorrência otimista dos produtos.

O desenvolvimento dos novos contextos também utiliza migrations para manter o schema versionado.

---

## Redis

**Status:** Implementado.

Atualmente utilizado para:

- Sessões de refresh token
- Rotação de refresh token
- Consumo atômico de refresh tokens
- Códigos de verificação de e-mail
- Expiração dos códigos
- Consumo de uso único
- Cooldown de reenvio
- Controle de tentativas de verificação
- Códigos de recuperação de senha
- Cooldown de recuperação
- Controle de tentativas de recuperação
- Controle de tentativas de login
- Controle de tentativas de cadastro

O Redis é utilizado principalmente para dados temporários e operações que exigem TTL ou atomicidade.

O carrinho utiliza persistência relacional, mantendo o Redis dedicado às responsabilidades temporárias e de segurança atualmente implementadas.

---

# Armazenamento de objetos

## Neon Object Storage

**Status:** Implementado em ambiente de desenvolvimento.

O Neon Object Storage é utilizado para armazenar as imagens dos produtos.

A integração utiliza uma API compatível com Amazon S3.

### Responsabilidades

- Armazenamento dos arquivos processados
- Recuperação das imagens
- Exclusão de arquivos
- Isolamento do conteúdo binário em relação ao PostgreSQL

O banco de dados armazena a referência da imagem através de `imageKey`.

A implementação concreta de armazenamento está localizada em:

```text
catalog/infrastructure/storage/S3ImageStorage.java
```

A configuração do cliente S3 está localizada em:

```text
shared/infrastructure/config/S3ClientConfig.java
```

### Variáveis de ambiente

```dotenv
AWS_ENDPOINT_URL_S3=
AWS_ACCESS_KEY_ID=
AWS_SECRET_ACCESS_KEY=
AWS_REGION=us-east-2
```

As credenciais reais devem permanecer fora do controle de versão.

O arquivo `.env.example` documenta as variáveis necessárias sem conter segredos.

O arquivo `.env` deve permanecer ignorado pelo Git.

---

# Segurança

### Implementado

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
- [x] Autenticação automática após verificação de e-mail
- [x] Códigos temporários com TTL
- [x] Uso único dos códigos
- [x] Cooldowns
- [x] Limites de tentativas
- [x] Recuperação segura de senha
- [x] Rate limiting de login
- [x] Rate limiting de cadastro
- [x] Leitura pública do catálogo
- [x] Gerenciamento do catálogo restrito a `ADMIN`
- [x] Gerenciamento administrativo de imagens
- [x] Armazenamento privado de imagens
- [x] Recuperação pública através de endpoint HTTP
- [x] Carrinho associado ao usuário autenticado
- [x] Operações privadas sem confiar em `userId` enviado pelo cliente

### Planejado

- [ ] Rate limiting global
- [ ] Hardening adicional de produção
- [ ] Proteções adicionais de infraestrutura

---

# Tratamento de erros

**Status:** Implementado.

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

- Erros de autenticação
- Erros de autorização
- Erros de validação
- Recursos inexistentes
- Conflitos de negócio
- Categorias inativas
- Produtos inativos
- Variantes duplicadas
- Operações de segurança
- Imagens inválidas
- Falhas de processamento de imagens
- Falhas de armazenamento
- Exceções de negócio do carrinho

Informações sensíveis ou detalhes internos da infraestrutura não devem ser expostos ao cliente.

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
- [x] Autenticação automática após verificação
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

### Imagens de produtos

Os testes automatizados incluem:

- [x] Casos de uso de upload
- [x] Casos de uso de recuperação
- [x] Processamento de imagens
- [x] Recuperação de produto com imagem
- [x] Recuperação de produto sem imagem
- [x] Recuperação de produto inexistente
- [x] Resposta HTTP com imagem WebP
- [x] Resposta HTTP `404 Not Found`
- [x] Controle de concorrência otimista

### Testes HTTP de integração

- [x] API de categorias
- [x] API de produtos
- [x] API de variantes
- [x] Leitura pública do catálogo
- [x] Rejeição de operações administrativas sem autenticação
- [x] Rejeição de operações administrativas para `USER`
- [x] Operações administrativas para `ADMIN`
- [x] Validação de requests
- [x] Relações entre produtos e variantes
- [x] Recuperação pública de imagem
- [x] Retorno dos bytes da imagem
- [x] Retorno de `404` para imagem ausente
- [x] Retorno de `404` para produto inexistente

### Validação manual

Os fluxos também foram testados manualmente utilizando Postman e Neon Object Storage.

- [x] Upload de imagem
- [x] Verificação do arquivo armazenado
- [x] Atualização de `imageKey`
- [x] Substituição de imagem
- [x] Remoção da imagem anterior
- [x] Exclusão de imagem
- [x] Remoção do arquivo no armazenamento
- [x] Limpeza do `imageKey`
- [x] Download público
- [x] Exibição da imagem no navegador

Os testes de integração não dependem dos containers PostgreSQL e Redis utilizados no ambiente de desenvolvimento.

O Docker Engine precisa estar disponível para execução dos Testcontainers.

---

# Contextos futuros

## Usuários

**Status:** Em desenvolvimento.

Planejado:

- [ ] Evolução do perfil
- [ ] Endereços
- [ ] Recursos privados associados ao usuário

## Catálogo

**Status:** Base funcional implementada, com evoluções planejadas.

### Implementado

- [x] Categorias
- [x] Produtos
- [x] Variantes
- [x] Estados de disponibilidade
- [x] Busca por nome
- [x] Ordenação
- [x] Paginação
- [x] Filtro por categoria
- [x] Arquivamento de produtos
- [x] API pública de consulta
- [x] Gerenciamento por `ADMIN`
- [x] Armazenamento de imagens
- [x] Processamento WebP
- [x] Upload e substituição de imagens
- [x] Exclusão de imagens
- [x] Recuperação pública de imagens
- [x] Controle de concorrência otimista
- [x] Testes unitários
- [x] Testes HTTP de integração

### Próximas evoluções

- [ ] Evolução da API pública para consumo da vitrine
- [ ] Integração completa do catálogo com o frontend
- [ ] Otimizações de consultas conforme a demanda

## Carrinho

**Status:** Funcionalidades principais implementadas.

### Implementado

- [x] Modelagem do carrinho
- [x] Modelagem dos itens
- [x] Associação ao usuário autenticado
- [x] Persistência
- [x] Consulta
- [x] Adição de itens
- [x] Atualização de quantidades
- [x] Remoção de itens
- [x] Limpeza do carrinho
- [x] Integração com produtos e variantes
- [x] Validação de disponibilidade
- [x] Testes unitários

### Próximas evoluções

- [ ] Consolidação dos testes HTTP de integração
- [ ] Integração com o frontend
- [ ] Integração com a criação de pedidos
- [ ] Revalidação de disponibilidade no fechamento do pedido

## Pedidos

**Status:** Planejado.

- [ ] Modelagem de pedidos
- [ ] Criação de pedidos
- [ ] Acompanhamento de status
- [ ] Histórico de pedidos

## Pagamentos

**Status:** Planejado.

- [ ] Definição do provedor de pagamentos
- [ ] Integração com pedidos
- [ ] Processamento de pagamentos
- [ ] Tratamento de notificações

## Avaliações

**Status:** Planejado.

- [ ] Modelagem de avaliações
- [ ] Avaliação de produtos
- [ ] Consulta de avaliações

---

# Executando o projeto

## Pré-requisitos

- Java 21
- Docker
- Docker Compose

### 1. Clonar o repositório

```bash
git clone git@github.com:DwnlCR/spicehub-marketplace-backend.git
```

Entre no diretório:

```bash
cd spicehub-marketplace-backend
```

### 2. Iniciar PostgreSQL e Redis

```bash
docker compose up -d
```

Em ambientes com Compose legado:

```bash
docker-compose up -d
```

### 3. Configurar variáveis de ambiente

Crie o arquivo `.env` a partir do exemplo:

```bash
cp .env.example .env
```

Configure as credenciais necessárias para as integrações externas.

Exemplo para o armazenamento de imagens:

```dotenv
AWS_ENDPOINT_URL_S3=
AWS_ACCESS_KEY_ID=
AWS_SECRET_ACCESS_KEY=
AWS_REGION=us-east-2
```

Também devem ser configuradas as variáveis necessárias para PostgreSQL, Redis, JWT e Resend.

O arquivo `.env` não deve ser versionado.

Caso as variáveis sejam carregadas a partir do terminal Linux:

```bash
set -a
source .env
set +a
```

### 4. Executar a aplicação

```bash
./gradlew bootRun
```

### Portas do ambiente de desenvolvimento

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

Para executar o build completo:

```bash
./gradlew clean build
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
├── application-prod.yml
└── application-test.yml
```

Configurações sensíveis devem ser fornecidas através de variáveis de ambiente.

### Exemplos

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

AWS_ENDPOINT_URL_S3
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
AWS_REGION
```

Segredos não devem ser versionados no Git.

O arquivo `.env.example` pode ser versionado para documentar a configuração necessária, desde que não contenha credenciais reais.

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
- [x] Autenticação automática após verificação
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
- [x] Filtro por categoria
- [x] Arquivamento de produtos
- [x] Estados de produto
- [x] Estados de variante
- [x] Regras de ativação e desativação
- [x] Autorização administrativa
- [x] Consultas públicas
- [x] Integração com Neon Object Storage
- [x] Processamento de imagens WebP
- [x] Upload de imagens
- [x] Substituição de imagens
- [x] Exclusão de imagens
- [x] Recuperação pública de imagens
- [x] Controle de concorrência otimista
- [x] Testes unitários
- [x] Testes de integração
- [ ] Integração completa com o frontend

## Carrinho

- [x] Modelo de carrinho
- [x] Modelo de itens
- [x] Persistência PostgreSQL
- [x] Associação ao usuário autenticado
- [x] API de consulta
- [x] API de adição
- [x] API de atualização de quantidade
- [x] API de remoção
- [x] API de limpeza
- [x] Validação de quantidades
- [x] Integração com catálogo
- [x] Validação de disponibilidade
- [x] Testes unitários
- [ ] Consolidação dos testes HTTP de integração
- [ ] Integração com frontend
- [ ] Integração com pedidos

## Marketplace

- [ ] Perfil e endereços
- [x] Carrinho — funcionalidades principais
- [ ] Pedidos
- [ ] Pagamentos
- [ ] Avaliações

---

# Status

O SpiceHub está em desenvolvimento.

A infraestrutura base e o contexto de Identity e Authentication estão funcionais em ambiente de desenvolvimento.

O fluxo de verificação de e-mail realiza autenticação automática após a confirmação bem-sucedida, emitindo access token JWT e refresh token opaco.

O catálogo possui suporte a categorias, produtos, variantes e imagens, incluindo persistência, regras de disponibilidade, busca, ordenação, paginação, filtro por categoria, arquivamento, autorização administrativa e endpoints públicos de consulta.

O gerenciamento de imagens utiliza Neon Object Storage, processamento WebP, substituição, exclusão e recuperação pública por HTTP.

O armazenamento permanece privado, enquanto o backend disponibiliza as imagens para consumo pela aplicação frontend.

O produto também possui controle de concorrência otimista, com versionamento persistido no PostgreSQL.

O módulo de carrinho possui domínio próprio, persistência, integração com o catálogo e endpoints para consulta, adição, atualização, remoção e limpeza dos itens.

A suíte de testes cobre os principais fluxos de autenticação, as regras centrais do catálogo e os casos de uso do carrinho.

A consolidação dos testes HTTP do carrinho e a integração com o frontend permanecem como próximas etapas.

O desenvolvimento seguirá de forma incremental, priorizando a integração da vitrine e do carrinho com o frontend e, posteriormente, a implementação de pedidos e pagamentos.

---

# Autor

**Daniel Rodrigues**

Engenharia de Software — Universidade Federal do Ceará (UFC)

GitHub: [DwnlCR](https://github.com/DwnlCR)
