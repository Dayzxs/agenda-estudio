package com.estudio.agendaestudio.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

// @Entity diz ao JPA que esta classe representa uma tabela no banco.
@Entity
// @Table define o nome da tabela. Uso o plural por convenção de banco de dados.
@Table(name = "agendamentos")
public class Agendamento {

    // @Id marca a chave primária da tabela.
    // @GeneratedValue com IDENTITY faz o próprio banco numerar os registros.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @ManyToOne: muitos agendamentos para um cliente.
    // FetchType.LAZY significa que o cliente só é buscado no banco quando
    // eu realmente acessar o campo, e não em toda consulta de agendamento.
    // @JoinColumn define o nome da coluna de chave estrangeira na tabela.
    @NotNull(message = "O cliente é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    // Mesmo relacionamento, agora com o serviço.
    // É dele que vem a duração usada para calcular o horário de término.
    @NotNull(message = "O serviço é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servico_id", nullable = false)
    private Servico servico;

    // LocalDateTime guarda data e hora juntas, sem fuso horário.
    // É o tipo adequado para agenda de um estúdio que atende presencialmente.
    @NotNull(message = "A data e hora de início são obrigatórias")
    @Column(name = "data_hora_inicio", nullable = false)
    private LocalDateTime dataHoraInicio;

    // Horário final calculado a partir do início mais a duração do serviço.
    // Gravo no banco em vez de calcular toda vez para tornar a verificação
    // de conflito de horário uma consulta simples de intervalo.
    @Column(name = "data_hora_fim", nullable = false)
    private LocalDateTime dataHoraFim;

    // @Enumerated com STRING grava o nome do status no banco ("AGENDADO").
    // Com ORDINAL gravaria o número da posição, o que quebraria os dados
    // caso alguém reordenasse o enum depois.
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusAgendamento status = StatusAgendamento.AGENDADO;

    // Campo livre para anotações do tatuador sobre a sessão.
    @Column(columnDefinition = "TEXT")
    private String observacoes;

    // O JPA exige um construtor vazio para conseguir instanciar a entidade.
    public Agendamento() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Servico getServico() {
        return servico;
    }

    public void setServico(Servico servico) {
        this.servico = servico;
    }

    public LocalDateTime getDataHoraInicio() {
        return dataHoraInicio;
    }

    public void setDataHoraInicio(LocalDateTime dataHoraInicio) {
        this.dataHoraInicio = dataHoraInicio;
    }

    public LocalDateTime getDataHoraFim() {
        return dataHoraFim;
    }

    public void setDataHoraFim(LocalDateTime dataHoraFim) {
        this.dataHoraFim = dataHoraFim;
    }

    public StatusAgendamento getStatus() {
        return status;
    }

    public void setStatus(StatusAgendamento status) {
        this.status = status;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}