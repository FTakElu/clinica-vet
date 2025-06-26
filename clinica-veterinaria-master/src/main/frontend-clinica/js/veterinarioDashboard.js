// js/veterinarioDashboard.js
import { api } from './api.js';
import { showModal, displayMessage } from './common.js';

document.addEventListener('DOMContentLoaded', async () => {
    const userId = localStorage.getItem('userId');
    const userName = localStorage.getItem('userName');
    const veterinarioNameDisplay = document.getElementById('veterinarioNameDisplay');
    if (veterinarioNameDisplay) {
        veterinarioNameDisplay.textContent = userName;
    }

    const consultaListDiv = document.getElementById('consultaList');
    const consultaSearchInput = document.getElementById('consultaSearch');

    const relatorioConsultaModal = document.getElementById('relatorioConsultaModal');
    const relatorioModalTitle = document.getElementById('relatorioModalTitle');
    const relatorioConsultaForm = document.getElementById('relatorioConsultaForm');
    const relatorioConsultaIdHidden = document.getElementById('relatorioConsultaIdHidden');
    const relatorioConsultaInfo = document.getElementById('relatorioConsultaInfo');
    const relatorioDescricaoInput = document.getElementById('relatorioDescricao');
    const relatorioSubmitBtn = document.getElementById('relatorioSubmitBtn');
    const relatorioMessageDiv = document.getElementById('relatorioMessage');

    const viewPetVaccinesModal = document.getElementById('viewPetVaccinesModal');
    const petNameForVaccines = document.getElementById('petNameForVaccines');
    const vacinasAplicadasList = document.getElementById('vacinasAplicadasList');

    async function loadMyConsultas(searchQuery = '') {
        if (!userId) {
            displayMessage(consultaListDiv, 'ID do veterinário não encontrado.', 'error');
            return;
        }
        try {
            const consultas = await api.consultas.getByVeterinarioId(userId);
            consultaListDiv.innerHTML = '';
            const filteredConsultas = consultas.filter(consulta =>
                (consulta.pet?.nome.toLowerCase().includes(searchQuery.toLowerCase()) ||
                 consulta.dono?.nome.toLowerCase().includes(searchQuery.toLowerCase()) ||
                 new Date(consulta.dataHora).toLocaleDateString().includes(searchQuery))
            );

            if (filteredConsultas && filteredConsultas.length > 0) {
                filteredConsultas.forEach(consulta => {
                    const consultaCard = document.createElement('div');
                    consultaCard.className = 'data-card';
                    const consultaDate = new Date(consulta.dataHora).toLocaleDateString();
                    const consultaTime = new Date(consulta.dataHora).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

                    consultaCard.innerHTML = `
                        <h4>Consulta com ${consulta.pet?.nome || 'N/A'} (Dono: ${consulta.dono?.nome || 'N/A'})</h4>
                        <p>Data: ${consultaDate} às ${consultaTime}</p>
                        <p>Motivo: ${consulta.motivo}</p>
                        <p>Status: ${consulta.status}</p>
                        <button class="btn btn-secondary update-status-btn" data-id="${consulta.id}" data-status="${consulta.status}">Atualizar Status</button>
                        ${consulta.relatorioId ? `<button class="btn btn-info view-report-btn" data-consulta-id="${consulta.id}" data-pet-id="${consulta.petId}">Ver Relatório</button>` : `<button class="btn btn-success generate-report-btn" data-consulta-id="${consulta.id}" data-pet-id="${consulta.petId}">Gerar Relatório</button>`}
                        <button class="btn btn-warning view-pet-vaccines" data-pet-id="${consulta.petId}" data-pet-name="${consulta.pet?.nome || 'N/A'}">Ver Histórico de Vacinas do Pet</button>
                    `;
                    consultaListDiv.appendChild(consultaCard);
                });
            } else {
                consultaListDiv.innerHTML = '<p>Nenhuma consulta agendada para você.</p>';
            }
        } catch (error) {
            console.error('Erro ao carregar consultas:', error);
            displayMessage(consultaListDiv, error.message || 'Erro ao carregar suas consultas.', 'error');
        }
    }

    // --- Lógica para Gerar/Visualizar Relatório de Consulta ---
    consultaListDiv.addEventListener('click', async (e) => {
        if (e.target.classList.contains('generate-report-btn')) {
            const consultaId = e.target.dataset.consultaId;
            try {
                const consulta = await api.consultas.getById(consultaId);
                relatorioModalTitle.textContent = 'Gerar Relatório de Consulta';
                relatorioSubmitBtn.textContent = 'Gerar Relatório';
                relatorioConsultaIdHidden.value = consultaId;
                relatorioDescricaoInput.value = ''; // Limpa para nova criação
                relatorioConsultaInfo.innerHTML = `<strong>Pet:</strong> ${consulta.pet?.nome || 'N/A'} | <strong>Data:</strong> ${new Date(consulta.dataHora).toLocaleDateString()}`;
                showModal(relatorioConsultaModal);
            } catch (error) {
                console.error('Erro ao buscar consulta para gerar relatório:', error);
                displayMessage(consultaListDiv, error.message || 'Erro ao carregar dados da consulta.', 'error');
            }
        } else if (e.target.classList.contains('view-report-btn')) {
            const consultaId = e.target.dataset.consultaId;
            try {
                const relatorio = await api.relatoriosConsulta.getByConsultaId(consultaId);
                const consulta = await api.consultas.getById(consultaId);

                if (relatorio && consulta) {
                    relatorioModalTitle.textContent = 'Visualizar/Editar Relatório de Consulta';
                    relatorioSubmitBtn.textContent = 'Salvar Alterações'; // Permite edição se for o veterinário logado
                    relatorioConsultaIdHidden.value = consultaId;
                    relatorioDescricaoInput.value = relatorio.descricao;
                    relatorioConsultaInfo.innerHTML = `<strong>Pet:</strong> ${consulta.pet?.nome || 'N/A'} | <strong>Data:</strong> ${new Date(consulta.dataHora).toLocaleDateString()}`;
                    showModal(relatorioConsultaModal);
                } else {
                    displayMessage(consultaListDiv, 'Relatório não encontrado para esta consulta.', 'error');
                }
            } catch (error) {
                console.error('Erro ao buscar relatório para visualização:', error);
                displayMessage(consultaListDiv, error.message || 'Erro ao carregar relatório.', 'error');
            }
        } else if (e.target.classList.contains('update-status-btn')) {
            const consultaId = e.target.dataset.id;
            const currentStatus = e.target.dataset.status;
            let newStatus;
            if (currentStatus === 'Agendada' || currentStatus === 'Confirmada') {
                newStatus = 'Em Andamento';
            } else if (currentStatus === 'Em Andamento') {
                newStatus = 'Concluída';
            } else {
                displayMessage(consultaListDiv, 'Status da consulta não pode ser alterado a partir do estado atual.', 'info');
                return;
            }

            if (confirm(`Mudar status da consulta para "${newStatus}"?`)) {
                try {
                    const response = await api.consultas.update(consultaId, { status: newStatus });
                    displayMessage(consultaListDiv, response.message, 'success');
                    await loadMyConsultas();
                } catch (error) {
                    console.error('Erro ao atualizar status da consulta:', error);
                    displayMessage(consultaListDiv, error.message || 'Erro ao atualizar status.', 'error');
                }
            }
        } else if (e.target.classList.contains('view-pet-vaccines')) {
            const petId = e.target.dataset.petId;
            const petName = e.target.dataset.petName;
            try {
                const vacinas = await api.vacinasAplicadas.getByPetId(petId);
                petNameForVaccines.textContent = petName;
                vacinasAplicadasList.innerHTML = '';
                if (vacinas && vacinas.length > 0) {
                    vacinas.forEach(vacina => {
                        const li = document.createElement('li');
                        li.textContent = `${vacina.tipoVacina?.nome || 'Vacina Desconhecida'} em ${new Date(vacina.dataAplicacao).toLocaleDateString()} (Por: ${vacina.veterinario?.nome || 'N/A'})`;
                        vacinasAplicadasList.appendChild(li);
                    });
                } else {
                    vacinasAplicadasList.innerHTML = '<li>Nenhuma vacina registrada para este pet.</li>';
                }
                showModal(viewPetVaccinesModal);
            } catch (error) {
                console.error('Erro ao carregar vacinas do pet:', error);
                displayMessage(consultaListDiv, error.message || 'Não foi possível carregar as vacinas para este pet.', 'error');
            }
        }
    });

    if (relatorioConsultaForm) {
        relatorioConsultaForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const consultaId = relatorioConsultaIdHidden.value;
            const descricao = relatorioDescricaoInput.value;

            try {
                let response;
                // Verificar se já existe relatório para esta consulta
                const existingReport = await api.relatoriosConsulta.getByConsultaId(consultaId).catch(() => null);

                if (existingReport && existingReport.id) { // Se o relatório já existe, atualiza
                    response = await api.relatoriosConsulta.update(consultaId, { descricao });
                } else { // Se não existe, cria um novo
                    response = await api.relatoriosConsulta.create({ consultaId, descricao, veterinarioId: userId });
                }
                displayMessage(relatorioMessageDiv, response.message, 'success');
                relatorioConsultaForm.reset();
                await loadMyConsultas(); // Recarrega a lista de consultas
                setTimeout(() => hideModal(relatorioConsultaModal), 1000);
            } catch (error) {
                console.error('Erro ao salvar relatório:', error);
                displayMessage(relatorioMessageDiv, error.message || 'Erro ao salvar relatório.', 'error');
            }
        });
    }

    if (consultaSearchInput) {
        consultaSearchInput.addEventListener('input', (e) => {
            loadMyConsultas(e.target.value);
        });
    }

    if (userId) {
        loadMyConsultas();
    }
});