// js/script.js
// Nenhuma lógica específica complexa aqui por enquanto,
// mas pode ser usada para inicializar qualquer coisa que
// precise ser executada em todas as páginas, como
// listeners para menus de navegação responsivos, etc.

import { setupModalCloseButtons } from './common.js';

document.addEventListener('DOMContentLoaded', () => {
    // Inicializa a funcionalidade de fechar modais globalmente
    setupModalCloseButtons();

    // Outros scripts globais aqui, se houver
    console.log('Global script loaded.');
});