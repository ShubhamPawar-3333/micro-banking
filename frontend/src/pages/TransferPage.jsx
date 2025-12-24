import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { accountService, transferService } from '../services';

export default function TransferPage() {
    const [formData, setFormData] = useState({
        fromAccountId: '',
        toAccountId: '',
        amount: '',
        description: '',
    });
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const { data: accounts = [] } = useQuery({
        queryKey: ['accounts'],
        queryFn: accountService.getAccounts,
    });

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            await transferService.transfer({
                ...formData,
                amount: parseFloat(formData.amount),
            });
            navigate('/dashboard');
        } catch (err) {
            setError(err.response?.data?.error?.message || 'Transfer failed');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="container dashboard">
            <h1 className="dashboard-title" style={{ marginBottom: '2rem' }}>Transfer Funds</h1>

            <div className="card" style={{ maxWidth: '500px' }}>
                {error && <p className="form-error" style={{ marginBottom: '1rem' }}>{error}</p>}

                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label className="form-label">From Account</label>
                        <select
                            name="fromAccountId"
                            className="form-input"
                            value={formData.fromAccountId}
                            onChange={handleChange}
                            required
                        >
                            <option value="">Select account</option>
                            {accounts.map((acc) => (
                                <option key={acc.id} value={acc.id}>
                                    {acc.accountType} - ****{acc.accountNumber.slice(-4)} (${parseFloat(acc.balance).toFixed(2)})
                                </option>
                            ))}
                        </select>
                    </div>

                    <div className="form-group">
                        <label className="form-label">To Account</label>
                        <select
                            name="toAccountId"
                            className="form-input"
                            value={formData.toAccountId}
                            onChange={handleChange}
                            required
                        >
                            <option value="">Select account</option>
                            {accounts
                                .filter((acc) => acc.id !== formData.fromAccountId)
                                .map((acc) => (
                                    <option key={acc.id} value={acc.id}>
                                        {acc.accountType} - ****{acc.accountNumber.slice(-4)}
                                    </option>
                                ))}
                        </select>
                    </div>

                    <div className="form-group">
                        <label className="form-label">Amount</label>
                        <input
                            type="number"
                            name="amount"
                            className="form-input"
                            value={formData.amount}
                            onChange={handleChange}
                            min="0.01"
                            step="0.01"
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label className="form-label">Description (optional)</label>
                        <input
                            type="text"
                            name="description"
                            className="form-input"
                            value={formData.description}
                            onChange={handleChange}
                        />
                    </div>

                    <div style={{ display: 'flex', gap: '1rem' }}>
                        <button type="button" className="btn btn-secondary" onClick={() => navigate('/dashboard')}>
                            Cancel
                        </button>
                        <button type="submit" className="btn btn-primary" style={{ flex: 1 }} disabled={loading}>
                            {loading ? 'Processing...' : 'Transfer'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}
