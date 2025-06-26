// js/gerenciarTiposVacina.js
import { api } from './api.js';
import { showModal, hideModal, displayMessage } from './common.js';

document.addEventListener('DOMContentLoaded', async () => {
    const addTipoVacinaBtn = document.getElementById('addTipoVacinaBtn');
    const tipoVacinaListDiv = document.getElementById('tipoVacinaList');
    const tipoVacinaSearchInput = document.getElementById('tipoVacinaSearch');

    const tipoVacinaModal = document.getElementById('tipoVacinaModal');
    const tipoVacinaModalTitle = document.getElementById('tipoVacinaModalTitle');
    const tipoVacinaForm = document.getElementById('tipoVacinaForm');
    const tipoVacinaIdHidden = document.getElementById('tipoVacinaIdHidden');
    const tipoVacinaNomeInput = document.getElementById('tipoVacinaNome');
    const tipoVacinaDescricaoInput = document.getElementById('tipoVacinaDescricao');
    const tipoVacinaPeriodoReforcoInput = document.getElementById('tipoVacinaPeriodoReforco'); // NOVO: Referência ao campo de período de reforço
    const tipoVacinaSubmitBtn = tipoVacinaForm.querySelector('button[type="submit"]');
    const tipoVacinaMessageDiv = document.getElementById('tipoVacinaMessage');

    async function loadTiposVacina(searchQuery = '') {
        try {
            const tipos = await api.tiposVacina.getAll(); // Verifique api.js para esta implementação
            tipoVacinaListDiv.innerHTML = '';
            const filteredTipos = tipos.filter(tipo =>
                tipo.nome.toLowerCase().includes(searchQuery.toLowerCase())
            );

            if (filteredTipos && filteredTipos.length > 0) {
                filteredTipos.forEach(tipo => {
                    const tipoCard = document.createElement('div');
                    tipoCard.className = 'data-card';
                    tipoCard.innerHTML = `
                        <h4>${tipo.nome}</h4>
                        <p>${tipo.descricao || 'Sem descrição'}</p>
                        <p>Reforço: ${tipo.periodoReforcoEmMeses > 0 ? tipo.periodoReforcoEmMeses + ' meses' : 'Não se aplica'}</p>
                        <button class="btn btn-secondary edit-tipo-vacina-btn" data-id="${tipo.id}">Editar</button>
                        <button class="btn btn-danger delete-tipo-vacina-btn" data-id="${tipo.id}">Excluir</button>
                    `;
                    tipoVacinaListDiv.appendChild(tipoCard);
                });
            } else {
                tipoVacinaListDiv.innerHTML = '<p>Nenhum tipo de vacina encontrado.</p>';
            }
        } catch (error) {
            console.error('Erro ao carregar tipos de vacina:', error);
            displayMessage(tipoVacinaListDiv, error.message || 'Erro ao carregar tipos de vacina.', 'error');
        }
    }

    if (addTipoVacinaBtn) {
        addTipoVacinaBtn.addEventListener('click', () => {
            tipoVacinaModalTitle.textContent = 'Cadastrar Novo Tipo de Vacina';
            tipoVacinaSubmitBtn.textContent = 'Cadastrar Tipo';
            tipoVacinaIdHidden.value = '';
            tipoVacinaForm.reset();
            tipoVacinaPeriodoReforcoInput.value = ''; // Reseta o novo campo
            showModal(tipoVacinaModal);
        });
    }

    tipoVacinaListDiv.addEventListener('click', async (e) => {
        if (e.target.classList.contains('edit-tipo-vacina-btn')) {
            const tipoId = e.target.dataset.id;
            try {
                const tipo = await api.tiposVacina.getById(tipoId); // Verifique api.js para esta implementação
                tipoVacinaModalTitle.textContent = 'Editar Tipo de Vacina';
                tipoVacinaSubmitBtn.textContent = 'Salvar Alterações';
                tipoVacinaIdHidden.value = tipo.id;
                tipoVacinaNomeInput.value = tipo.nome;
                tipoVacinaDescricaoInput.value = tipo.descricao || '';
                tipoVacinaPeriodoReforcoInput.value = tipo.periodoReforcoEmMeses; // Preenche o novo campo
                showModal(tipoVacinaModal);
            } catch (error) {
                console.error('Erro ao buscar tipo de vacina para edição:', error);
                displayMessage(tipoVacinaListDiv, error.message || 'Erro ao carregar dados do tipo de vacina.', 'error');
            }
        } else if (e.target.classList.contains('delete-tipo-vacina-btn')) {
            const tipoIdToDelete = e.target.dataset.id;
            if (confirm('Tem certeza que deseja excluir este tipo de vacina? Isso afetará registros de vacinas aplicadas.')) {
                try {
                    const response = await api.tiposVacina.remove(tipoIdToDelete); // Verifique api.js para esta implementação
                    displayMessage(tipoVacinaListDiv, response.message, 'success');
                    await loadTiposVacina();
                } catch (error) {
                    console.error('Erro ao excluir tipo de vacina:', error);
                    displayMessage(tipoVacinaListDiv, error.message || 'Erro ao excluir tipo de vacina.', 'error');
                }
            }
        }
    });

    if (tipoVacinaForm) {
        tipoVacinaForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const id = tipoVacinaIdHidden.value;
            const tipoData = {
                nome: tipoVacinaNomeInput.value,
                descricao: tipoVacinaDescricaoInput.value,
                periodoReforcoEmMeses: parseInt(tipoVacinaPeriodoReforcoInput.value) || 0, // Pega o novo campo e garante que seja número
            };

            try {
                let response;
                if (id) {
                    response = await api.tiposVacina.update(id, tipoData); // Verifique api.js para esta implementação
                } else {
                    response = await api.tiposVacina.create(tipoData); // Verifique api.js para esta implementação
                }
                // Assumindo que a resposta do backend tem um campo 'message'
                displayMessage(tipoVacinaMessageDiv, response.message || 'Operação realizada com sucesso!', 'success');
                tipoVacinaForm.reset();
                await loadTiposVacina();
                setTimeout(() => hideModal(tipoVacinaModal), 1000);
            } catch (error) {
                console.error('Erro ao salvar tipo de vacina:', error);
                // Exemplo: error.response.data.message se usar Axios e o backend retornar JSON de erro
                displayMessage(tipoVacinaMessageDiv, error.message || 'Erro ao salvar tipo de vacina.', 'error');
            }
        });
    }

    if (tipoVacinaSearchInput) {
        tipoVacinaSearchInput.addEventListener('input', (e) => {
            loadTiposVacina(e.target.value);
        });
    }

    // Carrega os tipos de vacina ao iniciar a página
    loadTiposVacina();
});