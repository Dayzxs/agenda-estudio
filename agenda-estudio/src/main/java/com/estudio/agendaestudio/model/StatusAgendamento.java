package com.estudio.agendaestudio.model;

// Enum representa um conjunto fechado de valores possíveis.
// O compilador só aceita esses quatro status, então erro de digitação
// vira erro de compilação em vez de dado inconsistente no banco.
public enum StatusAgendamento {

    // Sessão marcada e ainda por acontecer.
    AGENDADO,

    // Cliente compareceu e a sessão foi realizada.
    CONCLUIDO,

    // Cancelado com aviso prévio, seja pelo cliente ou pelo estúdio.
    CANCELADO,

    // Cliente não apareceu e não avisou. Separado de CANCELADO porque
    // é um indicador que o estúdio precisa acompanhar por cliente.
    NAO_COMPARECEU
}