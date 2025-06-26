// js/agendarConsulta.js
import { api } from './api.js';
import { displayMessage } from './common.js';

document.addEventListener('DOMContentLoaded', async () => {
    const userId = localStorage.getItem('userId');
    const scheduleAppointmentForm = document.getElementById('scheduleAppointmentForm');
    const petSelect = document.getElementById('petSelect');
    const veterinarioSelect = document.getElementById('veterinarioSelect'); // <-- NOVO: Select para Veterinários
    const scheduleMessage = document.getElementById('scheduleMessage');

    async function loadClientPetsForSelect() {
        if (!userId) {
            displayMessage(scheduleMessage, 'ID do usuário não encontrado para carregar pets.', 'error');
            return;
        }
        try {
            // Supondo que api.pets.getByOwnerId agora espera o ID do cliente
            const pets = await api.pets.getByOwnerId(userId);
            petSelect.innerHTML = '<option value="">Selecione um Pet</option>';
            if (pets && pets.length > 0) {
                pets.forEach(pet => {
                    const option = document.createElement('option');
                    option.value = pet.id;
                    option.textContent = pet.nome;
                    petSelect.appendChild(option);
                });
            } else {
                petSelect.innerHTML = '<option value="">Nenhum pet cadastrado.</option>';
                displayMessage(scheduleMessage, 'Por favor, cadastre um pet antes de agendar uma consulta.', 'info');
            }
        } catch (error) {
            console.error('Erro ao carregar pets para select:', error);
            displayMessage(scheduleMessage, error.message || 'Erro ao carregar seus pets para agendamento.', 'error');
        }
    }

    async function loadVeterinariosForSelect() {
        try {
            // Supondo que api.users.getVeterinarios retorna uma lista de VeterinarioDTO/UsuarioDTO com ID e nome
            const veterinarios = await api.users.getVeterinarios();
            veterinarioSelect.innerHTML = '<option value="">Selecione um Veterinário</option>';
            if (veterinarios && veterinarios.length > 0) {
                veterinarios.forEach(vet => {
                    const option = document.createElement('option');
                    option.value = vet.id;
                    option.textContent = vet.nomeCompleto; // Ou vet.nome, dependendo do DTO de retorno
                    veterinarioSelect.appendChild(option);
                });
            } else {
                veterinarioSelect.innerHTML = '<option value="">Nenhum veterinário disponível.</option>';
                displayMessage(scheduleMessage, 'Nenhum veterinário disponível para agendamento.', 'info');
            }
        } catch (error) {
            console.error('Erro ao carregar veterinários para select:', error);
            displayMessage(scheduleMessage, error.message || 'Erro ao carregar veterinários.', 'error');
        }
    }

    if (scheduleAppointmentForm) {
        scheduleAppointmentForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const petId = petSelect.value;
            const veterinarioId = veterinarioSelect.value; // <-- NOVO: Pega o ID do veterinário
            const dataConsulta = document.getElementById('dataConsulta').value;
            const horaConsulta = document.getElementById('horaConsulta').value;
            const observacoes = document.getElementById('motivoConsulta').value; // <-- MUDANÇA: 'motivo' agora é 'observacoes'

            if (!petId || !veterinarioId || !dataConsulta || !horaConsulta) {
                displayMessage(scheduleMessage, 'Por favor, preencha todos os campos obrigatórios (Pet, Veterinário, Data e Hora).', 'error');
                return;
            }

            const dataHora = new Date(`${dataConsulta}T${horaConsulta}`);

            try {
                const newConsulta = await api.consultas.create({
                    petId: parseInt(petId), // Converte para número inteiro
                    clienteId: parseInt(userId), // Converte para número inteiro e usa 'clienteId'
                    veterinarioId: parseInt(veterinarioId), // Converte para número inteiro e envia
                    dataHora: dataHora.toISOString(), // Envia em formato ISO para o backend
                    observacoes: observacoes, // <-- MUDANÇA: Envia como 'observacoes'
                    status: 'AGENDADA' // <-- MUDANÇA: Envia em UPPERCASE para corresponder ao Enum
                });
                displayMessage(scheduleMessage, 'Consulta agendada com sucesso!', 'success');
                scheduleAppointmentForm.reset();
                setTimeout(() => {
                    window.location.href = 'minhas-consultas.html';
                }, 1000);
            } catch (error) {
                console.error('Erro ao agendar consulta:', error);
                // Tenta exibir a mensagem de erro do backend se disponível
                displayMessage(scheduleMessage, error.message || 'Erro ao agendar consulta.', 'error');
            }
        });
    }

    if (userId) {
        loadClientPetsForSelect();
        loadVeterinariosForSelect(); // <-- NOVO: Carrega veterinários ao carregar a página
    }
});