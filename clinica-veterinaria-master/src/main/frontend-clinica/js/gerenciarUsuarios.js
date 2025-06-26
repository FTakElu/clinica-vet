// js/gerenciarUsuarios.js
import { api } from './api.js';
import { showModal, hideModal, displayMessage } from './common.js'; // Assumindo que common.js tem essas funções

document.addEventListener('DOMContentLoaded', async () => {
    console.log('gerenciarUsuarios.js carregado.');

    // Elementos da Página Principal
    const addGenericUserBtn = document.getElementById('addGenericUserBtn'); // Botão "Cadastrar Novo Usuário"
    const userSearchInput = document.getElementById('userSearch'); // Campo de busca
    const userListDiv = document.getElementById('userList'); // Div onde a lista de usuários é exibida

    // Elementos do Modal de Usuário
    const userModal = document.getElementById('userModal');
    const closeButton = userModal.querySelector('.close-button');
    const userModalTitle = document.getElementById('userModalTitle');
    const userForm = document.getElementById('userForm');
    const userIdHidden = document.getElementById('userIdHidden');
    const userNameInput = document.getElementById('userName'); // Corresponde ao nomeCompleto no DTO
    const userEmailInput = document.getElementById('userEmail');
    const userSenhaInput = document.getElementById('userSenha');
    const userRoleSelect = document.getElementById('userRole'); // Select para tipo de usuário

    // Referências aos elementos dos campos específicos de role, incluindo seus `.form-group`s
    const formGroupTelefone = document.getElementById('formGroupTelefone');
    const userTelefoneInput = document.getElementById('userTelefone');

    const formGroupCpf = document.getElementById('formGroupCpf');
    const userCpfInput = document.getElementById('userCpf');

    const formGroupCrmv = document.getElementById('formGroupCrmv');
    const userCrmvInput = document.getElementById('userCrmv');

    const formGroupEspecialidade = document.getElementById('formGroupEspecialidade');
    const userEspecialidadeInput = document.getElementById('userEspecialidade');

    const formGroupEndereco = document.getElementById('formGroupEndereco');
    const userEnderecoInput = document.getElementById('userEndereco');

    const userSubmitBtn = userForm.querySelector('button[type="submit"]');
    const userMessageDiv = document.getElementById('userMessage'); // Div para mensagens no modal

    // Função para mostrar/esconder campos baseados na role selecionada no modal
    function toggleRoleSpecificFields() {
        const selectedRole = userRoleSelect.value; // 'cliente', 'veterinario', 'secretario', 'admin'

        // Oculta todos os campos específicos primeiro e limpa seus valores
        const allFormGroups = [
            formGroupTelefone, formGroupCpf, formGroupCrmv,
            formGroupEspecialidade, formGroupEndereco
        ];
        const allInputs = [
            userTelefoneInput, userCpfInput, userCrmvInput,
            userEspecialidadeInput, userEnderecoInput
        ];

        allFormGroups.forEach(group => {
            if (group) { // Verifica se o elemento existe
                group.style.display = 'none';
            }
        });
        allInputs.forEach(input => {
            if (input) { // Verifica se o elemento existe
                input.value = ''; // Limpa o valor para evitar envio de dados de outra role
            }
        });

        // Mostra os campos relevantes
        switch (selectedRole) {
            case 'cliente':
                if (formGroupTelefone) formGroupTelefone.style.display = 'block';
                if (formGroupEndereco) formGroupEndereco.style.display = 'block';
                if (formGroupCpf) formGroupCpf.style.display = 'block';
                break;
            case 'veterinario':
                if (formGroupTelefone) formGroupTelefone.style.display = 'block';
                if (formGroupEndereco) formGroupEndereco.style.display = 'block';
                if (formGroupCrmv) formGroupCrmv.style.display = 'block';
                if (formGroupEspecialidade) formGroupEspecialidade.style.display = 'block';
                break;
            case 'secretario':
            case 'admin':
                if (formGroupTelefone) formGroupTelefone.style.display = 'block'; // Admins/Secretários podem ter telefone
                // Não adicionamos campos extras por padrão para admin/secretário aqui
                break;
        }
    }

    // Adicionar listener para o select da role (no caso de edição, a role pode mudar o formulário)
    if (userRoleSelect) {
        userRoleSelect.addEventListener('change', toggleRoleSpecificFields);
    }

    // --- Funções de Carregamento de Dados ---
    async function loadUsers(searchQuery = '') {
        userListDiv.innerHTML = '<p class="text-gray-600 text-lg text-center">Carregando usuários...</p>';
        try {
            console.log('Tentando carregar usuários...');
            // Assumimos que /api/usuarios lista todos os usuários e o token do ADMIN/SECRETARIO tem permissão
            const users = await api.users.getAll();
            userListDiv.innerHTML = ''; // Limpa a mensagem de carregamento

            console.log('Dados de usuários recebidos da API:', users);

            const filteredUsers = users.filter(user => {
                const searchLower = searchQuery.toLowerCase();
                // A busca agora também considera o 'role'
                return (user.nomeCompleto && user.nomeCompleto.toLowerCase().includes(searchLower)) ||
                       (user.email && user.email.toLowerCase().includes(searchLower)) ||
                       (user.role && user.role.toLowerCase().includes(searchLower));
            });

            if (filteredUsers && filteredUsers.length > 0) {
                filteredUsers.forEach(user => {
                    const userCard = document.createElement('div');
                    userCard.className = 'bg-white p-4 rounded-lg shadow flex flex-col sm:flex-row justify-between items-start sm:items-center mb-4 border border-gray-200'; // Estilização Tailwind

                    let specificFieldsHtml = '';
                    if (user.role === 'VETERINARIO') {
                        specificFieldsHtml = `<p class="text-gray-600 text-sm mt-1">CRMV: <span class="font-medium">${user.crmv || 'N/A'}</span></p>
                                              <p class="text-gray-600 text-sm">Especialidade: <span class="font-medium">${user.especialidade || 'N/A'}</span></p>`;
                    } else if (user.role === 'CLIENTE') {
                        specificFieldsHtml = `<p class="text-gray-600 text-sm mt-1">CPF: <span class="font-medium">${user.cpf || 'N/A'}</span></p>`;
                    }

                    userCard.innerHTML = `
                        <div class="flex-grow mb-3 sm:mb-0">
                            <h4 class="text-xl font-semibold text-purple-700">${user.nomeCompleto}</h4>
                            <p class="text-gray-600">Email: <span class="font-medium">${user.email}</span></p>
                            <p class="text-gray-600">Perfil: <span class="font-bold text-purple-600">${user.role}</span></p>
                            <p class="text-gray-600 text-sm">Telefone: <span class="font-medium">${user.telefone || 'N/A'}</span></p>
                            <p class="text-gray-600 text-sm">Endereço: <span class="font-medium">${user.endereco || 'N/A'}</span></p>
                            ${specificFieldsHtml}
                        </div>
                        <div class="flex space-x-2">
                            <button class="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-full shadow-md transition-all duration-300 text-sm inline-flex items-center edit-user-btn" data-id="${user.id}">
                                <i class="fas fa-edit mr-1"></i> Editar
                            </button>
                            <button class="bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded-full shadow-md transition-all duration-300 text-sm inline-flex items-center delete-user-btn" data-id="${user.id}" data-role="${user.role}">
                                <i class="fas fa-trash-alt mr-1"></i> Excluir
                            </button>
                        </div>
                    `;
                    userListDiv.appendChild(userCard);
                });
            } else {
                userListDiv.innerHTML = '<p class="text-gray-600 text-lg text-center">Nenhum usuário encontrado com os filtros aplicados.</p>';
            }
        } catch (error) {
            console.error('Erro ao carregar usuários:', error);
            const errorMessage = error.message.includes('Failed to fetch') || error.message.includes('NetworkError')
                               ? 'Erro de conexão com o servidor. Verifique sua rede e tente novamente.'
                               : error.message || 'Erro ao carregar usuários.';
            userListDiv.innerHTML = `<p class="text-red-600 text-lg text-center">${errorMessage}</p>`;
        }
    }

    // --- Lógica de CRUD para Usuários ---

    // Event listener para o botão "Cadastrar Novo Usuário"
    if (addGenericUserBtn) {
        addGenericUserBtn.addEventListener('click', () => {
            userModalTitle.textContent = 'Cadastrar Novo Usuário';
            userSubmitBtn.innerHTML = '<i class="fas fa-user-plus mr-2"></i> Cadastrar Usuário';
            userIdHidden.value = '';
            userForm.reset();
            userRoleSelect.value = 'cliente'; // Define 'cliente' como valor inicial padrão
            userRoleSelect.disabled = false; // Permite escolher a role na criação
            userSenhaInput.required = true; // Senha é obrigatória na criação
            displayMessage(userMessageDiv, '', ''); // Limpa mensagens anteriores
            toggleRoleSpecificFields(); // Atualiza visibilidade dos campos com base na role padrão
            showModal(userModal);
        });
    }

    // Eventos de clique para editar/excluir usuários (delegação de eventos)
    userListDiv.addEventListener('click', async (e) => {
        if (e.target.classList.contains('edit-user-btn') || e.target.closest('.edit-user-btn')) {
            const button = e.target.closest('.edit-user-btn');
            const userId = button.dataset.id;
            try {
                const user = await api.users.getById(userId); // Busca o usuário pelo ID
                userModalTitle.textContent = 'Editar Usuário';
                userSubmitBtn.innerHTML = '<i class="fas fa-save mr-2"></i> Salvar Alterações';
                userIdHidden.value = user.id;
                userNameInput.value = user.nomeCompleto; // Usar nomeCompleto
                userEmailInput.value = user.email;

                // Preenche campos específicos se existirem dados
                userTelefoneInput.value = user.telefone || '';
                userEnderecoInput.value = user.endereco || '';
                userCpfInput.value = user.cpf || '';
                userCrmvInput.value = user.crmv || '';
                userEspecialidadeInput.value = user.especialidade || '';

                userRoleSelect.value = user.role.toLowerCase(); // Converte para minúsculas para o select
                userRoleSelect.disabled = true; // Não permite mudar a role de um usuário existente
                userSenhaInput.value = ''; // Limpa a senha para não preencher com hash
                userSenhaInput.required = false; // Senha opcional na edição
                displayMessage(userMessageDiv, '', '');
                toggleRoleSpecificFields(); // Ajusta campos visíveis ao editar
                showModal(userModal);
            } catch (error) {
                console.error('Erro ao buscar usuário para edição:', error);
                displayMessage(userMessageDiv, error.message || 'Erro ao carregar dados do usuário para edição.', 'error');
            }
        } else if (e.target.classList.contains('delete-user-btn') || e.target.closest('.delete-user-btn')) {
            const button = e.target.closest('.delete-user-btn');
            const userIdToDelete = button.dataset.id;
            const userRoleToDelete = button.dataset.role;

            if (confirm(`Tem certeza que deseja excluir o usuário ${userRoleToDelete} (ID: ${userIdToDelete})? Esta ação é irreversível!`)) {
                try {
                    // API de exclusão de usuário. Lembre-se que apenas ADMIN pode excluir.
                    await api.users.remove(userIdToDelete);
                    displayMessage(userListDiv, 'Usuário excluído com sucesso!', 'success');
                    await loadUsers(); // Recarregar lista após exclusão
                } catch (error) {
                    console.error('Erro ao excluir usuário:', error);
                    let errorMessage = error.message || 'Erro ao excluir usuário.';
                    if (error.status === 403) { // Exemplo de tratamento de status específico
                        errorMessage = 'Você não tem permissão para excluir usuários. Apenas Administradores podem realizar esta ação.';
                    }
                    displayMessage(userListDiv, errorMessage, 'error');
                }
            }
        }
    });

    // Submissão do Formulário de Usuário (Cadastro/Edição)
    if (userForm) {
        userForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const id = userIdHidden.value;
            const roleSelected = userRoleSelect.value; // 'cliente', 'veterinario', 'secretario', 'admin' (em minúsculas)

            // Objeto base com campos comuns. Use nomeCompleto para o backend.
            const userData = {
                nomeCompleto: userNameInput.value,
                email: userEmailInput.value,
            };

            // Adiciona senha se não estiver vazia (para criação ou alteração de senha na edição)
            if (userSenhaInput.value) {
                userData.senha = userSenhaInput.value;
            }

            // Adiciona campos específicos com base na role
            // Sempre inclua os campos, mesmo que vazios, para o DTO
            if (userTelefoneInput) userData.telefone = userTelefoneInput.value;
            if (userEnderecoInput) userData.endereco = userEnderecoInput.value;
            if (userCpfInput) userData.cpf = userCpfInput.value;
            if (userCrmvInput) userData.crmv = userCrmvInput.value;
            if (userEspecialidadeInput) userData.especialidade = userEspecialidadeInput.value;


            // Log para debug
            console.log("Dados enviados para o backend:", userData);

            try {
                let response;
                if (id) {
                    // Edição: Usa a rota PUT /api/usuarios/{id}
                    response = await api.users.update(id, userData);
                    displayMessage(userMessageDiv, 'Usuário atualizado com sucesso!', 'success');
                } else {
                    // Criação: Usa as rotas específicas de registro do AuthService,
                    // que já esperam o DTO completo para cada role
                    switch (roleSelected) {
                        case 'cliente':
                            response = await api.auth.registerClient(userData);
                            break;
                        case 'veterinario':
                            response = await api.auth.registerVeterinario(userData);
                            break;
                        case 'secretario':
                            response = await api.auth.registerSecretario(userData); // Assumindo esta função existe na api.auth
                            break;
                        case 'admin':
                            response = await api.auth.registerAdmin(userData); // Assumindo esta função existe na api.auth
                            break;
                        default:
                            throw new Error('Role de usuário inválida para cadastro.');
                    }
                    displayMessage(userMessageDiv, 'Usuário cadastrado com sucesso!', 'success');
                }

                userForm.reset();
                toggleRoleSpecificFields(); // Oculta os campos específicos após o reset do formulário
                await loadUsers(); // Recarrega a lista de usuários para mostrar as alterações
                setTimeout(() => hideModal(userModal), 1000); // Fecha o modal após um pequeno atraso
            } catch (error) {
                console.error('Erro ao salvar usuário:', error);
                const errorMessage = error.response?.data?.message || error.message || 'Erro ao salvar usuário.';
                displayMessage(userMessageDiv, errorMessage, 'error');
            }
        });
    }

    // Campo de busca
    if (userSearchInput) {
        let debounceTimeout;
        userSearchInput.addEventListener('input', (e) => {
            clearTimeout(debounceTimeout);
            debounceTimeout = setTimeout(() => {
                loadUsers(e.target.value);
            }, 300); // Pequeno atraso para não sobrecarregar a API a cada tecla
        });
    }

    // --- Carregar dados iniciais e configurar modal ---
    loadUsers(); // Carrega os usuários ao iniciar a página

    // Configura o estado inicial dos campos específicos quando o modal é carregado (e antes de ser mostrado pela primeira vez)
    toggleRoleSpecificFields();

    // Fechar o modal ao clicar no 'x'
    if (closeButton) {
        closeButton.addEventListener('click', () => {
            hideModal(userModal);
        });
    }

    // Fechar o modal ao clicar fora dele
    window.addEventListener('click', (event) => {
        if (event.target === userModal) {
            hideModal(userModal);
        }
    });
});