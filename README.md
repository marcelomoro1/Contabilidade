# Sistema de Gerenciamento de vendas

![Banner do Sistema](https://img.shields.io/badge/Sistema%20de%20Contabilidade-v1.0-blue)
![Java](https://img.shields.io/badge/Java-17-red)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.x-blue)
![Bootstrap](https://img.shields.io/badge/Bootstrap-5.3-yellow)

## 📋 Índice

- [Sobre o Projeto](#-sobre-o-projeto)
- [Funcionalidades](#-funcionalidades)
- [Tecnologias Utilizadas](#-tecnologias-utilizadas)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Instalação e Execução](#-instalação-e-execução)
- [Screenshots](#-screenshots)
- [Contribuição](#-contribuição)
- [Licença](#-licença)

## 🚀 Sobre o Projeto

O Projeto é uma aplicação web desenvolvida para gerenciar operações comerciais de uma loja, incluindo vendas, clientes, fornecedores e produtos. O sistema permite o controle de estoque, registro de vendas, cadastro de clientes, fornecedores e relatórios sobre a contabilidade da empresa

## ✨ Funcionalidades

### Gestão de Clientes
- Cadastro de clientes (pessoa física e jurídica)
- Visualização de detalhes do cliente
- Edição e exclusão de clientes
- Validação de CPF/CNPJ

### Gestão de Fornecedores
- Cadastro de fornecedores
- Visualização de detalhes do fornecedor
- Edição e exclusão de fornecedores
- Validação de CNPJ

### Gestão de Produtos
- Cadastro de produtos com preços de compra e venda
- Controle de estoque
- Cálculo automático de crédito e débito de ICMS
- Visualização, edição e exclusão de produtos

### Gestão de Vendas
- Registro de vendas com cliente e produto
- Seleção de forma de pagamento (à vista ou a prazo)
- Cálculo automático de valores totais
- Visualização de detalhes da venda
- Exclusão de vendas

### Contabilidade
- Balanço Patrimonial: Visualização do Balanço Patrimonial com Ativos (Caixa e Equivalentes, Estoque de Produtos), Passivos (Contas a Pagar Fornecedores) e Patrimônio Líquido (Capital Social, Lucros/Prejuízos Acumulados).
- Caixa: Controle de entradas e saídas de dinheiro, e visualização do saldo atual do caixa.
- Bens (Patrimônio): Listagem de bens com ID, Nome, Descrição, Tipo, Valor de Aquisição, Data de Aquisição, Valor de Entrada, Parcelas Totais, Parcelas Pagas, Valor da Parcela, Data do 1º Vencimento, Fornecedor e Saldo Devedor. Permite cadastrar novos itens.
- Capital Social: Registro e histórico do Capital Social da empresa.
  
## 🛠 Tecnologias Utilizadas

- **Backend:**
  - Java 17
  - Spring Boot 3.x
  - Spring MVC
  - Spring Data JPA
  - Hibernate
  - Maven

- **Frontend:**
  - Thymeleaf
  - Bootstrap 5.3
  - jQuery
  - Bootstrap Icons
  - jQuery Mask Plugin

- **Banco de Dados:**
  - MySQL (produção)

## 📁 Estrutura do Projeto

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── loja/
│   │           ├── controller/       # Controladores REST e MVC
│   │           ├── model/            # Entidades JPA
│   │           │   └── enums/        # Enumeradores
│   │           ├── repository/       # Repositórios JPA
│   │           ├── service/          # Camada de serviço
│   │           └── LojaApplication.java
│   └── resources/
│       ├── static/
│       │   ├── css/                  # Estilos CSS
│       │   └── js/                   # Scripts JavaScript
│       ├── templates/                # Templates Thymeleaf
│       │   ├── clientes/             # Páginas de clientes
│       │   ├── fornecedores/         # Páginas de fornecedores
│       │   ├── produtos/             # Páginas de produtos
│       │   └── vendas/               # Páginas de vendas
│       └── application.properties    # Configurações da aplicação
```

## 🚀 Instalação e Execução

### Pré-requisitos
- Java 17 ou superior
- Maven 3.6 ou superior
- Git

### Passos para instalação

1. Clone o repositório:
```bash
git clone https://github.com/seu-usuario/sistema-contabilidade.git
cd sistema-contabilidade
```

2. Compile o projeto:
```bash
mvn clean install
```

3. Execute a aplicação:
```bash
mvn spring-boot:run
```

4. Acesse a aplicação no navegador:
```
http://localhost:8080
```

### Configuração do banco de dados

Para configurar um banco de dados MySQL em produção, edite o arquivo `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/sistema_contabilidade
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```


1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/nova-feature`)
3. Faça commit das suas alterações (`git commit -m 'Adiciona nova feature'`)
4. Faça push para a branch (`git push origin feature/nova-feature`)
5. Abra um Pull Request


