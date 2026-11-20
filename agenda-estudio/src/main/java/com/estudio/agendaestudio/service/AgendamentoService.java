package com.estudio.agendaestudio.service;

import com.estudio.agendaestudio.exception.HorarioIndisponivelException;
import com.estudio.agendaestudio.model.Agendamento;
import com.estudio.agendaestudio.model.Cliente;
import com.estudio.agendaestudio.model.Servico;
import com.estudio.agendaestudio.model.StatusAgendamento;
import com.estudio.agendaestudio.repository.AgendamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

// @Service marca esta classe como componente de regra de negócio.
// É aqui que mora a regra central do sistema: nenhum horário duplicado.
@Service
public class AgendamentoService {

    // Valor usado quando o agendamento ainda não tem id (cadastro novo).
    // Nenhum registro real tem id -1, então a consulta de conflito
    // não exclui ninguém indevidamente.
    private static final Long NENHUM_ID = -1L;

    private final AgendamentoRepository agendamentoRepository;
    private final ClienteService clienteService;
    private final ServicoService servicoService;

    // Repare que o serviço depende de outros serviços, não dos repositórios
    // deles. Assim as regras de cliente e de serviço continuam valendo
    // mesmo quando quem chama é o agendamento.
    public AgendamentoService(AgendamentoRepository agendamentoRepository,
                              ClienteService clienteService,
                              ServicoService servicoService) {
        this.agendamentoRepository = agendamentoRepository;
        this.clienteService = clienteService;
        this.servicoService = servicoService;
    }

    @Transactional(readOnly = true)
    public Agendamento buscarPorId(Long id) {
        return agendamentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Agendamento não encontrado com o id " + id));
    }

    // Agenda de um dia inteiro: monto a faixa das 00:00 até as 23:59:59
    // e deixo o banco filtrar o intervalo.
    @Transactional(readOnly = true)
    public List<Agendamento> listarPorDia(LocalDate dia) {
        LocalDateTime inicioDoDia = dia.atStartOfDay();
        LocalDateTime fimDoDia = dia.atTime(LocalTime.MAX);
        return agendamentoRepository
                .findByDataHoraInicioBetweenOrderByDataHoraInicioAsc(inicioDoDia, fimDoDia);
    }

    @Transactional(readOnly = true)
    public List<Agendamento> listarPorCliente(Long clienteId) {
        return agendamentoRepository.findByClienteIdOrderByDataHoraInicioDesc(clienteId);
    }

    @Transactional(readOnly = true)
    public List<Agendamento> listarPorStatus(StatusAgendamento status) {
        return agendamentoRepository.findByStatusOrderByDataHoraInicioAsc(status);
    }

    @Transactional
    public Agendamento criar(Long clienteId, Long servicoId,
                             LocalDateTime dataHoraInicio, String observacoes) {

        // Busco pelos serviços, não pelos repositórios: se o cliente ou o
        // serviço não existirem, o erro sai daqui com mensagem clara.
        Cliente cliente = clienteService.buscarPorId(clienteId);
        Servico servico = servicoService.buscarPorId(servicoId);

        validarHorario(dataHoraInicio);

        // O horário final vem da duração cadastrada no serviço.
        LocalDateTime dataHoraFim = dataHoraInicio.plusMinutes(servico.getDuracaoMinutos());

        // Regra central: o intervalo pedido não pode invadir nenhum outro.
        validarConflito(dataHoraInicio, dataHoraFim, NENHUM_ID);

        Agendamento agendamento = new Agendamento();
        agendamento.setCliente(cliente);
        agendamento.setServico(servico);
        agendamento.setDataHoraInicio(dataHoraInicio);
        agendamento.setDataHoraFim(dataHoraFim);
        agendamento.setStatus(StatusAgendamento.AGENDADO);
        agendamento.setObservacoes(observacoes);

        return agendamentoRepository.save(agendamento);
    }

    // Remarcar é o caso que revela o detalhe do idIgnorado: sem ele,
    // o agendamento entraria em conflito consigo mesmo.
    @Transactional
    public Agendamento remarcar(Long id, LocalDateTime novoInicio) {
        Agendamento agendamento = buscarPorId(id);

        if (agendamento.getStatus() != StatusAgendamento.AGENDADO) {
            throw new IllegalArgumentException(
                    "Só é possível remarcar agendamentos com status AGENDADO");
        }

        validarHorario(novoInicio);

        LocalDateTime novoFim = novoInicio
                .plusMinutes(agendamento.getServico().getDuracaoMinutos());

        validarConflito(novoInicio, novoFim, id);

        agendamento.setDataHoraInicio(novoInicio);
        agendamento.setDataHoraFim(novoFim);

        return agendamentoRepository.save(agendamento);
    }

    // Mudar o status não mexe em horário, então não precisa revalidar conflito.
    @Transactional
    public Agendamento alterarStatus(Long id, StatusAgendamento novoStatus) {
        Agendamento agendamento = buscarPorId(id);
        agendamento.setStatus(novoStatus);
        return agendamentoRepository.save(agendamento);
    }

    @Transactional
    public void excluir(Long id) {
        Agendamento agendamento = buscarPorId(id);
        agendamentoRepository.delete(agendamento);
    }

    // Método privado: é detalhe interno da regra, ninguém de fora chama.
    private void validarHorario(LocalDateTime dataHoraInicio) {
        if (dataHoraInicio.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "Não é possível agendar para uma data e hora no passado");
        }
    }

    // O coração do sistema. Delego a checagem de sobreposição ao banco
    // e aqui só decido o que fazer com o resultado.
    private void validarConflito(LocalDateTime inicio, LocalDateTime fim, Long idIgnorado) {
        List<Agendamento> conflitos =
                agendamentoRepository.buscarConflitos(inicio, fim, idIgnorado);

        if (!conflitos.isEmpty()) {
            Agendamento conflito = conflitos.get(0);
            // Exceção própria: o GlobalExceptionHandler a converte em 409 Conflict.
            throw new HorarioIndisponivelException(
                    "Horário indisponível: já existe um agendamento das "
                            + conflito.getDataHoraInicio().toLocalTime()
                            + " às "
                            + conflito.getDataHoraFim().toLocalTime());
        }
    }
}