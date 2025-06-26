// js/clienteDashboard.js
import { api } from './api.js';
import { displayMessage } from './common.js';

document.addEventListener('DOMContentLoaded', async () => {
    const userId = localStorage.getItem('userId');
    const userName = localStorage.getItem('userName');
    const clientNameDisplay = document.getElementById('clientNameDisplay');
    const latestActivitiesDiv = document.getElementById('latestActivities');

    if (clientNameDisplay) {
        clientNameDisplay.textContent = userName;
    }

    async function loadLatestActivities() {
        if (!userId) {
            displayMessage(latestActivitiesDiv, 'ID do usuário não encontrado.', 'error');
            return;
        }

        try {
            latestActivitiesDiv.innerHTML = '<p>Carregando...</p>';

            // Buscar as últimas consultas
            const consultas = await api.consultas.getByOwnerId(userId);
            let consultaHtml = '<h4>Últimas Consultas:</h4>';
            if (consultas && consultas.length > 0) {
                // Ordenar por data mais recente e pegar, por exemplo, as 3 últimas
                const sortedConsultas = consultas.sort((a, b) => new Date(b.dataHora) - new Date(a.dataHora)).slice(0, 3);
                sortedConsultas.forEach(consulta => {
                    const dataHora = new Date(consulta.dataHora).toLocaleString();
                    consultaHtml += `<div class="data-card">
                        <p><strong>Pet:</strong> ${consulta.pet?.nome || 'N/A'}</p>
                        <p><strong>Data/Hora:</strong> ${dataHora}</p>
                        <p><strong>Motivo:</strong> ${consulta.motivo}</p>
                        <p><strong>Status:</strong> ${consulta.status}</p>
                        ${consulta.relatorioId ? `<a href="relatorio-consulta.html?consultaId=${consulta.id}" class="btn btn-info btn-small">Ver Relatório</a>` : ''}
                    </div>`;
                });
            } else {
                consultaHtml += '<p>Nenhuma consulta recente.</p>';
            }

            latestActivitiesDiv.innerHTML = consultaHtml;

            // Opcional: Adicionar últimas vacinas aplicadas nos pets do cliente
            // Isso exigiria uma rota no backend para "vacinas aplicadas pelos meus pets"
            // Por simplicidade, vou omitir por enquanto, mas seria similar
            // à lógica de carregar pets e depois suas vacinas.

        } catch (error) {
            console.error('Erro ao carregar atividades recentes:', error);
            displayMessage(latestActivitiesDiv, error.message || 'Erro ao carregar atividades recentes.', 'error');
        }
    }

    if (userId) {
        loadLatestActivities();
    }
});