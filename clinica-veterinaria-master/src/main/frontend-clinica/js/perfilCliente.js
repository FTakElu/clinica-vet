// js/perfilCliente.js
import { api } from './api.js';
import { showModal, hideModal, displayMessage } from './common.js';

document.addEventListener('DOMContentLoaded', async () => {
    const userId = localStorage.getItem('userId');
    const userNomeSpan = document.getElementById('userNome');
    const userEmailSpan = document.getElementById('userEmail');
    const userTelefoneSpan = document.getElementById('userTelefone');
    const userEnderecoSpan = document.getElementById('userEndereco');
    const editProfileBtn = document.getElementById('editProfileBtn');
    const editProfileModal = document.getElementById('editProfileModal');
    const editProfileForm = document.getElementById('editProfileForm');
    const editProfileMessage = document.getElementById('editProfileMessage');

    async function loadUserData() {
        if (!userId) {
            displayMessage(document.getElementById('userData'), 'ID do usuário não encontrado.', 'error');
            return;
        }
        try {
            const userData = await api.users.getById(userId);
            if (userData) {
                userNomeSpan.textContent = userData.nome;
                userEmailSpan.textContent = userData.email;
                userTelefoneSpan.textContent = userData.telefone || 'Não informado';
                userEnderecoSpan.textContent = userData.endereco || 'Não informado';

                // Preencher formulário de edição
                document.getElementById('editNome').value = userData.nome;
                document.getElementById('editEmail').value = userData.email;
                document.getElementById('editTelefone').value = userData.telefone || '';
                document.getElementById('editEndereco').value = userData.endereco || '';
            }
        } catch (error) {
            console.error('Erro ao carregar dados do usuário:', error);
            displayMessage(document.getElementById('userData'), error.message || 'Erro ao carregar seus dados.', 'error');
        }
    }

    if (editProfileBtn) {
        editProfileBtn.addEventListener('click', () => {
            showModal(editProfileModal);
        });
    }

    if (editProfileForm) {
        editProfileForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const nome = document.getElementById('editNome').value;
            const email = document.getElementById('editEmail').value;
            const telefone = document.getElementById('editTelefone').value;
            const endereco = document.getElementById('editEndereco').value;
            const senha = document.getElementById('editSenha').value; // Senha opcional

            const userData = { nome, email, telefone, endereco };
            if (senha) {
                userData.senha = senha;
            }

            try {
                const response = await api.users.update(userId, userData);
                displayMessage(editProfileMessage, response.message, 'success');
                await loadUserData(); // Recarrega os dados do usuário
                setTimeout(() => hideModal(editProfileModal), 1000);
            } catch (error) {
                console.error('Erro ao atualizar perfil:', error);
                displayMessage(editProfileMessage, error.message || 'Erro ao atualizar perfil.', 'error');
            }
        });
    }

    if (userId) {
        loadUserData();
    }
});