# Agenda de Estúdio de Tatuagem

Sistema de agendamento para estúdio de tatuagem: API REST em Java com Spring Boot e uma interface web que consome a própria API.

**Aplicação no ar:** https://agenda-estudio-28g9.onrender.com
**Documentação da API (Swagger):** https://agenda-estudio-28g9.onrender.com/swagger-ui.html

> Hospedado no plano gratuito do Render, que coloca o serviço para dormir após alguns minutos sem acesso. A primeira abertura pode levar até um minuto.

---

## O problema

Em um estúdio de tatuagem, cada atendimento ocupa um bloco de horário cuja duração depende do serviço contratado: uma tatuagem pequena leva uma hora, uma sessão de fechamento pode levar três. Agendar dois clientes em horários que se sobrepõem significa perder um deles.

O sistema resolve isso calculando o horário final automaticamente a partir da duração cadastrada no serviço e recusando qualquer agendamento que invada um intervalo já ocupado.

---

## Funcionalidades

- Cadastro de clientes e de serviços (com duração e preço)
- Criação de agendamentos com cálculo automático do horário de término
- Listagem da agenda por dia, por cliente ou por status
- Remarcação de horário, com nova verificação de conflito
- Controle de status: agendado, concluído, cancelado e não compareceu
- Exclusão de agendamentos
- Documentação interativa da API via Swagger

---

## Regras de negócio

**Nenhuma sobreposição de horários.** Antes de gravar, o sistema consulta o banco em busca de agendamentos que ocupem qualquer parte do intervalo pedido. Havendo conflito, a operação é recusada com a mensagem indicando o horário já ocupado.

**O horário final não é digitado.** Ele vem da soma entre o início informado e a duração cadastrada no serviço, o que evita erro de digitação e mantém a agenda coerente com o catálogo.

**Remarcação ignora o próprio agendamento.** Ao remarcar, a verificação de conflito exclui o registro que está sendo alterado — caso contrário, ele entraria em conflito consigo mesmo.

**Não se agenda no passado.** Datas anteriores ao momento atual são recusadas.

**Só agendamentos ativos podem ser remarcados.** Um atendimento já concluído ou cancelado não aceita mudança de horário.

As regras ficam no back-end, não no front-end. Isso significa que elas valem tanto para quem usa a tela quanto para quem chamar a API diretamente.

---

## Tratamento de erros

A exceção de conflito de horário é customizada e interceptada por um `@RestControllerAdvice`, que a converte em **409 Conflict** com um corpo descritivo:

    {
      "timestamp": "2026-11-20T19:30:00",
      "status": 409,
      "erro": "Conflito de agendamento",
      "mensagem": "Horário indisponível: já existe um agendamento das 14:00 às 15:00"
    }

A diferença importa: 500 significa que o servidor quebrou sem saber por quê; 409 significa que o pedido foi entendido, mas conflita com o estado atual dos dados. O front-end usa esse código para exibir a mensagem ao usuário em vez de um erro genérico.

---

## Tecnologias

**Back-end**
- Java 21
- Spring Boot 4.1
- Spring Data JPA / Hibernate
- Bean Validation
- H2 Database (em memória)
- springdoc-openapi (Swagger UI)

**Front-end**
- HTML, CSS e JavaScript puro
- Fetch API para consumo dos endpoints

**Infraestrutura**
- Docker (build multi-stage)
- Render (hospedagem)

---

## Endpoints

### Agendamentos

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/agendamentos?dia=2026-12-10` | Lista a agenda de um dia |
| GET | `/api/agendamentos/{id}` | Busca um agendamento |
| GET | `/api/agendamentos/cliente/{clienteId}` | Histórico de um cliente |
| GET | `/api/agendamentos/status/{status}` | Filtra por status |
| POST | `/api/agendamentos` | Cria um agendamento |
| PATCH | `/api/agendamentos/{id}/horario` | Remarca |
| PATCH | `/api/agendamentos/{id}/status` | Altera o status |
| DELETE | `/api/agendamentos/{id}` | Exclui |

### Clientes e Serviços

Ambos possuem operações de cadastro, consulta, atualização e exclusão em `/api/clientes` e `/api/servicos`.

A lista completa, com os formatos de requisição e resposta, está no Swagger.

---

## Como rodar localmente

Pré-requisitos: Java 21 e Maven.

    git clone https://github.com/Dayzxs/agenda-estudio.git
    cd agenda-estudio/agenda-estudio
    ./mvnw spring-boot:run

A aplicação sobe em `http://localhost:8080`.

- Interface web: `http://localhost:8080/index.html`
- Swagger: `http://localhost:8080/swagger-ui.html`

O banco H2 é em memória e recriado a cada inicialização. Um arquivo `data.sql` popula clientes e serviços de exemplo automaticamente, então a aplicação já sobe pronta para uso.

### Com Docker

    docker build -t agenda-estudio .
    docker run -p 8080:8080 agenda-estudio

---

## Estrutura do projeto

    src/main/java/com/estudio/agendaestudio/
    ├── controller/    Endpoints REST
    ├── dto/           Objetos de entrada e saída da API
    ├── exception/     Exceção customizada e tratador global
    ├── model/         Entidades JPA
    ├── repository/    Interfaces de acesso a dados
    └── service/       Regras de negócio

    src/main/resources/
    ├── static/        Front-end (HTML, CSS, JavaScript)
    ├── data.sql       Dados iniciais
    └── application.properties

A separação em camadas mantém as regras de negócio concentradas nos serviços: os controllers apenas recebem e devolvem dados, e os repositories apenas acessam o banco.

---

## Próximos passos

- **Autenticação no painel.** Hoje a tela de administração fica acessível a quem souber o endereço. O caminho natural é Spring Security com login.
- **Página pública de agendamento.** Uma segunda interface, voltada ao cliente final, consumindo a mesma API.
- **Banco persistente.** O H2 em memória é adequado para desenvolvimento, mas os dados somem a cada reinício. PostgreSQL resolveria isso.
- **Testes automatizados.** Testes de unidade para as regras de conflito e testes de integração para os endpoints.

---

## Autora

**Dayane Rodrigues** — Tecnóloga em Análise e Desenvolvimento de Sistemas
[GitHub](https://github.com/Dayzxs) · [LinkedIn](https://linkedin.com/in/dayanerodrigues-dev)
