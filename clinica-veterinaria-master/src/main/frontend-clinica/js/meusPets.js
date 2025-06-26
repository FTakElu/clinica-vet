// js/meusPets.js
import { api } from './api.js';
import { displayMessage } from './common.js';

document.addEventListener('DOMContentLoaded', async () => {
    const userId = localStorage.getItem('userId');
    const petListDiv = document.getElementById('petList');

    async function loadMyPets() {
        if (!userId) {
            displayMessage(petListDiv, 'ID do usuário não encontrado para carregar pets.', 'error');
            return;
        }
        try {
            const pets = await api.pets.getByOwnerId(userId);
            petListDiv.innerHTML = ''; // Limpa a lista
            if (pets && pets.length > 0) {
                pets.forEach(pet => {
                    const petCard = document.createElement('div');
                    petCard.className = 'data-card';
                    petCard.innerHTML = `
                        <h4>${pet.nome}</h4>
                        <p>Espécie: ${pet.especie}</p>
                        <p>Raça: ${pet.raca || 'Não informada'}</p>
                        <p>Nascimento: ${pet.dataNascimento ? new Date(pet.dataNascimento).toLocaleDateString() : 'Não informado'}</p>
                        <a href="detalhe-pet.html?petId=${pet.id}" class="btn btn-info">Ver Detalhes e Vacinas</a>
                    `;
                    petListDiv.appendChild(petCard);
                });
            } else {
                petListDiv.innerHTML = '<p>Você ainda não tem pets cadastrados.</p>';
            }
        } catch (error) {
            console.error('Erro ao carregar pets:', error);
            displayMessage(petListDiv, error.message || 'Erro ao carregar seus pets.', 'error');
        }
    }

    if (userId) {
        loadMyPets();
    }
});