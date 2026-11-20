package com.estudio.agendaestudio.dto;

import com.estudio.agendaestudio.model.Agendamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Formato do JSON que sai da API.
//
// Achato os relacionamentos: em vez de devolver o objeto Cliente inteiro
// aninhado, devolvo id e nome. Isso evita expor dados desnecessários,
// evita JSON gigante e impede o problema de referência circular.
public record AgendamentoResponse(

        Long id,
        Long clienteId,
        String clienteNome,
        String clienteTelefone,
        Long servicoId,
        String servicoNome,
        Integer duracaoMinutos,
        BigDecimal preco,
        LocalDateTime dataHoraInicio,
        LocalDateTime dataHoraFim,
        String status,
        String observacoes

) {

    // Método de fábrica: concentra num lugar só a conversão de entidade
    // para DTO. Sem ele, essa montagem apareceria repetida em todo
    // endpoint que devolve agendamento.
    public static AgendamentoResponse de(Agendamento agendamento) {
        return new AgendamentoResponse(
                agendamento.getId(),
                agendamento.getCliente().getId(),
                agendamento.getCliente().getNome(),
                agendamento.getCliente().getTelefone(),
                agendamento.getServico().getId(),
                agendamento.getServico().getNome(),
                agendamento.getServico().getDuracaoMinutos(),
                agendamento.getServico().getPreco(),
                agendamento.getDataHoraInicio(),
                agendamento.getDataHoraFim(),
                agendamento.getStatus().name(),
                agendamento.getObservacoes()
        );
    }
}