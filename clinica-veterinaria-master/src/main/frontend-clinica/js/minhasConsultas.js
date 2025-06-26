// js/minhasConsultas.js
import { api } from './api.js';
import { showModal, displayMessage } from './common.js';

document.addEventListener('DOMContentLoaded', async () => {
    const userId = localStorage.getItem('userId');
    const consultaListDiv = document.getElementById('consultaList');
    const viewReportModal = document.getElementById('viewReportModal');
    const reportDetailsContent = document.getElementById('reportDetailsContent');

    async function loadMyConsultas() {
        if (!userId) {
            displayMessage(consultaListDiv, 'ID do usuário não encontrado para carregar consultas.', 'error');
            return;
        }
        try {
            const consultas = await api.consultas.getByOwnerId(userId);
            consultaListDiv.innerHTML = '';
            if (consultas && consultas.length > 0) {
                consultas.forEach(consulta => {
                    const consultaCard = document.createElement('div');
                    consultaCard.className = 'data-card';
                    const consultaDate = new Date(consulta.dataHora).toLocaleDateString();
                    const consultaTime = new Date(consulta.dataHora).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

                    consultaCard.innerHTML = `
                        <h4>Consulta com ${consulta.veterinario?.nome || 'N/A'}</h4>
                        <p>Pet: ${consulta.pet?.nome || 'N/A'}</p>
                        <p>Data: ${consultaDate} às ${consultaTime}</p>
                        <p>Motivo: ${consulta.motivo}</p>
                        <p>Status: ${consulta.status}</p>
                        ${consulta.relatorioId ? `<button class="btn btn-info view-report-btn" data-consulta-id="${consulta.id}" data-pet-id="${consulta.petId}">Ver Relatório</button>` : '<p>Relatório ainda não disponível.</p>'}
                    `;
                    consultaListDiv.appendChild(consultaCard);
                });
            } else {
                consultaListDiv.innerHTML = '<p>Você ainda não tem consultas agendadas.</p>';
            }
        } catch (error) {
            console.error('Erro ao carregar consultas:', error);
            displayMessage(consultaListDiv, error.message || 'Erro ao carregar suas consultas.', 'error');
        }
    }

    // Delegar evento para os botões de "Ver Relatório"
    document.addEventListener('click', async (e) => {
        if (e.target.classList.contains('view-report-btn')) {
            const consultaId = e.target.dataset.consultaId;
            const petId = e.target.dataset.petId; // Para carregar vacinas do pet
            try {
                const relatorio = await api.relatoriosConsulta.getByConsultaId(consultaId);
                const consulta = await api.consultas.getById(consultaId);

                if (consulta && relatorio) {
                    document.getElementById('reportConsultaDataHora').textContent = new Date(consulta.dataHora).toLocaleString();
                    document.getElementById('reportConsultaPet').textContent = consulta.pet?.nome || 'N/A';
                    document.getElementById('reportConsultaVeterinario').textContent = consulta.veterinario?.nome || 'N/A';
                    document.getElementById('reportConsultaMotivo').textContent = consulta.motivo;
                    document.getElementById('reportConsultaStatus').textContent = consulta.status;
                    document.getElementById('reportDescription').textContent = relatorio.descricao;

                    // Carregar vacinas aplicadas ao pet desta consulta
                    const vacinasAplicadasList = document.getElementById('vacinasAplicadasList');
                    vacinasAplicadasList.innerHTML = '';
                    const petVacinas = await api.vacinasAplicadas.getByPetId(petId);
                    if (petVacinas && petVacinas.length > 0) {
                        petVacinas.forEach(vacina => {
                            const li = document.createElement('li');
                            li.textContent = `${vacina.tipoVacina?.nome || 'Vacina Desconhecida'} em ${new Date(vacina.dataAplicacao).toLocaleDateString()} (Veterinário: ${vacina.veterinario?.nome || 'N/A'})`;
                            vacinasAplicadasList.appendChild(li);
                        });
                    } else {
                        vacinasAplicadasList.innerHTML = '<li>Nenhuma vacina registrada para este pet.</li>';
                    }
                    showModal(viewReportModal);
                } else {
                    displayMessage(consultaListDiv, 'Relatório ou consulta não encontrado.', 'error');
                }
            } catch (error) {
                console.error('Erro ao carregar relatório/vacinas:', error);
                displayMessage(consultaListDiv, error.message || 'Não foi possível carregar o relatório ou as vacinas.', 'error');
            }
        }
    });

    if (userId) {
        loadMyConsultas();
    }
});