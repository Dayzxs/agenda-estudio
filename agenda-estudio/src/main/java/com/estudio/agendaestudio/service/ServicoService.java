package com.estudio.agendaestudio.service;

import com.estudio.agendaestudio.model.Servico;
import com.estudio.agendaestudio.repository.AgendamentoRepository;
import com.estudio.agendaestudio.repository.ServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// @Service marca esta classe como componente de regra de negócio.
// O Spring cria uma instância dela sozinho e injeta onde for preciso.
@Service
public class ServicoService {

    private final ServicoRepository servicoRepository;
    private final AgendamentoRepository agendamentoRepository;

    // Injeção por construtor: deixa as dependências explícitas
    // e permite testar a classe sem subir a aplicação inteira.
    public ServicoService(ServicoRepository servicoRepository,
                          AgendamentoRepository agendamentoRepository) {
        this.servicoRepository = servicoRepository;
        this.agendamentoRepository = agendamentoRepository;
    }

    // Já devolve ordenado por nome, direto do banco.
    @Transactional(readOnly = true)
    public List<Servico> listarTodos() {
        return servicoRepository.findAllByOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public Servico buscarPorId(Long id) {
        return servicoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Serviço não encontrado com o id " + id));
    }

    @Transactional(readOnly = true)
    public List<Servico> buscarPorNome(String nome) {
        return servicoRepository.findByNomeContainingIgnoreCase(nome);
    }

    // Dois serviços com o mesmo nome confundiriam na hora de agendar.
    @Transactional
    public Servico criar(Servico servico) {
        if (servicoRepository.existsByNomeIgnoreCase(servico.getNome())) {
            throw new IllegalArgumentException(
                    "Já existe um serviço cadastrado com este nome");
        }
        return servicoRepository.save(servico);
    }

    @Transactional
    public Servico atualizar(Long id, Servico dados) {
        Servico servico = buscarPorId(id);

        // Só valido a duplicidade se o nome realmente mudou, para não
        // dar conflito do serviço com ele mesmo ao salvar sem alterar.
        if (!servico.getNome().equalsIgnoreCase(dados.getNome())
                && servicoRepository.existsByNomeIgnoreCase(dados.getNome())) {
            throw new IllegalArgumentException(
                    "Já existe um serviço cadastrado com este nome");
        }

        servico.setNome(dados.getNome());
        servico.setDuracaoMinutos(dados.getDuracaoMinutos());
        servico.setPreco(dados.getPreco());
        servico.setDescricao(dados.getDescricao());

        return servicoRepository.save(servico);
    }

    // Mesma proteção do cliente: serviço com histórico não pode sumir,
    // senão os agendamentos antigos ficariam órfãos.
    @Transactional
    public void excluir(Long id) {
        Servico servico = buscarPorId(id);

        if (agendamentoRepository.existsByServicoId(id)) {
            throw new IllegalArgumentException(
                    "Não é possível excluir um serviço que possui agendamentos");
        }

        servicoRepository.delete(servico);
    }
}