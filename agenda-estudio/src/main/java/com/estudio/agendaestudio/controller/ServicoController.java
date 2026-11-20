package com.estudio.agendaestudio.controller;

import com.estudio.agendaestudio.model.Servico;
import com.estudio.agendaestudio.service.ServicoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// @RestController: toda resposta destes métodos vira JSON automaticamente.
// @RequestMapping define o prefixo comum de todos os endpoints da classe.
@RestController
@RequestMapping("/api/servicos")
public class ServicoController {

    private final ServicoService servicoService;

    public ServicoController(ServicoService servicoService) {
        this.servicoService = servicoService;
    }

    // GET /api/servicos             -> catálogo completo, em ordem alfabética
    // GET /api/servicos?nome=black  -> filtra por nome
    @GetMapping
    public List<Servico> listar(@RequestParam(required = false) String nome) {
        if (nome != null && !nome.isBlank()) {
            return servicoService.buscarPorNome(nome);
        }
        return servicoService.listarTodos();
    }

    // GET /api/servicos/3
    @GetMapping("/{id}")
    public Servico buscarPorId(@PathVariable Long id) {
        return servicoService.buscarPorId(id);
    }

    // POST /api/servicos
    // 201 Created é o código correto para criação de recurso.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Servico criar(@Valid @RequestBody Servico servico) {
        return servicoService.criar(servico);
    }

    // PUT /api/servicos/3
    @PutMapping("/{id}")
    public Servico atualizar(@PathVariable Long id, @Valid @RequestBody Servico servico) {
        return servicoService.atualizar(id, servico);
    }

    // DELETE /api/servicos/3
    // 204 No Content: deu certo e não há corpo para devolver.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        servicoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}