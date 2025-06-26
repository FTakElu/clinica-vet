// js/auth.js (Focado em autenticação e redirecionamento de sessão)
import { api } from './api.js';

document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('token');
    const userRole = localStorage.getItem('userRole'); // Ex: ADMIN, CLIENTE, etc.
    const userName = localStorage.getItem('userName');
    const currentPagePath = window.location.pathname; // Ex: /gerenciar-consultas.html

    const logoutBtn = document.getElementById('logoutBtn');

    // Atualiza a mensagem de boas-vindas se o elemento existir
    const welcomeMessage = document.getElementById('welcomeMessage');
    if (welcomeMessage && userName) {
        welcomeMessage.textContent = `Olá, ${userName}!`;
    }

    // Mapeamento de dashboard principal por role
    const roleToDashboardMap = {
        'ADMIN': 'admin-dashboard.html',
        'SECRETARIO': 'secretario-dashboard.html',
        'VETERINARIO': 'veterinario-dashboard.html',
        'CLIENTE': 'cliente-dashboard.html',
    };

    // Mapeamento de páginas permitidas por role
    const allowedPagesByRole = {
        'ADMIN': [
            'admin-dashboard.html',
            'gerenciar-usuarios.html'
        ],
        'SECRETARIO': [
            'secretario-dashboard.html',
            'gerenciar-consultas.html',
            'gerenciarPets.html',
            'gerenciar-usuarios.html',
            'gerenciar-tipos-vacina.html'
        ],
        'VETERINARIO': [
            'veterinario-dashboard.html'
        ],
        'CLIENTE': [
            'cliente-dashboard.html'
        ]
    };

    // Páginas públicas que não exigem login
    const publicPages = [
        'login.html',
        'cadastro-cliente.html',
        'index.html'
    ];

    function handleInitialAccessRedirect() {
        const isPublicPage = publicPages.some(page => currentPagePath.includes(page));

        if (token && userRole) {
            const expectedDashboard = roleToDashboardMap[userRole];
            const allowedPages = allowedPagesByRole[userRole] || [];

            const isAllowedPage = allowedPages.some(page => currentPagePath.includes(page));

            if (isPublicPage) {
                // Usuário logado tentando acessar página pública → redirecionar para dashboard
                window.location.href = expectedDashboard || 'login.html';
                return;
            }

            if (!isAllowedPage) {
                // Usuário logado tentando acessar página não permitida para a role → redirecionar
                console.warn(`Página ${currentPagePath} não permitida para a role ${userRole}.`);
                window.location.href = expectedDashboard || 'login.html';
                return;
            }

            // Usuário logado e na página permitida → continua
        } else {
            // Não logado
            if (!isPublicPage) {
                console.warn('Usuário não logado. Redirecionando para login.');
                localStorage.clear();
                window.location.href = 'login.html';
                return;
            }
            // Usuário não logado, mas está em página pública → tudo certo
        }
    }

    handleInitialAccessRedirect();

    // Lógica para logout
    if (logoutBtn) {
        logoutBtn.addEventListener('click', (e) => {
            e.preventDefault();
            localStorage.clear();
            window.location.href = 'login.html';
        });
    }
});
