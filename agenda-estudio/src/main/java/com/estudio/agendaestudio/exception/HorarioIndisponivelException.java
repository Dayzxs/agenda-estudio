package com.estudio.agendaestudio.exception;

public class HorarioIndisponivelException extends RuntimeException {
    public HorarioIndisponivelException(String mensagem) {
        super(mensagem);
    }
}