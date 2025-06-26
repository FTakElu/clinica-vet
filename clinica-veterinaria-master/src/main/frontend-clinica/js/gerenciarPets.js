// js/gerenciarPets.js
import { api } from './api.js';
import { showModal, hideModal, displayMessage } from './common.js';

document.addEventListener('DOMContentLoaded', async () => {
    console.log('gerenciarPets.js carregado.');

    // Elementos da UI
    const addPetBtn = document.getElementById('addPetBtn');
    const petListDiv = document.getElementById('petList');
    const petSearchInput = document.getElementById('petSearch');

    // Modal e Formulário
    const petModal = document.getElementById('petModal');
    const petModalTitle = document.getElementById('petModalTitle');
    const petForm = document.getElementById('petForm');
    const petIdHidden = document.getElementById('petIdHidden');
    const petNomeInput = document.getElementById('petNome');
    const petDataNascimentoInput = document.getElementById('petDataNascimento');
    const petEspecieSelect = document.getElementById('petEspecie'); // Referência ao novo SELECT de espécie
    const petRacaInput = document.getElementById('petRaca');
    const petPesoInput = document.getElementById('petPeso');
    const petOwnerSelect = document.getElementById('petOwnerSelect'); // Referência ao novo SELECT de dono

    const petSubmitBtn = petForm.querySelector('button[type="submit"]');
    const petMessageDiv = document.getElementById('petMessage');

    // --- Funções de Carregamento de Dados ---

    // Nova função para carregar clientes e popular o dropdown de donos
    async function loadClientsForDropdown() {
        try {
            console.log('Carregando clientes para o dropdown...');
            // A rota /usuarios/clientes precisa ter @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIO')")
            const clients = await api.users.getClients();
            petOwnerSelect.innerHTML = ''; // Limpa opções existentes

            const defaultOption = document.createElement('option');
            defaultOption.value = '';
            defaultOption.textContent = 'Selecione um Dono';
            defaultOption.disabled = true;
            defaultOption.selected = true;
            petOwnerSelect.appendChild(defaultOption);

            if (clients && clients.length > 0) {
                clients.forEach(client => {
                    const option = document.createElement('option');
                    option.value = client.id;
                    option.textContent = `${client.nomeCompleto} (ID: ${client.id})`; // Usa nomeCompleto do cliente
                    petOwnerSelect.appendChild(option);
                });
            } else {
                const noClientsOption = document.createElement('option');
                noClientsOption.value = '';
                noClientsOption.textContent = 'Nenhum cliente disponível';
                noClientsOption.disabled = true;
                petOwnerSelect.appendChild(noClientsOption);
            }
        } catch (error) {
            console.error('Erro ao carregar clientes para o dropdown:', error);
            displayMessage(petMessageDiv, 'Erro ao carregar lista de clientes.', 'error');
            petOwnerSelect.innerHTML = '<option value="">Erro ao carregar clientes</option>';
            petOwnerSelect.disabled = true;
        }
    }

    async function loadPets(searchQuery = '') {
        try {
            console.log('Tentando carregar pets...');
            const pets = await api.pets.getAll(); // Sua API deve permitir que o Secretário veja todos os pets
            petListDiv.innerHTML = '';

            const filteredPets = pets.filter(pet => {
                const matchesSearch =
                    (pet.nome && pet.nome.toLowerCase().includes(searchQuery.toLowerCase())) ||
                    (pet.especie && pet.especie.toLowerCase().includes(searchQuery.toLowerCase())) ||
                    (pet.raca && pet.raca.toLowerCase().includes(searchQuery.toLowerCase())) ||
                    (pet.donoNome && pet.donoNome.toLowerCase().includes(searchQuery.toLowerCase())); // CORRIGIDO: usa pet.donoNome
                return matchesSearch;
            });

            if (filteredPets && filteredPets.length > 0) {
                filteredPets.forEach(pet => {
                    const petCard = document.createElement('div');
                    petCard.className = 'data-card';
                    petCard.innerHTML = `
                        <h4>${pet.nome} (${pet.especie})</h4>
                        <p>Dono: ${pet.donoNome || 'N/A'}</p> <!-- CORRIGIDO: Exibe o nome do dono -->
                        <p>Data de Nascimento: ${new Date(pet.dataNascimento).toLocaleDateString()}</p>
                        <p>Raça: ${pet.raca || 'N/A'}</p>
                        <p>Peso: ${pet.peso || 'N/A'} kg</p>
                        <div class="pet-actions mt-3">
                            <button class="btn btn-secondary edit-pet-btn" data-id="${pet.id}">Editar</button>
                            <button class="btn btn-danger delete-pet-btn" data-id="${pet.id}">Excluir</button>
                        </div>
                    `;
                    petListDiv.appendChild(petCard);
                });
            } else {
                petListDiv.innerHTML = '<p>Nenhum pet encontrado.</p>';
            }
        } catch (error) {
            console.error('Erro ao carregar pets:', error);
            displayMessage(petListDiv, error.message || 'Erro ao carregar pets.', 'error');
        }
    }

    // --- Lógica de CRUD para Pets ---

    addPetBtn.addEventListener('click', async () => {
        petModalTitle.textContent = 'Adicionar Novo Pet';
        petSubmitBtn.textContent = 'Adicionar Pet';
        petIdHidden.value = '';
        petForm.reset();
        petOwnerSelect.value = ''; // Garante que o select de dono esteja na opção padrão
        petEspecieSelect.value = ''; // Garante que o select de espécie esteja na opção padrão
        displayMessage(petMessageDiv, '', ''); // Limpa mensagens anteriores
        await loadClientsForDropdown(); // Recarrega clientes sempre que o modal é aberto para adição
        showModal(petModal);
    });

    petListDiv.addEventListener('click', async (e) => {
        if (e.target.classList.contains('edit-pet-btn')) {
            const petId = e.target.dataset.id;
            try {
                const pet = await api.pets.getById(petId); // Busca o pet pelo ID
                petModalTitle.textContent = 'Editar Pet';
                petSubmitBtn.textContent = 'Salvar Alterações';
                petIdHidden.value = pet.id;
                petNomeInput.value = pet.nome;
                petDataNascimentoInput.value = pet.dataNascimento; // Assumindo formato YYYY-MM-DD

                // Pré-seleciona a espécie
                petEspecieSelect.value = pet.especie || '';

                petRacaInput.value = pet.raca || '';
                petPesoInput.value = pet.peso || '';

                // Carrega clientes e pré-seleciona o dono
                await loadClientsForDropdown();
                if (pet.donoId) { // CORRIGIDO: usa pet.donoId
                    petOwnerSelect.value = pet.donoId;
                } else {
                    petOwnerSelect.value = ''; // Limpa se não houver donoId
                }

                displayMessage(petMessageDiv, '', '');
                showModal(petModal);
            } catch (error) {
                console.error('Erro ao buscar pet para edição:', error);
                displayMessage(petMessageDiv, error.message || 'Erro ao carregar dados do pet para edição.', 'error');
            }
        } else if (e.target.classList.contains('delete-pet-btn')) {
            const petIdToDelete = e.target.dataset.id;
            if (confirm(`Tem certeza que deseja excluir o pet (ID: ${petIdToDelete})?`)) {
                try {
                    await api.pets.remove(petIdToDelete); // Sua API deve permitir a exclusão de pets pelo Secretário
                    displayMessage(petListDiv, 'Pet excluído com sucesso!', 'success');
                    await loadPets(); // Recarregar lista após exclusão
                } catch (error) {
                    console.error('Erro ao excluir pet:', error);
                    displayMessage(petListDiv, error.message || 'Erro ao excluir pet.', 'error');
                }
            }
        }
    });

    if (petForm) {
        petForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const id = petIdHidden.value;

            // Coleta dados do formulário, incluindo os novos selects
            const petData = {
                nome: petNomeInput.value,
                dataNascimento: petDataNascimentoInput.value,
                especie: petEspecieSelect.value, // Pega o valor selecionado (String)
                raca: petRacaInput.value,
                peso: parseFloat(petPesoInput.value),
                donoId: parseInt(petOwnerSelect.value) // CORRIGIDO: usa donoId para enviar
            };

            // Validação básica para o dono (cliente)
            if (!petData.donoId || isNaN(petData.donoId)) { // CORRIGIDO: usa donoId
                displayMessage(petMessageDiv, 'Por favor, selecione um dono válido.', 'error');
                return;
            }

            try {
                let response;
                if (id) {
                    response = await api.pets.update(id, petData); // Sua API deve ter PUT /api/pets/{id}
                    displayMessage(petMessageDiv, 'Pet atualizado com sucesso!', 'success');
                } else {
                    response = await api.pets.create(petData); // Sua API deve ter POST /api/pets
                    displayMessage(petMessageDiv, 'Pet cadastrado com sucesso!', 'success');
                }

                petForm.reset();
                await loadPets(); // Recarrega a lista de pets
                setTimeout(() => hideModal(petModal), 1000);
            } catch (error) {
                console.error('Erro ao salvar pet:', error);
                displayMessage(petMessageDiv, error.message || 'Erro ao salvar pet. Verifique o console.', 'error');
            }
        });
    }

    if (petSearchInput) {
        petSearchInput.addEventListener('input', (e) => {
            loadPets(e.target.value);
        });
    }

    // --- Carregar dados iniciais e configurar modal ---
    loadPets();
    loadClientsForDropdown(); // Carrega os clientes ao carregar a página

    // Fechar o modal ao clicar no 'x'
    const closeButton = petModal.querySelector('.close-button');
    if (closeButton) {
        closeButton.addEventListener('click', () => {
            hideModal(petModal);
        });
    }

    // Fechar o modal ao clicar fora dele
    window.addEventListener('click', (event) => {
        if (event.target == petModal) {
            hideModal(petModal);
        }
    });
});
