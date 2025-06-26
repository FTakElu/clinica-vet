// js/registrarVacina.js
import { api } from './api.js';
import { displayMessage } from './common.js';

document.addEventListener('DOMContentLoaded', async () => {
    const userId = localStorage.getItem('userId');
    const userRole = localStorage.getItem('userRole');

    const dashboardLink = document.getElementById('dashboardLink');
    if (dashboardLink) {
        if (userRole === 'secretario') {
            dashboardLink.href = 'secretario-dashboard.html';
        } else if (userRole === 'veterinario') {
            dashboardLink.href = 'veterinario-dashboard.html';
        }
    }

    const registerVaccineForm = document.getElementById('registerVaccineForm');
    const regVacinaPetSelect = document.getElementById('regVacinaPet');
    const regVacinaTipoSelect = document.getElementById('regVacinaTipo');
    const regVacinaDataInput = document.getElementById('regVacinaData');
    const regVacinaVeterinarioSelect = document.getElementById('regVacinaVeterinario'); // Campo para secretário
    const veterinarioSelectGroup = document.getElementById('veterinarioSelectGroup');
    const regVacinaMessageDiv = document.getElementById('regVacinaMessage');

    // Ocultar campo de seleção de veterinário para veterinários
    if (userRole === 'veterinario' && veterinarioSelectGroup) {
        veterinarioSelectGroup.style.display = 'none';
    }

    async function loadPetsForSelect() {
        try {
            const pets = await api.pets.getAll(); // Veterinário e Secretário podem registrar para qualquer pet
            regVacinaPetSelect.innerHTML = '<option value="">Selecione um Pet</option>';
            if (pets && pets.length > 0) {
                pets.forEach(pet => {
                    const option = document.createElement('option');
                    option.value = pet.id;
                    option.textContent = `${pet.nome} (Dono: ${pet.dono?.nome || 'N/A'})`;
                    regVacinaPetSelect.appendChild(option);
                });
            }
        } catch (error) {
            console.error('Erro ao carregar pets para registro de vacina:', error);
            displayMessage(regVacinaMessageDiv, 'Erro ao carregar lista de pets.', 'error');
        }
    }

    async function loadTiposVacinaForSelect() {
        try {
            const tipos = await api.tiposVacina.getAll();
            regVacinaTipoSelect.innerHTML = '<option value="">Selecione um Tipo de Vacina</option>';
            if (tipos && tipos.length > 0) {
                tipos.forEach(tipo => {
                    const option = document.createElement('option');
                    option.value = tipo.id;
                    option.textContent = tipo.nome;
                    regVacinaTipoSelect.appendChild(option);
                });
            }
        } catch (error) {
            console.error('Erro ao carregar tipos de vacina para registro:', error);
            displayMessage(regVacinaMessageDiv, 'Erro ao carregar tipos de vacina.', 'error');
        }
    }

    async function loadVeterinariosForSelect() {
        if (userRole === 'secretario') { // Apenas secretário precisa selecionar o veterinário
            try {
                const veterinarios = await api.users.getVeterinarios();
                regVacinaVeterinarioSelect.innerHTML = '<option value="">Selecione o Veterinário (Opcional)</option>';
                if (veterinarios && veterinarios.length > 0) {
                    veterinarios.forEach(vet => {
                        const option = document.createElement('option');
                        option.value = vet.id;
                        option.textContent = vet.nome;
                        regVacinaVeterinarioSelect.appendChild(option);
                    });
                }
            } catch (error) {
                console.error('Erro ao carregar veterinários para registro de vacina:', error);
                displayMessage(regVacinaMessageDiv, 'Erro ao carregar veterinários.', 'error');
            }
        }
    }

    if (registerVaccineForm) {
        registerVaccineForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const petId = regVacinaPetSelect.value;
            const tipoVacinaId = regVacinaTipoSelect.value;
            const dataAplicacao = regVacinaDataInput.value;
            let veterinarioId = userId; // Padrão: o próprio usuário logado (veterinário)

            if (userRole === 'secretario') {
                veterinarioId = regVacinaVeterinarioSelect.value || null; // Secretário pode deixar opcional
            }

            if (!petId || !tipoVacinaId || !dataAplicacao) {
                displayMessage(regVacinaMessageDiv, 'Por favor, preencha todos os campos obrigatórios.', 'error');
                return;
            }

            try {
                const response = await api.vacinasAplicadas.create({
                    petId,
                    tipoVacinaId,
                    dataAplicacao,
                    veterinarioId // Será o userId do vet ou o selecionado pelo secretário
                });
                displayMessage(regVacinaMessageDiv, response.message, 'success');
                registerVaccineForm.reset();
            } catch (error) {
                console.error('Erro ao registrar vacina:', error);
                displayMessage(regVacinaMessageDiv, error.message || 'Erro ao registrar vacina.', 'error');
            }
        });
    }

    if (userId) {
        loadPetsForSelect();
        loadTiposVacinaForSelect();
        loadVeterinariosForSelect(); // Carrega apenas se for secretário
    }
});