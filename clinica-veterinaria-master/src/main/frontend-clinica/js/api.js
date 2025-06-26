// js/api.js
const API_BASE_URL = 'http://localhost:8080';
export async function fetchWithAuth(url, options = {}) {
    const token = localStorage.getItem('token');
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers,
    };

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}${url}`, {
        ...options,
        headers,
    });

    // Tratar erros de autenticação/autorização globalmente
    if (response.status === 401 || response.status === 403) {
        console.error('Erro de autenticação ou autorização. Redirecionando para o login.');
        localStorage.clear();
        window.location.href = 'login.html';
        throw new Error('Unauthorized or Forbidden');
    }

    // Tenta parsear JSON, mas permite respostas vazias (ex: DELETE pode não ter corpo)
    const data = response.headers.get('content-type')?.includes('application/json')
        ? await response.json()
        : null;

    if (!response.ok) {
        if (data && data.message) {
            throw new Error(data.message);
        } else {
            throw new Error(`Erro na requisição: ${response.status} ${response.statusText}`);
        }
    }

    return data;
}

// Funções de API
export const api = {
    auth: {
        login: (email, senha) => fetchWithAuth('/auth/login', {
            method: 'POST',
            body: JSON.stringify({ email, senha }),
            headers: { 'Content-Type': 'application/json' }
        }),
        registerClient: (userData) => fetchWithAuth('/auth/register/cliente', {
            method: 'POST',
            body: JSON.stringify(userData)
        }),
        // registerUserWithRole é a função genérica para roles que não têm um endpoint específico de registro
        registerUserWithRole: (userData) => fetchWithAuth('/auth/register/user-with-role', {
            method: 'POST',
            body: JSON.stringify(userData)
        }),
        registerVeterinario: (userData) => fetchWithAuth('/auth/register/veterinario', {
            method: 'POST',
            body: JSON.stringify(userData)
        }),
        // Se houver endpoints específicos para Secretário/Admin, adicione aqui
        // Exemplo:
        // registerSecretario: (userData) => fetchWithAuth('/auth/register/secretario', {
        //     method: 'POST',
        //     body: JSON.stringify(userData)
        // }),
        // registerAdmin: (userData) => fetchWithAuth('/auth/register/admin', {
        //     method: 'POST',
        //     body: JSON.stringify(userData)
        // }),
    },
    users: {
        getAll: () => fetchWithAuth('/api/usuarios'),
        getById: (id) => fetchWithAuth(`/api/usuarios/${id}`),
        // create: (userData) => fetchWithAuth('/api/usuarios', { method: 'POST', body: JSON.stringify(userData) }), // Geralmente não usado se há 'register' por role
        update: (id, userData) => fetchWithAuth(`/api/usuarios/${id}`, { method: 'PUT', body: JSON.stringify(userData) }),
        remove: (id) => fetchWithAuth(`/api/usuarios/${id}`, { method: 'DELETE' }),
        getClients: () => fetchWithAuth('/api/usuarios/clientes'),
        getVeterinarios: () => fetchWithAuth('/api/usuarios/veterinarios'),
    },
    pets: {
        getAll: () => fetchWithAuth('/api/pets'),
        getById: (id) => fetchWithAuth(`/api/pets/${id}`),
        getByOwnerId: (ownerId) => fetchWithAuth(`/api/clientes/${ownerId}/pets`),
        create: (petData) => fetchWithAuth('/api/pets', { method: 'POST', body: JSON.stringify(petData) }),
        update: (id, petData) => fetchWithAuth(`/api/pets/${id}`, { method: 'PUT', body: JSON.stringify(petData) }),
        remove: (id) => fetchWithAuth(`/api/pets/${id}`, { method: 'DELETE' }),
    },
    consultas: {
        getAll: () => fetchWithAuth('/api/consultas'),
        getById: (id) => fetchWithAuth(`/api/consultas/${id}`),
        getByOwnerId: (ownerId) => fetchWithAuth(`/api/clientes/${ownerId}/consultas`),
        getByVeterinarioId: (vetId) => fetchWithAuth(`/api/veterinarios/${vetId}/consultas`),
        create: (consultaData) => fetchWithAuth('/api/consultas', { method: 'POST', body: JSON.stringify(consultaData) }),
        update: (id, consultaData) => fetchWithAuth(`/api/consultas/${id}`, { method: 'PUT', body: JSON.stringify(consultaData) }),
        remove: (id) => fetchWithAuth(`/api/consultas/${id}`, { method: 'DELETE' }),
    },
    tiposVacina: {
        getAll: () => fetchWithAuth('/api/tipos-vacina'),
        create: (data) => fetchWithAuth('/api/tipos-vacina', { method: 'POST', body: JSON.stringify(data) }),
        // Correção de template literal
        update: (id, data) => fetchWithAuth(`/api/tipos-vacina/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
        remove: (id) => fetchWithAuth(`/api/tipos-vacina/${id}`, { method: 'DELETE' }), // Correção de template literal
    },
    vacinasAplicadas: {
        // Correção de template literal
        getByPetId: (petId) => fetchWithAuth(`/api/pets/${petId}/vacinas-aplicadas`),
        create: (data) => fetchWithAuth('/api/vacinas-aplicadas', { method: 'POST', body: JSON.stringify(data) }),
    },
    relatoriosConsulta: {
        // Correção de template literal
        getByConsultaId: (consultaId) => fetchWithAuth(`/api/consultas/${consultaId}/relatorio`),
        create: (data) => fetchWithAuth('/api/relatorios-consulta', { method: 'POST', body: JSON.stringify(data) }),
        // Correção de template literal
        update: (consultaId, data) => fetchWithAuth(`/api/consultas/${consultaId}/relatorio`, { method: 'PUT', body: JSON.stringify(data) }),
    }
};