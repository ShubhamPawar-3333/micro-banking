import { createContext, useContext, useState } from 'react';
import { authService } from '../services';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [isAuthenticated, setIsAuthenticated] = useState(() => authService.isAuthenticated());
    const loading = false; // Auth check is synchronous (localStorage)

    const login = async (credentials) => {
        await authService.login(credentials);
        setIsAuthenticated(true);
    };

    const logout = () => {
        authService.logout();
        setIsAuthenticated(false);
    };

    const register = async (data) => {
        return authService.register(data);
    };

    return (
        <AuthContext.Provider value={{ isAuthenticated, loading, login, logout, register }}>
            {children}
        </AuthContext.Provider>
    );
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
}
