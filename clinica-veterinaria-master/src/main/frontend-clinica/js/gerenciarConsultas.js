// js/gerenciarConsultas.js
import { api } from './api.js';
import { showModal, hideModal, displayMessage, formatDateForInput, formatTimeForInput } from './common.js';

document.addEventListener('DOMContentLoaded', async () => {
    const addConsultaBtn = document.getElementById('addConsultaBtn');
    const consultaListDiv = document.getElementById('consultaList');
    const consultaSearchInput = document.getElementById('consultaSearch');

    const consultaModal = document.getElementById('consultaModal');
    const consultaModalTitle = document.getElementById('consultaModalTitle');
    const consultaForm = document.getElementById('consultaForm');
    const consultaIdHidden = document.getElementById('consultaIdHidden');
    const consultaPetIdSelect = document.getElementById('consultaPetId');
    const consultaDonoInfoInput = document.getElementById('consultaDonoInfo');
    const consultaVeterinarioIdSelect = document.getElementById('consultaVeterinarioId');
    const consultaDataInput = document.getElementById('consultaData');
    const consultaHoraInput = document.getElementById('consultaHora');
    const consultaMotivoInput = document.getElementById('consultaMotivo');
    const consultaStatusSelect = document.getElementById('consultaStatus');
    const consultaSubmitBtn = consultaForm.querySelector('button[type="submit"]');
    const consultaMessageDiv = document.getElementById('consultaMessage');

    let allPets = []; // Armazenar todos os pets para fácil acesso

    async function loadPetsForSelects() {
        try {
            allPets = await api.pets.getAll();
            consultaPetIdSelect.innerHTML = '<option value="">Selecione um Pet</option>';
            if (allPets && allPets.length > 0) {
                allPets.forEach(pet => {
                    const option = document.createElement('option');
                    option.value = pet.id;
                    option.textContent = `${pet.nome} (Dono: ${pet.dono?.nome || 'N/A'})`;
                    option.dataset.donoNome = pet.dono?.nome || 'N/A'; // Armazena o nome do dono
                    option.dataset.donoId = pet.donoId; // Armazena o ID do dono
                    consultaPetIdSelect.appendChild(option);
                });
            }
        } catch (error) {
            console.error('Erro ao carregar pets para selects:', error);
            displayMessage(consultaMessageDiv, 'Erro ao carregar lista de pets.', 'error');
        }
    }

    async function loadVeterinariosForSelects() {
        try {
            const veterinarios = await api.users.getVeterinarios();
            consultaVeterinarioIdSelect.innerHTML = '<option value="">Selecione um Veterinário</option>';
            if (veterinarios && veterinarios.length > 0) {
                veterinarios.forEach(vet => {
                    const option = document.createElement('option');
                    option.value = vet.id;
                    option.textContent = vet.nome;
                    consultaVeterinarioIdSelect.appendChild(option);
                });
            }
        } catch (error) {
            console.error('Erro ao carregar veterinários para selects:', error);
            displayMessage(consultaMessageDiv, 'Erro ao carregar lista de veterinários.', 'error');
        }
    }

    if (consultaPetIdSelect) {
        consultaPetIdSelect.addEventListener('change', () => {
            const selectedOption = consultaPetIdSelect.options[consultaPetIdSelect.selectedIndex];
            consultaDonoInfoInput.value = selectedOption.dataset.donoNome || '';
        });
    }

    async function loadConsultas(searchQuery = '') {
        try {
            const consultas = await api.consultas.getAll();
            consultaListDiv.innerHTML = '';
            const filteredConsultas = consultas.filter(consulta =>
                (consulta.pet?.nome.toLowerCase().includes(searchQuery.toLowerCase()) ||
                 consulta.dono?.nome.toLowerCase().includes(searchQuery.toLowerCase()) ||
                 consulta.veterinario?.nome.toLowerCase().includes(searchQuery.toLowerCase()) ||
                 new Date(consulta.dataHora).toLocaleDateString().includes(searchQuery))
            );

            if (filteredConsultas && filteredConsultas.length > 0) {
                filteredConsultas.forEach(consulta => {
                    const consultaCard = document.createElement('div');
                    consultaCard.className = 'data-card';
                    const consultaDate = new Date(consulta.dataHora).toLocaleDateString();
                    const consultaTime = new Date(consulta.dataHora).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

                    consultaCard.innerHTML = `
                        <h4>Consulta em ${consultaDate} às ${consultaTime}</h4>
                        <p>Pet: ${consulta.pet?.nome || 'N/A'} (Dono: ${consulta.dono?.nome || 'N/A'})</p>
                        <p>Veterinário: ${consulta.veterinario?.nome || 'N/A'}</p>
                        <p>Motivo: ${consulta.motivo}</p>
                        <p>Status: ${consulta.status}</p>
                        <button class="btn btn-secondary edit-consulta-btn" data-id="${consulta.id}">Editar</button>
                        <button class="btn btn-danger delete-consulta-btn" data-id="${consulta.id}">Excluir</button>
                        ${consulta.relatorioId ? `<a href="relatorio-consulta.html?consultaId=${consulta.id}" class="btn btn-info btn-small">Ver Relatório</a>` : ''}
                    `;
                    consultaListDiv.appendChild(consultaCard);
                });
            } else {
                consultaListDiv.innerHTML = '<p>Nenhuma consulta encontrada.</p>';
            }
        } catch (error) {
            console.error('Erro ao carregar consultas:', error);
            displayMessage(consultaListDiv, error.message || 'Erro ao carregar consultas.', 'error');
        }
    }

    if (addConsultaBtn) {
        addConsultaBtn.addEventListener('click', () => {
            consultaModalTitle.textContent = 'Agendar Nova Consulta';
            consultaSubmitBtn.textContent = 'Agendar Consulta';
            consultaIdHidden.value = '';
            consultaForm.reset();
            consultaDonoInfoInput.value = ''; // Limpa o dono
            loadPetsForSelects();
            loadVeterinariosForSelects();
            showModal(consultaModal);
        });
    }

    consultaListDiv.addEventListener('click', async (e) => {
        if (e.target.classList.contains('edit-consulta-btn')) {
            const consultaId = e.target.dataset.id;
            try {
                const consulta = await api.consultas.getById(consultaId);
                consultaModalTitle.textContent = 'Editar Consulta';
                consultaSubmitBtn.textContent = 'Salvar Alterações';
                consultaIdHidden.value = consulta.id;

                await loadPetsForSelects();
                consultaPetIdSelect.value = consulta.petId;
                consultaDonoInfoInput.value = consulta.dono?.nome || 'N/A'; // Preenche o dono

                await loadVeterinariosForSelects();
                consultaVeterinarioIdSelect.value = consulta.veterinarioId;

                consultaDataInput.value = formatDateForInput(consulta.dataHora);
                consultaHoraInput.value = formatTimeForInput(consulta.dataHora);
                consultaMotivoInput.value = consulta.motivo || '';
                consultaStatusSelect.value = consulta.status;
                showModal(consultaModal);
            } catch (error) {
                console.error('Erro ao buscar consulta para edição:', error);
                displayMessage(consultaListDiv, error.message || 'Erro ao carregar dados da consulta.', 'error');
            }
        } else if (e.target.classList.contains('delete-consulta-btn')) {
            const consultaIdToDelete = e.target.dataset.id;
            if (confirm('Tem certeza que deseja excluir esta consulta?')) {
                try {
                    const response = await api.consultas.remove(consultaIdToDelete);
                    displayMessage(consultaListDiv, response.message, 'success');
                    await loadConsultas();
                } catch (error) {
                    console.error('Erro ao excluir consulta:', error);
                    displayMessage(consultaListDiv, error.message || 'Erro ao excluir consulta.', 'error');
                }
            }
        }
    });

    if (consultaForm) {
        consultaForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const id = consultaIdHidden.value;
            const dataHora = new Date(`${consultaDataInput.value}T${consultaHoraInput.value}`);

            const consultaData = {
                petId: consultaPetIdSelect.value,
                veterinarioId: consultaVeterinarioIdSelect.value,
                dataHora: dataHora.toISOString(),
                motivo: consultaMotivoInput.value,
                status: consultaStatusSelect.value,
                donoId: consultaPetIdSelect.options[consultaPetIdSelect.selectedIndex].dataset.donoId // Pega o dono ID do pet selecionado
            };

            try {
                let response;
                if (id) {
                    response = await api.consultas.update(id, consultaData);
                } else {
                    response = await api.consultas.create(consultaData);
                }
                displayMessage(consultaMessageDiv, response.message, 'success');
                consultaForm.reset();
                await loadConsultas();
                setTimeout(() => hideModal(consultaModal), 1000);
            } catch (error) {
                console.error('Erro ao salvar consulta:', error);
                displayMessage(consultaMessageDiv, error.message || 'Erro ao salvar consulta.', 'error');
            }
        });
    }

    if (consultaSearchInput) {
        consultaSearchInput.addEventListener('input', (e) => {
            loadConsultas(e.target.value);
        });
    }

    loadConsultas();
    loadPetsForSelects(); // Inicializa o select de pets
    loadVeterinariosForSelects(); // Inicializa o select de veterinários
});