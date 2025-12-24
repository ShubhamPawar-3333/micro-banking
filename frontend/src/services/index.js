import api from './api';

export const authService = {
    register: async (data) => {
        const response = await api.post('/auth/register', data);
        return response.data;
    },

    login: async (data) => {
        const response = await api.post('/auth/login', data);
        const { accessToken, refreshToken } = response.data;
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('refreshToken', refreshToken);
        return response.data;
    },

    logout: () => {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
    },

    isAuthenticated: () => {
        return !!localStorage.getItem('accessToken');
    },
};

export const accountService = {
    getAccounts: async () => {
        const response = await api.get('/accounts');
        return response.data.accounts;
    },

    getAccount: async (accountId) => {
        const response = await api.get(`/accounts/${accountId}`);
        return response.data;
    },

    createAccount: async (accountType) => {
        const response = await api.post('/accounts', { accountType });
        return response.data;
    },

    closeAccount: async (accountId) => {
        const response = await api.delete(`/accounts/${accountId}`);
        return response.data;
    },
};

export const transferService = {
    transfer: async (data) => {
        const response = await api.post('/transfers', data);
        return response.data;
    },

    getTransactions: async (params = {}) => {
        const response = await api.get('/transactions', { params });
        return response.data;
    },
};
