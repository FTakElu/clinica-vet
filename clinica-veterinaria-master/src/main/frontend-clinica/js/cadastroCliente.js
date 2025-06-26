// js/cadastroCliente.js
import { api } from './api.js'; // Importa a api
import { displayMessage } from './common.js'; // Importa função de mensagem

document.addEventListener('DOMContentLoaded', () => {
    const cadastroClienteForm = document.getElementById('cadastroClienteForm');
    const messageDiv = document.getElementById('message');

    if (cadastroClienteForm) {
        cadastroClienteForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            const nome = document.getElementById('nome').value;
            const email = document.getElementById('email').value;
            const senha = document.getElementById('senha').value;
            const telefone = document.getElementById('telefone').value;
            const endereco = document.getElementById('endereco').value;

            try {
                // Usa a rota de registro específica para clientes
                const data = await api.auth.registerClient({ nome, email, senha, telefone, endereco });

                displayMessage(messageDiv, data.message, 'success');
                cadastroClienteForm.reset(); // Limpa o formulário

                // Opcional: Redirecionar para login após sucesso
                setTimeout(() => {
                    window.location.href = 'login.html';
                }, 1500);
            } catch (error) {
                console.error('Erro ao cadastrar cliente:', error);
                displayMessage(messageDiv, error.message || 'Erro ao cadastrar. Tente novamente.', 'error');
            }
        });
    }
});