package com.estudio.agendaestudio.controller;

import com.estudio.agendaestudio.dto.AgendamentoRequest;
import com.estudio.agendaestudio.dto.AgendamentoResponse;
import com.estudio.agendaestudio.model.StatusAgendamento;
import com.estudio.agendaestudio.service.AgendamentoService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// @RestController: toda resposta destes métodos vira JSON automaticamente.
// @RequestMapping define o prefixo comum de todos os endpoints da classe.
@RestController
@RequestMapping("/api/agendamentos")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    public AgendamentoController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    // GET /api/agendamentos?dia=2026-10-15
    // @DateTimeFormat com ISO.DATE ensina o Spring a converter o texto
    // da URL em LocalDate. Sem ela, a requisição falharia na conversão.
    // O dia é opcional: sem ele, uso a data de hoje.
    @GetMapping
    public List<AgendamentoResponse> listarPorDia(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dia) {

        LocalDate diaConsultado = (dia != null) ? dia : LocalDate.now();

        return agendamentoService.listarPorDia(diaConsultado)
                .stream()
                .map(AgendamentoResponse::de)
                .toList();
    }

    // GET /api/agendamentos/7
    @GetMapping("/{id}")
    public AgendamentoResponse buscarPorId(@PathVariable Long id) {
        return AgendamentoResponse.de(agendamentoService.buscarPorId(id));
    }

    // GET /api/agendamentos/cliente/5
    // Histórico completo de um cliente, do mais recente ao mais antigo.
    @GetMapping("/cliente/{clienteId}")
    public List<AgendamentoResponse> listarPorCliente(@PathVariable Long clienteId) {
        return agendamentoService.listarPorCliente(clienteId)
                .stream()
                .map(AgendamentoResponse::de)
                .toList();
    }

    // GET /api/agendamentos/status/AGENDADO
    @GetMapping("/status/{status}")
    public List<AgendamentoResponse> listarPorStatus(@PathVariable StatusAgendamento status) {
        return agendamentoService.listarPorStatus(status)
                .stream()
                .map(AgendamentoResponse::de)
                .toList();
    }

    // POST /api/agendamentos
    // É aqui que a regra de conflito de horário entra em ação:
    // se o intervalo invadir outro agendamento, o service lança erro
    // e a API devolve 400 com a mensagem explicando o choque.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AgendamentoResponse criar(@Valid @RequestBody AgendamentoRequest request) {
        return AgendamentoResponse.de(
                agendamentoService.criar(
                        request.clienteId(),
                        request.servicoId(),
                        request.dataHoraInicio(),
                        request.observacoes()
                )
        );
    }

    // PATCH /api/agendamentos/7/horario?novoInicio=2026-10-15T14:00:00
    // PATCH em vez de PUT porque altero só um aspecto do recurso,
    // não substituo o agendamento inteiro.
    @PatchMapping("/{id}/horario")
    public AgendamentoResponse remarcar(
            @PathVariable Long id,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime novoInicio) {

        return AgendamentoResponse.de(agendamentoService.remarcar(id, novoInicio));
    }

    // PATCH /api/agendamentos/7/status?novoStatus=CONCLUIDO
    @PatchMapping("/{id}/status")
    public AgendamentoResponse alterarStatus(@PathVariable Long id,
                                             @RequestParam StatusAgendamento novoStatus) {
        return AgendamentoResponse.de(agendamentoService.alterarStatus(id, novoStatus));
    }

    // DELETE /api/agendamentos/7
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        agendamentoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}