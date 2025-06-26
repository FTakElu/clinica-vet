// js/detalhePet.js
import { api } from './api.js';
import { displayMessage } from './common.js';

document.addEventListener('DOMContentLoaded', async () => {
    const urlParams = new URLSearchParams(window.location.search);
    const petId = urlParams.get('petId');

    const petNameDisplay = document.getElementById('petNameDisplay');
    const petNomeSpan = document.getElementById('petNome');
    const petEspecieSpan = document.getElementById('petEspecie');
    const petRacaSpan = document.getElementById('petRaca');
    const petDataNascimentoSpan = document.getElementById('petDataNascimento');
    const petSexoSpan = document.getElementById('petSexo');
    const petDonoSpan = document.getElementById('petDono');
    const vacinaListDiv = document.getElementById('vacinaList');

    async function loadPetDetails() {
        if (!petId) {
            displayMessage(document.querySelector('.container'), 'ID do pet não fornecido na URL.', 'error');
            return;
        }

        try {
            const pet = await api.pets.getById(petId);
            if (pet) {
                petNameDisplay.textContent = pet.nome;
                petNomeSpan.textContent = pet.nome;
                petEspecieSpan.textContent = pet.especie;
                petRacaSpan.textContent = pet.raca || 'Não informada';
                petDataNascimentoSpan.textContent = pet.dataNascimento ? new Date(pet.dataNascimento).toLocaleDateString() : 'Não informado';
                petSexoSpan.textContent = pet.sexo;
                petDonoSpan.textContent = pet.dono?.nome || 'N/A';
            } else {
                displayMessage(document.querySelector('.container'), 'Pet não encontrado.', 'error');
                return;
            }

            const vacinas = await api.vacinasAplicadas.getByPetId(petId);
            vacinaListDiv.innerHTML = '';
            if (vacinas && vacinas.length > 0) {
                vacinas.forEach(vacina => {
                    const vacinaCard = document.createElement('div');
                    vacinaCard.className = 'data-card';
                    vacinaCard.innerHTML = `
                        <h4>${vacina.tipoVacina?.nome || 'Vacina Desconhecida'}</h4>
                        <p>Data de Aplicação: ${new Date(vacina.dataAplicacao).toLocaleDateString()}</p>
                        <p>Aplicada por: ${vacina.veterinario?.nome || 'N/A'}</p>
                    `;
                    vacinaListDiv.appendChild(vacinaCard);
                });
            } else {
                vacinaListDiv.innerHTML = '<p>Nenhuma vacina registrada para este pet.</p>';
            }

        } catch (error) {
            console.error('Erro ao carregar detalhes do pet ou vacinas:', error);
            displayMessage(document.querySelector('.container'), error.message || 'Erro ao carregar detalhes do pet.', 'error');
        }
    }

    loadPetDetails();
});