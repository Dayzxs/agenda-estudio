package com.estudio.agendaestudio.service;

import com.estudio.agendaestudio.model.Cliente;
import com.estudio.agendaestudio.repository.AgendamentoRepository;
import com.estudio.agendaestudio.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// @Service marca esta classe como componente de regra de negócio.
// O Spring cria uma instância dela sozinho e injeta onde for preciso.
@Service
public class ClienteService {

    // Os repositórios chegam prontos pelo construtor.
    // Campos final garantem que ninguém troca a dependência depois.
    private final ClienteRepository clienteRepository;
    private final AgendamentoRepository agendamentoRepository;

    // Injeção por construtor: o Spring passa os repositórios ao criar a classe.
    // É o formato recomendado porque deixa as dependências explícitas
    // e permite testar a classe sem subir a aplicação inteira.
    public ClienteService(ClienteRepository clienteRepository,
                          AgendamentoRepository agendamentoRepository) {
        this.clienteRepository = clienteRepository;
        this.agendamentoRepository = agendamentoRepository;
    }

    // readOnly = true avisa ao banco que a transação não vai escrever,
    // o que permite otimizações na consulta.
    @Transactional(readOnly = true)
    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cliente não encontrado com o id " + id));
    }

    @Transactional(readOnly = true)
    public List<Cliente> buscarPorNome(String nome) {
        return clienteRepository.findByNomeContainingIgnoreCase(nome);
    }

    // Regra de negócio: telefone é o identificador prático do cliente
    // no estúdio, então não pode repetir.
    @Transactional
    public Cliente criar(Cliente cliente) {
        if (clienteRepository.existsByTelefone(cliente.getTelefone())) {
            throw new IllegalArgumentException(
                    "Já existe um cliente cadastrado com este telefone");
        }
        return clienteRepository.save(cliente);
    }

    @Transactional
    public Cliente atualizar(Long id, Cliente dados) {
        Cliente cliente = buscarPorId(id);

        // Só valido a duplicidade se o telefone realmente mudou.
        // Sem essa checagem, salvar o cadastro sem alterar o telefone
        // daria conflito do cliente com ele mesmo.
        if (!cliente.getTelefone().equals(dados.getTelefone())
                && clienteRepository.existsByTelefone(dados.getTelefone())) {
            throw new IllegalArgumentException(
                    "Já existe um cliente cadastrado com este telefone");
        }

        cliente.setNome(dados.getNome());
        cliente.setTelefone(dados.getTelefone());
        cliente.setEmail(dados.getEmail());
        cliente.setObservacoes(dados.getObservacoes());

        return clienteRepository.save(cliente);
    }

    // Excluir cliente com histórico apagaria agendamentos junto.
    // Prefiro bloquear a exclusão a perder o histórico do estúdio.
    @Transactional
    public void excluir(Long id) {
        Cliente cliente = buscarPorId(id);

        if (agendamentoRepository.existsByClienteId(id)) {
            throw new IllegalArgumentException(
                    "Não é possível excluir um cliente que possui agendamentos");
        }

        clienteRepository.delete(cliente);
    }
}