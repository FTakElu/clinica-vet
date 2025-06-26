// js/adminDashboard.js
import { api } from './api.js';
import { showModal, hideModal, displayMessage } from './common.js';

document.addEventListener('DOMContentLoaded', async () => {
    const userName = localStorage.getItem('userName');
    const adminNameDisplay = document.getElementById('adminNameDisplay');
    if (adminNameDisplay) {
        adminNameDisplay.textContent = userName;
    }

    const addOtherUserBtn = document.getElementById('addOtherUserBtn');
    const userListAdminDiv = document.getElementById('userListAdmin');
    const userSearchAdminInput = document.getElementById('userSearchAdmin');

    const adminUserModal = document.getElementById('adminUserModal');
    const adminUserModalTitle = document.getElementById('adminUserModalTitle');
    const adminUserForm = document.getElementById('adminUserForm');
    const adminUserIdHidden = document.getElementById('adminUserIdHidden');
    const adminUserNameInput = document.getElementById('adminUserName');
    const adminUserEmailInput = document.getElementById('adminUserEmail');
    const adminUserSenhaInput = document.getElementById('adminUserSenha');
    const adminUserRoleSelect = document.getElementById('adminUserRole');
    const adminUserTelefoneInput = document.getElementById('adminUserTelefone');
    const adminUserEnderecoInput = document.getElementById('adminUserEndereco');

    // Adicione referências aos campos específicos de role, se existirem no HTML
    const adminUserCpfInput = document.getElementById('adminUserCpf');
    const adminUserCrmvInput = document.getElementById('adminUserCrmv');
    const adminUserEspecialidadeInput = document.getElementById('adminUserEspecialidade');

    const adminUserSubmitBtn = adminUserForm.querySelector('button[type="submit"]');
    const adminUserMessageDiv = document.getElementById('adminUserMessage');

    // Função para mostrar/esconder campos baseados na role selecionada
    function toggleRoleSpecificFields() {
        const selectedRole = adminUserRoleSelect.value;

        // Oculta todos os campos específicos primeiro (e limpa seus valores)
        const allRoleSpecificInputs = [
            adminUserTelefoneInput, adminUserEnderecoInput,
            adminUserCpfInput, adminUserCrmvInput, adminUserEspecialidadeInput
        ];
        allRoleSpecificInputs.forEach(input => {
            if (input) {
                input.closest('.form-group').style.display = 'none';
                input.value = ''; // Limpa o valor para evitar envio de dados de outra role
            }
        });

        // Mostra os campos relevantes
        if (selectedRole === 'cliente') {
            if (adminUserTelefoneInput) adminUserTelefoneInput.closest('.form-group').style.display = 'block';
            if (adminUserEnderecoInput) adminUserEnderecoInput.closest('.form-group').style.display = 'block'; // Clientes geralmente têm endereço
            if (adminUserCpfInput) adminUserCpfInput.closest('.form-group').style.display = 'block';
        } else if (selectedRole === 'veterinario') {
            if (adminUserTelefoneInput) adminUserTelefoneInput.closest('.form-group').style.display = 'block'; // Veterinários também podem ter telefone
            if (adminUserEnderecoInput) adminUserEnderecoInput.closest('.form-group').style.display = 'block'; // Veterinários também podem ter endereço
            if (adminUserCrmvInput) adminUserCrmvInput.closest('.form-group').style.display = 'block';
            if (adminUserEspecialidadeInput) adminUserEspecialidadeInput.closest('.form-group').style.display = 'block';
        } else if (selectedRole === 'admin' || selectedRole === 'secretario') {
            if (adminUserTelefoneInput) adminUserTelefoneInput.closest('.form-group').style.display = 'block';
            if (adminUserEnderecoInput) adminUserEnderecoInput.closest('.form-group').style.display = 'block';
        }
    }

    // Adicionar listener para o select da role
    if (adminUserRoleSelect) {
        adminUserRoleSelect.addEventListener('change', toggleRoleSpecificFields);
    }


    async function loadAllUsers(searchQuery = '') {
        try {
            const users = await api.users.getAll(); // Admin pode ver todos os usuários
            userListAdminDiv.innerHTML = '';
            const filteredUsers = users.filter(user =>
                (user.nomeCompleto && user.nomeCompleto.toLowerCase().includes(searchQuery.toLowerCase())) || // Usar nomeCompleto
                (user.email && user.email.toLowerCase().includes(searchQuery.toLowerCase())) ||
                (user.role && user.role.toLowerCase().includes(searchQuery.toLowerCase()))
            );

            if (filteredUsers && filteredUsers.length > 0) {
                filteredUsers.forEach(user => {
                    const userCard = document.createElement('div');
                    userCard.className = 'data-card';
                    userCard.innerHTML = `
                        <h4>${user.nomeCompleto} (${user.role})</h4> <p>Email: ${user.email}</p>
                        <p>Telefone: ${user.telefone || 'N/A'}</p>
                        <p>Endereço: ${user.endereco || 'N/A'}</p>
                        ${user.role === 'VETERINARIO' ? `<p>CRMV: ${user.crmv || 'N/A'}</p><p>Especialidade: ${user.especialidade || 'N/A'}</p>` : ''}
                        ${user.role === 'CLIENTE' ? `<p>CPF: ${user.cpf || 'N/A'}</p>` : ''}
                        <button class="btn btn-secondary edit-user-admin-btn" data-id="${user.id}">Editar</button>
                        <button class="btn btn-danger delete-user-admin-btn" data-id="${user.id}" data-role="${user.role}">Excluir</button>
                    `;
                    userListAdminDiv.appendChild(userCard);
                });
            } else {
                userListAdminDiv.innerHTML = '<p>Nenhum usuário cadastrado.</p>';
            }
        } catch (error) {
            console.error('Erro ao carregar todos os usuários:', error);
            displayMessage(userListAdminDiv, error.message || 'Erro ao carregar usuários.', 'error');
        }
    }

    if (addOtherUserBtn) {
        addOtherUserBtn.addEventListener('click', () => {
            adminUserModalTitle.textContent = 'Cadastrar Novo Usuário';
            adminUserSubmitBtn.textContent = 'Cadastrar Usuário';
            adminUserIdHidden.value = '';
            adminUserForm.reset();
            adminUserRoleSelect.value = 'cliente'; // Default para 'cliente' ao abrir
            adminUserRoleSelect.disabled = false;
            adminUserSenhaInput.required = true;
            displayMessage(adminUserMessageDiv, '', '');
            toggleRoleSpecificFields(); // Chama para inicializar a visibilidade dos campos
            showModal(adminUserModal);
        });
    }

    userListAdminDiv.addEventListener('click', async (e) => {
        if (e.target.classList.contains('edit-user-admin-btn')) {
            const userId = e.target.dataset.id;
            try {
                const user = await api.users.getById(userId);
                adminUserModalTitle.textContent = 'Editar Usuário';
                adminUserSubmitBtn.textContent = 'Salvar Alterações';
                adminUserIdHidden.value = user.id;
                adminUserNameInput.value = user.nomeCompleto; // CORRIGIDO: Usar nomeCompleto
                adminUserEmailInput.value = user.email;
                adminUserTelefoneInput.value = user.telefone || '';
                adminUserEnderecoInput.value = user.endereco || '';
                if (adminUserCpfInput) adminUserCpfInput.value = user.cpf || '';
                if (adminUserCrmvInput) adminUserCrmvInput.value = user.crmv || '';
                if (adminUserEspecialidadeInput) adminUserEspecialidadeInput.value = user.especialidade || '';

                adminUserRoleSelect.value = user.role.toLowerCase();
                adminUserRoleSelect.disabled = false;
                adminUserSenhaInput.value = '';
                adminUserSenhaInput.required = false;
                displayMessage(adminUserMessageDiv, '', '');
                toggleRoleSpecificFields(); // Chama para ajustar campos ao editar
                showModal(adminUserModal);
            } catch (error) {
                console.error('Erro ao buscar usuário para edição:', error);
                displayMessage(adminUserMessageDiv, error.message || 'Erro ao carregar dados do usuário.', 'error');
            }
        } else if (e.target.classList.contains('delete-user-admin-btn')) {
            const userIdToDelete = e.target.dataset.id;
            const userRoleToDelete = e.target.dataset.role;
            const currentLoggedInUserId = localStorage.getItem('userId');

            if (userIdToDelete === currentLoggedInUserId) {
                alert('Você não pode excluir a si mesmo.');
                return;
            }

            if (confirm(`Tem certeza que deseja excluir o usuário ${userRoleToDelete}?`)) {
                try {
                    await api.users.remove(userIdToDelete);
                    displayMessage(userListAdminDiv, 'Usuário excluído com sucesso!', 'success');
                    await loadAllUsers();
                } catch (error) {
                    console.error('Erro ao excluir usuário:', error);
                    displayMessage(userListAdminDiv, error.message || 'Erro ao excluir usuário.', 'error');
                }
            }
        }
    });

    if (adminUserForm) {
        adminUserForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const id = adminUserIdHidden.value;
            const roleSelected = adminUserRoleSelect.value;

            // Cria um objeto base com campos comuns
            const userData = {
                nomeCompleto: adminUserNameInput.value, // CORRIGIDO: nomeCompleto para bater com DTO
                email: adminUserEmailInput.value,
                role: roleSelected.toUpperCase(), // Sempre envia a role em MAIÚSCULAS para o backend
            };

            // Adiciona a senha se o campo não estiver vazio
            if (adminUserSenhaInput.value) {
                userData.senha = adminUserSenhaInput.value;
            }

            // Adiciona campos opcionais, garantindo que são strings vazias se não preenchidos
            if (adminUserTelefoneInput) userData.telefone = adminUserTelefoneInput.value || '';
            if (adminUserEnderecoInput) userData.endereco = adminUserEnderecoInput.value || '';
            if (adminUserCpfInput) userData.cpf = adminUserCpfInput.value || ''; // Para CLIENTE
            if (adminUserCrmvInput) userData.crmv = adminUserCrmvInput.value || ''; // Para VETERINARIO
            if (adminUserEspecialidadeInput) userData.especialidade = adminUserEspecialidadeInput.value || ''; // Para VETERINARIO

            // Log para debug
            console.log("Dados enviados para o backend:", userData);

            try {
                let response;
                if (id) {
                    // Para PUT, certifique-se de que o UsuarioService no backend também aceita UsuarioDTO ou um DTO de atualização sem senha obrigatória.
                    // Se o seu `updateUsuario` no `UsuarioService` espera um `UsuarioDTO` simples,
                    // certifique-se de que ele pode lidar com campos nulos/vazios ou que
                    // o DTO de atualização não tem `@NotBlank` em campos opcionais.
                    response = await api.users.update(id, userData); // Usando api.users.update
                    displayMessage(adminUserMessageDiv, 'Usuário atualizado com sucesso!', 'success');
                } else {
                    response = await api.auth.registerUserWithRole(userData);
                    displayMessage(adminUserMessageDiv, 'Usuário cadastrado com sucesso!', 'success');
                }

                adminUserForm.reset();
                // Oculta os campos específicos após o reset e antes de fechar o modal
                toggleRoleSpecificFields();
                await loadAllUsers();
                setTimeout(() => hideModal(adminUserModal), 1000);
            } catch (error) {
                console.error('Erro ao salvar usuário:', error);
                displayMessage(adminUserMessageDiv, error.message || 'Erro ao salvar usuário.', 'error');
            }
        });
    }

    if (userSearchAdminInput) {
        userSearchAdminInput.addEventListener('input', (e) => {
            loadAllUsers(e.target.value);
        });
    }

    loadAllUsers(); // Carrega usuários na inicialização
});
