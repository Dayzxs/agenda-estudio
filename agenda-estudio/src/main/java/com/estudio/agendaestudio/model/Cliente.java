package com.estudio.agendaestudio.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// @Entity diz ao JPA que esta classe representa uma tabela no banco.
// Sem ela, o Spring ignora a classe e nenhuma tabela é criada.
@Entity
// @Table define o nome da tabela. Sem isso ela se chamaria "cliente"
// (o nome da classe). Uso o plural por convenção de banco de dados.
@Table(name = "clientes")
public class Cliente {

    // @Id marca a chave primária da tabela.
    // @GeneratedValue com IDENTITY faz o próprio banco numerar os registros,
    // igual ao AUTO_INCREMENT do SQL. Não preciso gerar o id na mão.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @NotBlank rejeita null, string vazia e string só com espaços.
    // A validação roda antes de chegar no banco, quando a requisição entra.
    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
    // nullable = false replica a regra no banco: é a segunda barreira,
    // caso algum dado entre por fora da API.
    @Column(nullable = false, length = 100)
    private String nome;

    @NotBlank(message = "O telefone é obrigatório")
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String telefone;

    // @Email valida o formato do endereço. É opcional aqui:
    // muito cliente de estúdio agenda só pelo telefone.
    @Email(message = "E-mail inválido")
    @Size(max = 100)
    @Column(length = 100)
    private String email;

    // Campo livre para anotações do tatuador: alergias, preferências,
    // histórico. columnDefinition = "TEXT" permite texto longo.
    @Column(columnDefinition = "TEXT")
    private String observacoes;

    // O JPA exige um construtor vazio para conseguir instanciar a classe
    // quando lê os dados do banco. Sem ele, a aplicação nem sobe.
    public Cliente() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}