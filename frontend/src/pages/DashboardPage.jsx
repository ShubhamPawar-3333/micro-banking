import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { accountService, transferService } from '../services';

export default function DashboardPage() {
    const [accounts, setAccounts] = useState([]);
    const [transactions, setTransactions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showNewAccountModal, setShowNewAccountModal] = useState(false);

    useEffect(() => {
        loadData();
    }, []);

    const loadData = async () => {
        try {
            const [accountsData, txnData] = await Promise.all([
                accountService.getAccounts(),
                transferService.getTransactions({ size: 5 }),
            ]);
            setAccounts(accountsData);
            setTransactions(txnData.content || []);
        } catch (error) {
            console.error('Failed to load data:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleCreateAccount = async (accountType) => {
        try {
            await accountService.createAccount(accountType);
            setShowNewAccountModal(false);
            loadData();
        } catch (error) {
            alert('Failed to create account');
        }
    };

    const totalBalance = accounts.reduce((sum, acc) => sum + parseFloat(acc.balance || 0), 0);

    if (loading) {
        return <div className="container dashboard">Loading...</div>;
    }

    return (
        <div className="container dashboard">
            <div className="dashboard-header">
                <h1 className="dashboard-title">Dashboard</h1>
                <button className="btn btn-primary" onClick={() => setShowNewAccountModal(true)}>
                    + New Account
                </button>
            </div>

            {/* Accounts Overview */}
            <div className="accounts-grid">
                {accounts.map((account) => (
                    <Link key={account.id} to={`/accounts/${account.id}`} style={{ textDecoration: 'none' }}>
                        <div className="card account-card">
                            <span className="account-type">{account.accountType}</span>
                            <p className="account-number">****{account.accountNumber.slice(-4)}</p>
                            <p className="card-value">${parseFloat(account.balance).toLocaleString('en-US', { minimumFractionDigits: 2 })}</p>
                        </div>
                    </Link>
                ))}

                <div className="card" style={{ background: 'linear-gradient(135deg, #2563eb, #7c3aed)' }}>
                    <p className="card-title" style={{ color: 'rgba(255,255,255,0.8)' }}>Total Balance</p>
                    <p className="card-value">${totalBalance.toLocaleString('en-US', { minimumFractionDigits: 2 })}</p>
                </div>
            </div>

            {/* Quick Actions */}
            <div style={{ marginBottom: '2rem' }}>
                <h2 style={{ fontSize: '1.125rem', marginBottom: '1rem' }}>Quick Actions</h2>
                <div style={{ display: 'flex', gap: '1rem' }}>
                    <Link to="/transfer" className="btn btn-secondary">Transfer Funds</Link>
                    <Link to="/transactions" className="btn btn-secondary">View All Transactions</Link>
                </div>
            </div>

            {/* Recent Transactions */}
            <div>
                <h2 style={{ fontSize: '1.125rem', marginBottom: '1rem' }}>Recent Transactions</h2>
                {transactions.length > 0 ? (
                    <div className="transaction-list">
                        {transactions.map((txn) => (
                            <div key={txn.id} className="transaction-item">
                                <div>
                                    <p style={{ fontWeight: 500 }}>{txn.type}</p>
                                    <p style={{ fontSize: '0.875rem', color: 'var(--color-text-muted)' }}>
                                        {new Date(txn.createdAt).toLocaleDateString()}
                                    </p>
                                </div>
                                <span className={`transaction-amount ${txn.type === 'DEPOSIT' ? 'positive' : 'negative'}`}>
                                    {txn.type === 'DEPOSIT' ? '+' : '-'}${parseFloat(txn.amount).toFixed(2)}
                                </span>
                            </div>
                        ))}
                    </div>
                ) : (
                    <p style={{ color: 'var(--color-text-muted)' }}>No transactions yet</p>
                )}
            </div>

            {/* New Account Modal */}
            {showNewAccountModal && (
                <div className="modal-overlay" onClick={() => setShowNewAccountModal(false)}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                        <h2 className="modal-title">Create New Account</h2>
                        <p style={{ marginBottom: '1.5rem', color: 'var(--color-text-muted)' }}>
                            Select account type:
                        </p>
                        <div style={{ display: 'flex', gap: '1rem' }}>
                            <button className="btn btn-primary" style={{ flex: 1 }} onClick={() => handleCreateAccount('SAVINGS')}>
                                Savings
                            </button>
                            <button className="btn btn-secondary" style={{ flex: 1 }} onClick={() => handleCreateAccount('CHECKING')}>
                                Checking
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
