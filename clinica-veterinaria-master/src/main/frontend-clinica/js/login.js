// js/login.js
 import { api } from './api.js'; // Importa a api
 import { displayMessage } from './common.js'; // Importa função de mensagem


 document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    const messageDiv = document.getElementById('message');


    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();


            const email = document.getElementById('email').value;
            const senha = document.getElementById('senha').value;


            try {
                const data = await api.auth.login(email, senha); // Usa a função de login da api.js


                // Armazena o token e a role no localStorage
                localStorage.setItem('token', data.token);
                localStorage.setItem('userRole', data.userRole);
                localStorage.setItem('userId', data.userId);
                localStorage.setItem('userName', data.userName);


                displayMessage(messageDiv, data.message || 'Login bem-sucedido!', 'success');


                // Redireciona para o dashboard apropriado
                setTimeout(() => {
                    const roleToDashboardMap = {
                        'ADMIN': 'admin-dashboard.html',      // REMOVIDA A BARRA INICIAL
                        'SECRETARIO': 'secretario-dashboard.html', // REMOVIDA A BARRA INICIAL
                        'VETERINARIO': 'veterinario-dashboard.html', // REMOVIDA A BARRA INICIAL
                        'CLIENTE': 'cliente-dashboard.html',    // REMOVIDA A BARRA INICIAL
                    };


                    const userRoleUppercase = data.userRole.toUpperCase();


                    if (roleToDashboardMap[userRoleUppercase]) {
                        // Usa o caminho relativo diretamente, sem barra inicial
                        window.location.href = roleToDashboardMap[userRoleUppercase];
                    } else {
                        console.warn('Role desconhecida ou dashboard não mapeado:', data.userRole);
                        // Redireciona para o login novamente se a role for desconhecida ou não mapeada
                        window.location.href = 'login.html';
                    }
                }, 500);


            } catch (error) {
                console.error('Erro ao fazer login:', error);
                displayMessage(messageDiv, error.message || 'Erro ao fazer login. Verifique suas credenciais.', 'error');
            }
        });
    }
 });
