import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import Header from '../components/Header';
import { AuthProvider } from '../hooks/useAuth';

vi.mock('../services', () => ({
    authService: {
        isAuthenticated: vi.fn(),
        logout: vi.fn(),
    },
}));

const renderWithProviders = (component) => {
    return render(
        <BrowserRouter>
            <AuthProvider>
                {component}
            </AuthProvider>
        </BrowserRouter>
    );
};

describe('Header', () => {
    it('renders logo', () => {
        renderWithProviders(<Header />);
        expect(screen.getByText('MicroBank')).toBeInTheDocument();
    });

    it('shows login/register when not authenticated', async () => {
        const { authService } = await import('../services');
        authService.isAuthenticated.mockReturnValue(false);

        renderWithProviders(<Header />);

        expect(screen.getByText('Login')).toBeInTheDocument();
        expect(screen.getByText('Get Started')).toBeInTheDocument();
    });

    it('shows navigation when authenticated', async () => {
        const { authService } = await import('../services');
        authService.isAuthenticated.mockReturnValue(true);

        renderWithProviders(<Header />);

        expect(screen.getByText('Dashboard')).toBeInTheDocument();
        expect(screen.getByText('Transfer')).toBeInTheDocument();
        expect(screen.getByText('Transactions')).toBeInTheDocument();
        expect(screen.getByText('Logout')).toBeInTheDocument();
    });
});
