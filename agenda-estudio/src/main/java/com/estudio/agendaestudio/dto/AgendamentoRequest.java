package com.estudio.agendaestudio.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

// Record: classe imutável que só transporta dados.
// O Java gera construtor, acessores, equals e toString sozinho.
//
// Este é o formato do JSON que entra ao criar um agendamento.
// Repare que recebo só os ids de cliente e serviço, não os objetos:
// quem envia a requisição não precisa conhecer a estrutura das tabelas.
public record AgendamentoRequest(

        @NotNull(message = "O cliente é obrigatório")
        Long clienteId,

        @NotNull(message = "O serviço é obrigatório")
        Long servicoId,

        // @Future rejeita data passada já na entrada da requisição.
        // A mesma regra existe no service, e isso é proposital: o DTO
        // devolve erro de validação padronizado, o service garante a regra
        // mesmo se o agendamento vier por outro caminho no futuro.
        @NotNull(message = "A data e hora de início são obrigatórias")
        @Future(message = "A data e hora devem estar no futuro")
        LocalDateTime dataHoraInicio,

        @Size(max = 500, message = "As observações devem ter no máximo 500 caracteres")
        String observacoes

) {
}