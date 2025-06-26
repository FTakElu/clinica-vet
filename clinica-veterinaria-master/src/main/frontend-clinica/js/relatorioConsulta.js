// js/veterinario.js
import { api } from './api.js';
import { requireAuth } from './auth.js';
import { showLoadingSpinner, hideLoadingSpinner, formatDate } from './common.js';

document.addEventListener('DOMContentLoaded', async () => {
    // Ensure the user is authenticated and has the 'VETERINARIO' role
    if (!requireAuth(['VETERINARIO'])) {
        return;
    }

    // Determine the current page to load the correct functionality
    const currentPage = window.location.pathname;

    if (currentPage.includes('veterinario-dashboard.html')) {
        await loadVeterinarioDashboard();
    } else if (currentPage.includes('registrar-vacina.html')) {
        await loadRegistrarVacinaPage();
    }
    // No specific logic for 'relatorio-consulta.html' here as it's for Secretary,
    // and its logic should be in a separate JS file (e.g., relatorioConsulta.js)
});

/**
 * Loads and displays data for the Veterinarian Dashboard.
 */
async function loadVeterinarioDashboard() {
    // Select the elements based on the updated HTML IDs
    const veterinarioNameDisplay = document.getElementById('veterinarioNameDisplay'); // Corrected ID from 'veterinarioNome'
    const consultaListDiv = document.getElementById('consultaList'); // Renamed from 'consultasDoDia'
    const consultaSearchInput = document.getElementById('consultaSearch'); // Added for search functionality

    showLoadingSpinner(); // Show loading indicator

    try {
        // Fetch veterinarian's name and display it
        const veterinarioInfo = await api.get('/veterinarios/me'); // Assuming /veterinarios/me endpoint exists
        if (veterinarioNameDisplay) {
            veterinarioNameDisplay.textContent = veterinarioInfo.nome;
            // Also update the welcome message in the header
            const welcomeMessage = document.getElementById('welcomeMessage');
            if (welcomeMessage) {
                welcomeMessage.textContent = `Bem-vindo(a), ${veterinarioInfo.nome}`;
            }
        }

        // Fetch today's appointments for the veterinarian
        const today = new Date().toISOString().split('T')[0]; // Format YYYY-MM-DD
        const consultationsToday = await api.get(`/consultas/veterinario/dia?data=${today}`); // Assuming endpoint

        // Render appointments in the 'consultaList' panel
        renderConsultationList(consultationsToday, consultaListDiv);

        // Add search functionality
        if (consultaSearchInput) {
            consultaSearchInput.addEventListener('input', () => {
                const searchTerm = consultaSearchInput.value.toLowerCase();
                const filteredConsultations = consultationsToday.filter(consulta =>
                    consulta.petNome.toLowerCase().includes(searchTerm) ||
                    consulta.clienteNome.toLowerCase().includes(searchTerm)
                );
                renderConsultationList(filteredConsultations, consultaListDiv);
            });
        }

        // Removed 'meusPacientesDiv' logic as it was removed from the HTML dashboard for simplicity.
        // If patient management is needed, it should be a separate page or a dedicated panel with a link.

    } catch (error) {
        console.error('Error loading veterinarian dashboard:', error);
        if (consultaListDiv) {
            consultaListDiv.innerHTML = '<p class="text-red-500">Erro ao carregar consultas. Tente novamente.</p>';
        }
    } finally {
        hideLoadingSpinner(); // Hide loading indicator
    }
}

/**
 * Helper function to render the list of consultations.
 * @param {Array} consultations - The array of consultation objects.
 * @param {HTMLElement} targetElement - The DOM element where consultations should be rendered.
 */
function renderConsultationList(consultations, targetElement) {
    if (!targetElement) return;

    targetElement.innerHTML = ''; // Clear previous content

    if (consultations.length === 0) {
        targetElement.innerHTML = '<p class="text-gray-600 text-lg">Nenhuma consulta agendada para hoje.</p>';
        return;
    }

    // Create a list (e.g., <ul>) to display consultations
    const ul = document.createElement('ul');
    ul.className = 'space-y-4'; // Tailwind spacing for list items

    consultations.forEach(consulta => {
        const li = document.createElement('li');
        li.className = 'bg-white p-4 rounded-lg shadow-sm border border-gray-200'; // Card-like styling for each consultation

        li.innerHTML = `
            <p class="text-lg font-semibold text-purple-700">Pet: ${consulta.petNome} (Dono: ${consulta.clienteNome})</p>
            <p class="text-gray-700">Hora: ${consulta.hora} - Status: <span class="font-medium ${consulta.status === 'AGENDADA' ? 'text-blue-600' : 'text-green-600'}">${consulta.status}</span></p>
            <p class="text-gray-600 text-sm mt-1">Descrição: ${consulta.descricao || 'N/A'}</p>
            <div class="mt-3 flex gap-2">
                <button data-consulta-id="${consulta.id}" class="btn-detail bg-blue-500 hover:bg-blue-600 text-white font-bold py-2 px-4 rounded-full text-sm transition-colors duration-200">Ver Detalhes</button>
                <button data-pet-id="${consulta.petId}" class="btn-view-vaccines bg-green-500 hover:bg-green-600 text-white font-bold py-2 px-4 rounded-full text-sm transition-colors duration-200">Ver Vacinas</button>
            </div>
        `;
        ul.appendChild(li);
    });
    targetElement.appendChild(ul);

    // Add event listeners for the new buttons
    targetElement.querySelectorAll('.btn-detail').forEach(button => {
        button.addEventListener('click', (event) => {
            const consultaId = event.target.dataset.consultaId;
            // Redirect to a consultation detail/edit page
            window.location.href = `detalhe-consulta.html?id=${consultaId}`;
        });
    });

    targetElement.querySelectorAll('.btn-view-vaccines').forEach(button => {
        button.addEventListener('click', (event) => {
            const petId = event.target.dataset.petId;
            // Redirect to a page to view pet vaccines (or show a modal if you re-introduce it elsewhere)
            window.location.href = `vacinas-pet.html?petId=${petId}`; // Example page
        });
    });
}

/**
 * Loads data and sets up form submission for the Register Vaccine page.
 */
async function loadRegistrarVacinaPage() {
    const registerVaccineForm = document.getElementById('registerVaccineForm');
    // Corrected IDs based on your HTML
    const regVacinaPetSelect = document.getElementById('regVacinaPet');
    const regVacinaTipoSelect = document.getElementById('regVacinaTipo');
    const regVacinaDataInput = document.getElementById('regVacinaData'); // Renamed for consistency
    const regVacinaVeterinarioSelect = document.getElementById('regVacinaVeterinario'); // Added

    if (!registerVaccineForm) return;

    showLoadingSpinner(); // Show loading indicator

    try {
        // Fetch pets
        const pets = await api.get('/pets'); // Assuming /pets endpoint
        regVacinaPetSelect.innerHTML = '<option value="">Selecione um Pet</option>';
        pets.forEach(pet => {
            const option = document.createElement('option');
            option.value = pet.id;
            // Assuming 'clienteNome' is available on the pet object for clarity
            option.textContent = `${pet.nome} (Dono: ${pet.clienteNome || 'N/A'})`;
            regVacinaPetSelect.appendChild(option);
        });

        // Fetch vaccine types
        const vaccineTypes = await api.get('/tipos-vacina'); // Assuming /tipos-vacina endpoint
        regVacinaTipoSelect.innerHTML = '<option value="">Selecione o Tipo da Vacina</option>';
        vaccineTypes.forEach(type => {
            const option = document.createElement('option');
            option.value = type.id;
            option.textContent = type.nome;
            regVacinaTipoSelect.appendChild(option);
        });

        // Fetch veterinarians (for the optional selection)
        const veterinarians = await api.get('/veterinarios'); // Assuming /veterinarios endpoint for all vets
        regVacinaVeterinarioSelect.innerHTML = '<option value="">Selecione um Veterinário (Opcional)</option>';
        let loggedInVeterinarioId = null;

        // Get current logged-in veterinarian's ID
        try {
            const currentVet = await api.get('/veterinarios/me');
            loggedInVeterinarioId = currentVet.id;
            // Automatically select if the current user is a veterinarian
            const selfOption = document.createElement('option');
            selfOption.value = currentVet.id;
            selfOption.textContent = `${currentVet.nome} (Você)`;
            regVacinaVeterinarioSelect.appendChild(selfOption);
            regVacinaVeterinarioSelect.value = currentVet.id; // Pre-select current vet
        } catch (error) {
            console.warn('Could not auto-select logged-in veterinarian or user is not a veterinarian:', error);
            // If the user isn't a veterinarian or 'veterinarios/me' fails, let them choose from the list
        }

        veterinarians.forEach(vet => {
            if (vet.id !== loggedInVeterinarioId) { // Avoid duplicating the logged-in vet if already added
                const option = document.createElement('option');
                option.value = vet.id;
                option.textContent = vet.nome;
                regVacinaVeterinarioSelect.appendChild(option);
            }
        });

    } catch (error) {
        console.error('Error loading data for vaccine registration:', error);
        // Display user-friendly error messages in selects
        regVacinaPetSelect.innerHTML = '<option value="">Erro ao carregar pets</option>';
        regVacinaTipoSelect.innerHTML = '<option value="">Erro ao carregar tipos de vacina</option>';
        regVacinaVeterinarioSelect.innerHTML = '<option value="">Erro ao carregar veterinários</option>';
    } finally {
        hideLoadingSpinner(); // Hide loading indicator
    }

    // Handle form submission for vaccine registration
    registerVaccineForm.addEventListener('submit', async (e) => {
        e.preventDefault(); // Prevent default form submission
        showLoadingSpinner(); // Show loading indicator for submission

        // Collect data from the form
        const vaccineData = {
            petId: regVacinaPetSelect.value,
            tipoVacinaId: regVacinaTipoSelect.value,
            dataAplicacao: regVacinaDataInput.value,
            // 'fabricante' field was not in your HTML, removed from here unless added back
            veterinarioId: regVacinaVeterinarioSelect.value // Use the selected veterinarian
        };

        try {
            // Send POST request to register the vaccine
            await api.post('/vacinas', vaccineData); // Assuming /vacinas endpoint
            alert('Vacina registrada com sucesso!'); // Success message
            registerVaccineForm.reset(); // Clear the form
            // Optional: Reload data or redirect after successful registration
        } catch (error) {
            const errorMessageDiv = document.getElementById('regVacinaMessage');
            if (errorMessageDiv) {
                errorMessageDiv.textContent = 'Erro ao registrar vacina: ' + (error.message || 'Verifique os dados e tente novamente.');
                errorMessageDiv.classList.add('text-red-600', 'font-medium', 'text-base'); // Apply styling
            }
            console.error('Vaccine registration error:', error);
        } finally {
            hideLoadingSpinner(); // Hide loading indicator
        }
    });
}