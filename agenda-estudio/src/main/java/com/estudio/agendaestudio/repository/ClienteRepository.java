package com.estudio.agendaestudio.repository;

import com.estudio.agendaestudio.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// JpaRepository já entrega salvar, buscar por id, listar, deletar e paginar.
// Os dois tipos entre os sinais são a entidade e o tipo da chave primária.
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // O Spring monta a consulta a partir do nome do método.
    // "findBy" + "Telefone" vira: WHERE telefone = ?
    // Optional deixa explícito que pode não existir cliente com esse telefone.
    Optional<Cliente> findByTelefone(String telefone);

    // "ContainingIgnoreCase" vira um LIKE sem diferenciar maiúscula de minúscula.
    // É o método que sustenta a busca de cliente por nome parcial na tela.
    List<Cliente> findByNomeContainingIgnoreCase(String nome);

    // "existsBy" devolve só true ou false, sem trazer o registro inteiro.
    // Uso para impedir cadastro duplicado de telefone sem custo desnecessário.
    boolean existsByTelefone(String telefone);
}