// js/common.js
export function showModal(modalElement) {
    if (modalElement) {
        modalElement.style.display = 'flex'; // Usar 'flex' para centralização com Tailwind
    }
}

export function hideModal(modalElement) {
    if (modalElement) {
        modalElement.style.display = 'none';
    }
}

// Configura ouvintes para todos os botões de fechar modais e cliques fora dos modais
export function setupModalCloseButtons() {
    document.querySelectorAll('.close-button').forEach(button => {
        // Remove listeners antigos para evitar duplicação se a função for chamada múltiplas vezes
        const oldListener = button.dataset.listener; // Armazena a referência do listener
        if (oldListener) {
            button.removeEventListener('click', eval(oldListener)); // Remove o listener anterior
        }
        const newListener = (event) => {
            hideModal(event.target.closest('.modal'));
            // Opcional: Resetar formulários ao fechar
            const form = event.target.closest('.modal-content')?.querySelector('form');
            if (form) form.reset();
            const messageDiv = event.target.closest('.modal-content')?.querySelector('.message');
            if (messageDiv) displayMessage(messageDiv, '', ''); // Limpa mensagem com a função displayMessage
        };
        button.addEventListener('click', newListener);
        button.dataset.listener = newListener.name || 'newListener'; // Armazena uma referência para remoção futura
    });

    // Remove listener antigo do window para evitar duplicação
    const oldWindowListener = window.onclick; // Armazena a referência do listener
    if (oldWindowListener && oldWindowListener.name === 'handleWindowClick') {
        window.removeEventListener('click', oldWindowListener);
    }

    const handleWindowClick = function(event) {
        if (event.target.classList.contains('modal')) {
            hideModal(event.target);
            const form = event.target.querySelector('.modal-content')?.querySelector('form');
            if (form) form.reset();
            const messageDiv = event.target.querySelector('.modal-content')?.querySelector('.message');
            if (messageDiv) displayMessage(messageDiv, '', ''); // Limpa mensagem
        }
    };
    window.addEventListener('click', handleWindowClick);
    window.onclick = handleWindowClick; // Para remoção futura

    // Opcional: Fechar modais com a tecla ESC
    document.addEventListener('keydown', (event) => {
        if (event.key === 'Escape') {
            document.querySelectorAll('.modal').forEach(modal => {
                if (modal.style.display === 'flex') { // Apenas se o modal estiver visível
                    hideModal(modal);
                    const form = modal.querySelector('.modal-content')?.querySelector('form');
                    if (form) form.reset();
                    const messageDiv = modal.querySelector('.modal-content')?.querySelector('.message');
                    if (messageDiv) displayMessage(messageDiv, '', '');
                }
            });
        }
    });
}

export function formatDateForInput(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    // Adiciona 0 à esquerda se o mês ou dia for menor que 10
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

export function formatTimeForInput(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    // Adiciona 0 à esquerda se a hora ou minuto for menor que 10
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${hours}:${minutes}`;
}

// Melhoria na função displayMessage para usar classes Tailwind
export function displayMessage(element, message, type) {
    if (element) {
        element.textContent = message;
        let classList = ['message', 'text-center', 'mt-4', 'font-medium', 'text-base']; // Classes base

        // Adiciona classes de cor baseadas no tipo
        if (type === 'success') {
            classList.push('text-green-600');
        } else if (type === 'error') {
            classList.push('text-red-600');
        } else {
            // Se nenhum tipo for especificado, limpa as classes de cor
            classList = classList.filter(cls => !cls.startsWith('text-'));
        }
        element.className = classList.join(' '); // Aplica todas as classes

        // Remove a mensagem após um tempo, resetando as classes
        if (message) { // Apenas se houver uma mensagem para exibir
            setTimeout(() => {
                element.textContent = '';
                element.className = 'message'; // Reseta para a classe base
            }, 3000); // Mensagem desaparece após 3 segundos
        }
    }
}

// Para usar em arquivos JS que importam common.js
document.addEventListener('DOMContentLoaded', setupModalCloseButtons);