package com.estudio.agendaestudio.controller;

import com.estudio.agendaestudio.model.Cliente;
import com.estudio.agendaestudio.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// @RestController: toda resposta destes métodos vira JSON automaticamente.
// @RequestMapping define o prefixo comum de todos os endpoints da classe.
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    // Mesma injeção por construtor usada nos serviços.
    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    // GET /api/clientes            -> lista todos
    // GET /api/clientes?nome=ana   -> filtra por nome
    // @RequestParam com required = false deixa o filtro opcional.
    @GetMapping
    public List<Cliente> listar(@RequestParam(required = false) String nome) {
        if (nome != null && !nome.isBlank()) {
            return clienteService.buscarPorNome(nome);
        }
        return clienteService.listarTodos();
    }

    // GET /api/clientes/5
    // @PathVariable pega o valor direto da URL.
    @GetMapping("/{id}")
    public Cliente buscarPorId(@PathVariable Long id) {
        return clienteService.buscarPorId(id);
    }

    // POST /api/clientes
    // @Valid dispara as validações que estão nas anotações da entidade.
    // Sem ele, as regras de @NotBlank e @Size seriam ignoradas.
    // @ResponseStatus devolve 201 Created, que é o código correto
    // para criação de recurso — e não o 200 padrão.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Cliente criar(@Valid @RequestBody Cliente cliente) {
        return clienteService.criar(cliente);
    }

    // PUT /api/clientes/5
    @PutMapping("/{id}")
    public Cliente atualizar(@PathVariable Long id, @Valid @RequestBody Cliente cliente) {
        return clienteService.atualizar(id, cliente);
    }

    // DELETE /api/clientes/5
    // 204 No Content: deu certo e não há corpo para devolver.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        clienteService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}