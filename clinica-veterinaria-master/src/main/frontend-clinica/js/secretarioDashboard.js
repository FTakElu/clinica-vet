// js/secretarioDashboard.js
import { api } from './api.js';
import { displayMessage } from './common.js';

document.addEventListener('DOMContentLoaded', async () => {
    console.log('secretarioDashboard.js carregado.');

    const welcomeMessageElement = document.getElementById('welcomeMessage'); // ID do span na navbar
    const secretaryNameDisplay = document.getElementById('secretaryNameDisplay'); // ID do span no corpo
    const logoutButton = document.getElementById('logoutBtn'); // ID do botão de sair
    const systemActivitiesDiv = document.getElementById('systemActivities');

    // Seleciona os botões de Gerenciamento (tags <a>)
    const gerenciarUsuariosBtn = document.getElementById('gerenciar-usuarios-btn');
    const gerenciarPetsBtn = document.getElementById('gerenciar-pets-btn');
    const gerenciarConsultasBtn = document.getElementById('gerenciar-consultas-btn');
    const gerenciarTiposVacinaBtn = document.getElementById('gerenciar-tipos-vacina-btn');

    // Carregar nome do usuário logado
    const userName = localStorage.getItem('userName');
    if (userName) {
        welcomeMessageElement.textContent = `Olá, ${userName}!`;
        secretaryNameDisplay.textContent = userName;
    } else {
        welcomeMessageElement.textContent = 'Olá!';
        secretaryNameDisplay.textContent = 'Usuário';
    }

    // Lógica para o botão de Sair
    if (logoutButton) {
        logoutButton.addEventListener('click', () => {
            console.log('Botão Sair clicado!');
            localStorage.removeItem('jwtToken');
            localStorage.removeItem('userName');
            localStorage.removeItem('userId');
            localStorage.removeItem('userRole');
            window.location.href = 'login.html'; // Redireciona para a página de login
        });
    } else {
        console.error('Erro: Botão de Sair com ID "logoutBtn" não encontrado.');
    }

    // Adicionar event listeners para os botões de gerenciamento
    // e.preventDefault() é ESSENCIAL aqui para que o JavaScript controle o redirecionamento
    if (gerenciarUsuariosBtn) {
        gerenciarUsuariosBtn.addEventListener('click', (e) => {
            e.preventDefault(); // Impede o comportamento padrão do link (o navegador seguir o href)
            console.log('Botão Gerenciar Usuários clicado!');
            window.location.href = 'gerenciar-usuarios.html';
        });
    } else {
        console.error('Erro: Botão "Gerenciar Usuários" não encontrado.');
    }

    if (gerenciarPetsBtn) {
        gerenciarPetsBtn.addEventListener('click', (e) => {
            e.preventDefault(); // Impede o comportamento padrão do link
            console.log('Botão Gerenciar Pets clicado!');
            window.location.href = 'gerenciarPets.html'; // Corrigido para 'gerenciarPets.html' (com 'P' maiúsculo)
        });
    } else {
        console.error('Erro: Botão "Gerenciar Pets" não encontrado.');
    }

    if (gerenciarConsultasBtn) {
        gerenciarConsultasBtn.addEventListener('click', (e) => {
            e.preventDefault(); // Impede o comportamento padrão do link
            console.log('Botão Gerenciar Consultas clicado!');
            window.location.href = 'gerenciar-consultas.html';
        });
    } else {
        console.error('Erro: Botão "Gerenciar Consultas" não encontrado.');
    }

    if (gerenciarTiposVacinaBtn) {
        gerenciarTiposVacinaBtn.addEventListener('click', (e) => {
            e.preventDefault(); // Impede o comportamento padrão do link
            console.log('Botão Gerenciar Tipos de Vacina clicado!');
            window.location.href = 'gerenciar-tipos-vacina.html';
        });
    } else {
        console.error('Erro: Botão "Gerenciar Tipos de Vacina" não encontrado.');
    }


    // Função para carregar atividades recentes do sistema
    async function loadSystemActivities() {
        try {
            systemActivitiesDiv.innerHTML = '<p>Carregando atividades recentes...</p>';
            let contentHtml = '';

            // Últimas Consultas
            const consultas = await api.consultas.getAll();
            contentHtml += '<h4 class="mt-6 mb-3 text-lg font-semibold">Últimas Consultas Agendadas:</h4>';
            if (consultas && consultas.length > 0) {
                const sortedConsultas = consultas.sort((a, b) => new Date(b.dataHora) - new Date(a.dataHora)).slice(0, 5);
                sortedConsultas.forEach(consulta => {
                    const dataHora = new Date(consulta.dataHora).toLocaleString('pt-BR');
                    contentHtml += `<div class="data-card mb-2 p-3 border border-gray-200 rounded-md shadow-sm">
                        <p><strong>Pet:</strong> ${consulta.pet?.nome || 'N/A'} (Dono: ${consulta.cliente?.nomeCompleto || 'N/A'})</p>
                        <p><strong>Veterinário:</strong> ${consulta.veterinario?.nomeCompleto || 'N/A'}</p>
                        <p><strong>Data/Hora:</strong> ${dataHora}</p>
                        <p><strong>Status:</strong> ${consulta.status}</p>
                    </div>`;
                });
            } else {
                contentHtml += '<p class="text-gray-500">Nenhuma consulta recente.</p>';
            }

            // Últimos Pets Cadastrados
            const pets = await api.pets.getAll();
            contentHtml += '<h4 class="mt-6 mb-3 text-lg font-semibold">Últimos Pets Cadastrados:</h4>';
            if (pets && pets.length > 0) {
                const sortedPets = pets.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)).slice(0, 3); // Assuming createdAt field
                sortedPets.forEach(pet => {
                    contentHtml += `<div class="data-card mb-2 p-3 border border-gray-200 rounded-md shadow-sm">
                        <p><strong>Nome:</strong> ${pet.nome}</p>
                        <p><strong>Espécie:</strong> ${pet.especie}</p>
                        <p><strong>Dono:</strong> ${pet.dono?.nomeCompleto || 'N/A'}</p>
                    </div>`;
                });
            } else {
                contentHtml += '<p class="text-gray-500">Nenhum pet recente.</p>';
            }

            systemActivitiesDiv.innerHTML = contentHtml;

        } catch (error) {
            console.error('Erro ao carregar atividades do sistema:', error);
            displayMessage(systemActivitiesDiv, error.message || 'Erro ao carregar atividades do sistema. Verifique o console para mais detalhes.', 'error');
        }
    }

    loadSystemActivities();
});
