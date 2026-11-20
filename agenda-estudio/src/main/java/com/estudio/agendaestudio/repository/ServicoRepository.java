package com.estudio.agendaestudio.repository;

import com.estudio.agendaestudio.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// JpaRepository já entrega salvar, buscar por id, listar, deletar e paginar.
// Os dois tipos entre os sinais são a entidade e o tipo da chave primária.
@Repository
public interface ServicoRepository extends JpaRepository<Servico, Long> {

    // Busca por nome parcial, sem diferenciar maiúscula de minúscula.
    // Vira um LIKE na consulta gerada pelo Spring.
    List<Servico> findByNomeContainingIgnoreCase(String nome);

    // "OrderBy" no fim do nome define a ordenação da consulta.
    // Uso para listar o catálogo de serviços já em ordem alfabética,
    // sem precisar ordenar depois em memória.
    List<Servico> findAllByOrderByNomeAsc();

    // Impede cadastro de dois serviços com o mesmo nome.
    boolean existsByNomeIgnoreCase(String nome);
}