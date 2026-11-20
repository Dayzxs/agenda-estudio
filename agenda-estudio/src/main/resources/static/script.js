// Endereço base da API. Como o front é servido pelo próprio Spring,
// basta o caminho relativo.
const API = '/api/agendamentos';

const lista = document.getElementById('listaAgendamentos');
const aviso = document.getElementById('aviso');
const filtroDia = document.getElementById('filtroDia');

// Busca os agendamentos de um dia e manda desenhar na tela.
async function carregarAgendamentos(dia) {
    lista.innerHTML = '';
    aviso.textContent = 'Carregando...';

    try {
        const resposta = await fetch(`${API}?dia=${dia}`);

        if (!resposta.ok) {
            aviso.textContent = 'Não foi possível carregar a agenda.';
            return;
        }

        const agendamentos = await resposta.json();

        if (agendamentos.length === 0) {
            aviso.textContent = 'Nenhum agendamento para esta data.';
            return;
        }

        aviso.textContent = '';
        agendamentos.forEach(desenharCard);

    } catch (erro) {
        aviso.textContent = 'Erro de conexão com o servidor.';
    }
}

// Monta um card a partir de um agendamento vindo da API.
function desenharCard(agendamento) {
    const item = document.createElement('li');
    item.className = 'card';

    const inicio = new Date(agendamento.dataHoraInicio);

    item.innerHTML = `
        <div class="card-hora">
            <strong>${formatarHora(inicio)}</strong>
            <span>${formatarDiaMes(inicio)}</span>
        </div>
        <div class="card-info">
            <h3>${agendamento.clienteNome}</h3>
            <p>${agendamento.servicoNome} · ${agendamento.duracaoMinutos} min · ${formatarPreco(agendamento.preco)}</p>
        </div>
        <span class="etiqueta ${classeDoStatus(agendamento.status)}">${rotuloDoStatus(agendamento.status)}</span>
        ${montarAcoes(agendamento)}
    `;

    ligarAcoes(item, agendamento);
    lista.appendChild(item);
}

// Os botões dependem do status: um agendamento já encerrado
// não oferece concluir nem remarcar.
function montarAcoes(agendamento) {
    const botoes = [];

    if (agendamento.status === 'AGENDADO') {
        botoes.push('<button class="botao-acao" data-acao="concluir">Concluir</button>');
        botoes.push('<button class="botao-acao" data-acao="remarcar">Remarcar</button>');
        botoes.push('<button class="botao-acao" data-acao="faltou">Não compareceu</button>');
        botoes.push('<button class="botao-acao" data-acao="cancelar">Cancelar</button>');
    }

    botoes.push('<button class="botao-acao botao-acao-perigo" data-acao="excluir">Excluir</button>');

    return `
        <div class="card-acoes">${botoes.join('')}</div>
        <div class="remarcar-caixa">
            <input type="datetime-local" data-campo="novoHorario">
            <button class="botao-acao" data-acao="confirmarRemarcacao">Confirmar</button>
        </div>
    `;
}

function formatarHora(data) {
    return data.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
}

function formatarDiaMes(data) {
    return data.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' });
}

function formatarPreco(valor) {
    return Number(valor).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
}

// O status vem do enum do Java em maiúsculas (AGENDADO, CONCLUIDO...).
function classeDoStatus(status) {
    if (status === 'CONCLUIDO') return 'etiqueta-concluido';
    if (status === 'CANCELADO') return 'etiqueta-cancelado';
    if (status === 'NAO_COMPARECEU') return 'etiqueta-cancelado';
    return 'etiqueta-agendado';
}

// Converte o enum do Java em texto legível na tela.
function rotuloDoStatus(status) {
    if (status === 'NAO_COMPARECEU') return 'Não compareceu';
    if (status === 'CONCLUIDO') return 'Concluído';
    if (status === 'CANCELADO') return 'Cancelado';
    return 'Agendado';
}

// Ao trocar a data no filtro, recarrega a lista.
filtroDia.addEventListener('change', () => carregarAgendamentos(filtroDia.value));

// Ao abrir a página, mostra o dia do agendamento de teste.
filtroDia.value = '2026-12-10';
carregarAgendamentos(filtroDia.value);

// ---- Formulário de novo agendamento ----

const form = document.getElementById('formAgendamento');
const selectCliente = document.getElementById('cliente');
const selectServico = document.getElementById('servico');
const mensagemErro = document.getElementById('mensagemErro');

// Preenche um <select> com os registros vindos da API.
async function preencherSelect(elemento, url) {
    try {
        const resposta = await fetch(url);
        if (!resposta.ok) return;

        const itens = await resposta.json();

        itens.forEach(item => {
            const opcao = document.createElement('option');
            opcao.value = item.id;
            opcao.textContent = item.nome;
            elemento.appendChild(opcao);
        });
    } catch (erro) {
        console.error('Falha ao carregar ' + url, erro);
    }
}

// Mostra uma mensagem abaixo do formulário. O tipo define a cor.
function mostrarMensagem(texto, tipo) {
    mensagemErro.textContent = texto;
    mensagemErro.className = 'mensagem-erro visivel';

    if (tipo === 'sucesso') {
        mensagemErro.classList.add('mensagem-sucesso');
    }

    // Some sozinha depois de alguns segundos.
    setTimeout(esconderMensagem, 5000);
}

function esconderMensagem() {
    mensagemErro.className = 'mensagem-erro';
}

// O submit do formulário monta o JSON e envia por POST.
form.addEventListener('submit', async (evento) => {
    // Impede o recarregamento padrão da página.
    evento.preventDefault();
    esconderMensagem();

    const novoAgendamento = {
        clienteId: Number(selectCliente.value),
        servicoId: Number(selectServico.value),
        dataHoraInicio: document.getElementById('data').value,
        observacoes: document.getElementById('obs').value
    };

    try {
        const resposta = await fetch(API, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(novoAgendamento)
        });

        // 409 é o conflito de horário tratado pelo GlobalExceptionHandler.
        if (resposta.status === 409) {
            const erro = await resposta.json();
            mostrarMensagem(erro.mensagem, 'erro');
            return;
        }

        if (!resposta.ok) {
            mostrarMensagem('Não foi possível agendar. Verifique os dados.', 'erro');
            return;
        }

        form.reset();
        mostrarMensagem('Agendamento criado.', 'sucesso');
        filtroDia.value = novoAgendamento.dataHoraInicio.substring(0, 10);
        carregarAgendamentos(filtroDia.value);

    } catch (erro) {
        mostrarMensagem('Erro de conexão com o servidor.', 'erro');
    }
});

preencherSelect(selectCliente, '/api/clientes');
preencherSelect(selectServico, '/api/servicos');

// ---- Ações dos cards ----

// Liga cada botão do card à sua ação.
function ligarAcoes(item, agendamento) {
    const caixaRemarcar = item.querySelector('.remarcar-caixa');

    item.querySelectorAll('[data-acao]').forEach(botao => {
        botao.addEventListener('click', () => {
            const acao = botao.dataset.acao;

            if (acao === 'concluir') alterarStatus(agendamento.id, 'CONCLUIDO');
            if (acao === 'cancelar') alterarStatus(agendamento.id, 'CANCELADO');
            if (acao === 'faltou') alterarStatus(agendamento.id, 'NAO_COMPARECEU');
            if (acao === 'excluir') excluirAgendamento(agendamento.id, agendamento.clienteNome);

            // Abre e fecha o campo de nova data.
            if (acao === 'remarcar') {
                caixaRemarcar.classList.toggle('visivel');
            }

            if (acao === 'confirmarRemarcacao') {
                const novoHorario = caixaRemarcar.querySelector('[data-campo="novoHorario"]').value;
                if (novoHorario) {
                    remarcarAgendamento(agendamento.id, novoHorario);
                }
            }
        });
    });
}

// PATCH no status. O endpoint recebe o novo valor como parâmetro na URL.
async function alterarStatus(id, novoStatus) {
    try {
        const resposta = await fetch(`${API}/${id}/status?novoStatus=${novoStatus}`, {
            method: 'PATCH'
        });

        if (!resposta.ok) {
            mostrarMensagem('Não foi possível alterar o status.', 'erro');
            return;
        }

        carregarAgendamentos(filtroDia.value);

    } catch (erro) {
        mostrarMensagem('Erro de conexão com o servidor.', 'erro');
    }
}

// PATCH no horário. Pode devolver 409 se o novo horário conflitar.
async function remarcarAgendamento(id, novoInicio) {
    try {
        const resposta = await fetch(`${API}/${id}/horario?novoInicio=${novoInicio}:00`, {
            method: 'PATCH'
        });

        if (resposta.status === 409) {
            const erro = await resposta.json();
            mostrarMensagem(erro.mensagem, 'erro');
            return;
        }

        if (!resposta.ok) {
            mostrarMensagem('Não foi possível remarcar.', 'erro');
            return;
        }

        mostrarMensagem('Horário alterado.', 'sucesso');
        filtroDia.value = novoInicio.substring(0, 10);
        carregarAgendamentos(filtroDia.value);

    } catch (erro) {
        mostrarMensagem('Erro de conexão com o servidor.', 'erro');
    }
}

// DELETE é irreversível, então confirma antes.
async function excluirAgendamento(id, nomeCliente) {
    const confirmado = confirm(`Excluir o agendamento de ${nomeCliente}? Esta ação não pode ser desfeita.`);
    if (!confirmado) return;

    try {
        const resposta = await fetch(`${API}/${id}`, { method: 'DELETE' });

        if (!resposta.ok) {
            mostrarMensagem('Não foi possível excluir.', 'erro');
            return;
        }

        mostrarMensagem('Agendamento excluído.', 'sucesso');
        carregarAgendamentos(filtroDia.value);

    } catch (erro) {
        mostrarMensagem('Erro de conexão com o servidor.', 'erro');
    }
}