package com.estudio.agendaestudio.repository;

import com.estudio.agendaestudio.model.Agendamento;
import com.estudio.agendaestudio.model.StatusAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

// JpaRepository já entrega salvar, buscar por id, listar, deletar e paginar.
// Os dois tipos entre os sinais são a entidade e o tipo da chave primária.
@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    // Consulta central do projeto: detecta se o horário pedido invade
    // algum agendamento que já existe.
    //
    // A regra de sobreposição de intervalos é: dois períodos se cruzam
    // quando o início de um é menor que o fim do outro E o fim de um é
    // maior que o início do outro. Não basta comparar só o horário inicial.
    //
    // Cancelados e faltas não bloqueiam a agenda, por isso o filtro de status.
    //
    // O parâmetro idIgnorado existe para a edição: ao remarcar um agendamento,
    // ele não pode ser considerado conflito consigo mesmo. Quando for um
    // agendamento novo passo -1, que nunca corresponde a um id real.
    @Query("""
            SELECT a FROM Agendamento a
            WHERE a.status NOT IN (com.estudio.agendaestudio.model.StatusAgendamento.CANCELADO,
                                   com.estudio.agendaestudio.model.StatusAgendamento.NAO_COMPARECEU)
              AND a.id <> :idIgnorado
              AND a.dataHoraInicio < :fim
              AND a.dataHoraFim > :inicio
            """)
    List<Agendamento> buscarConflitos(@Param("inicio") LocalDateTime inicio,
                                      @Param("fim") LocalDateTime fim,
                                      @Param("idIgnorado") Long idIgnorado);

    // Agenda do dia: tudo que começa dentro da faixa informada, em ordem
    // cronológica. É o que alimenta a tela principal do estúdio.
    List<Agendamento> findByDataHoraInicioBetweenOrderByDataHoraInicioAsc(LocalDateTime inicio,
                                                                          LocalDateTime fim);

    // Histórico de um cliente específico, do mais recente para o mais antigo.
    List<Agendamento> findByClienteIdOrderByDataHoraInicioDesc(Long clienteId);

    // Filtro por situação, para telas como "próximos agendamentos".
    List<Agendamento> findByStatusOrderByDataHoraInicioAsc(StatusAgendamento status);

    // Impede excluir um serviço que já tem histórico de agendamento.
    boolean existsByServicoId(Long servicoId);

    // Mesma proteção para cliente.
    boolean existsByClienteId(Long clienteId);
}