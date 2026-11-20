package com.estudio.agendaestudio.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

// @Entity diz ao JPA que esta classe representa uma tabela no banco.
@Entity
// @Table define o nome da tabela. Uso o plural por convenção de banco de dados.
@Table(name = "servicos")
public class Servico {

    // @Id marca a chave primária da tabela.
    // @GeneratedValue com IDENTITY faz o próprio banco numerar os registros.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @NotBlank rejeita null, string vazia e string só com espaços.
    // A validação roda antes de chegar no banco, quando a requisição entra.
    @NotBlank(message = "O nome do serviço é obrigatório")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nome;

    // Duração em minutos. Vai ser usada para calcular o horário final
    // do agendamento e detectar conflito de agenda.
    // @NotNull porque Integer aceita null; @Min garante duração positiva.
    @NotNull(message = "A duração é obrigatória")
    @Min(value = 15, message = "A duração mínima é de 15 minutos")
    @Column(name = "duracao_minutos", nullable = false)
    private Integer duracaoMinutos;

    // BigDecimal em vez de double: dinheiro exige precisão exata.
    // precision = 10 e scale = 2 significam até 8 dígitos inteiros e 2 decimais.
    @NotNull(message = "O preço é obrigatório")
    @DecimalMin(value = "0.0", inclusive = false, message = "O preço deve ser maior que zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    // Campo livre para detalhar o serviço. Opcional, por isso sem @NotBlank.
    @Column(columnDefinition = "TEXT")
    private String descricao;

    // O JPA exige um construtor vazio para conseguir instanciar a entidade.
    public Servico() {
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

    public Integer getDuracaoMinutos() {
        return duracaoMinutos;
    }

    public void setDuracaoMinutos(Integer duracaoMinutos) {
        this.duracaoMinutos = duracaoMinutos;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}