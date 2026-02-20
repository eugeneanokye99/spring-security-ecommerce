import { useState, useEffect } from 'react';
import { Shield, Search, Filter, Calendar, User, AlertTriangle, CheckCircle, XCircle, Clock, Key, LogOut, UserPlus, Lock } from 'lucide-react';
import axios from 'axios';
import { showErrorAlert } from '../../utils/errorHandler';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

const SecurityAuditLogManagement = () => {
    const [logs, setLogs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    
    // Filters
    const [filters, setFilters] = useState({
        username: '',
        eventType: '',
        startDate: '',
        endDate: ''
    });

    const eventTypes = [
        { value: '', label: 'All Event Types' },
        { value: 'LOGIN_SUCCESS', label: 'Login Success', icon: CheckCircle, color: 'green' },
        { value: 'LOGIN_FAILURE', label: 'Login Failure', icon: XCircle, color: 'red' },
        { value: 'REGISTRATION', label: 'Registration', icon: UserPlus, color: 'blue' },
        { value: 'LOGOUT', label: 'Logout', icon: LogOut, color: 'gray' },
        { value: 'ACCESS_DENIED', label: 'Access Denied', icon: AlertTriangle, color: 'yellow' },
        { value: 'TOKEN_EXPIRED', label: 'Token Expired', icon: Clock, color: 'orange' },
        { value: 'TOKEN_INVALID', label: 'Token Invalid', icon: Key, color: 'red' },
        { value: 'PASSWORD_CHANGE', label: 'Password Change', icon: Lock, color: 'indigo' },
        { value: 'OAUTH2_LOGIN_SUCCESS', label: 'OAuth2 Login Success', icon: CheckCircle, color: 'purple' },
        { value: 'OAUTH2_LOGIN_FAILURE', label: 'OAuth2 Login Failure', icon: XCircle, color: 'red' }
    ];

    useEffect(() => {
        loadLogs();
    }, [page, filters]);

    const loadLogs = async () => {
        setLoading(true);
        try {
            const token = localStorage.getItem('token');
            let url = `${API_BASE_URL}/audit-logs?page=${page}&size=20&sortBy=timestamp&sortDir=desc`;
            
            // Apply filters
            if (filters.username && filters.eventType) {
                url = `${API_BASE_URL}/audit-logs/filter?username=${filters.username}&eventType=${filters.eventType}&page=${page}&size=20`;
            } else if (filters.username) {
                url = `${API_BASE_URL}/audit-logs/user/${filters.username}?page=${page}&size=20`;
            } else if (filters.eventType) {
                url = `${API_BASE_URL}/audit-logs/event-type/${filters.eventType}?page=${page}&size=20`;
            } else if (filters.startDate && filters.endDate) {
                url = `${API_BASE_URL}/audit-logs/date-range?startTime=${filters.startDate}&endTime=${filters.endDate}&page=${page}&size=20`;
            }

            const response = await axios.get(url, {
                headers: { Authorization: `Bearer ${token}` }
            });

            if (response.data?.data?.content) {
                setLogs(response.data.data.content);
                setTotalPages(response.data.data.totalPages);
                setTotalElements(response.data.data.totalElements);
            }
        } catch (error) {
            showErrorAlert(error, 'Failed to load security audit logs');
        } finally {
            setLoading(false);
        }
    };

    const handleFilterChange = (key, value) => {
        setFilters(prev => ({ ...prev, [key]: value }));
        setPage(0); // Reset to first page when filters change
    };

    const clearFilters = () => {
        setFilters({ username: '', eventType: '', startDate: '', endDate: '' });
        setPage(0);
    };

    const getEventIcon = (eventType) => {
        const event = eventTypes.find(e => e.value === eventType);
        if (!event || !event.icon) return Shield;
        return event.icon;
    };

    const getEventColor = (eventType) => {
        const event = eventTypes.find(e => e.value === eventType);
        return event?.color || 'gray';
    };

    const getEventBadge = (eventType, success) => {
        const color = getEventColor(eventType);
        const Icon = getEventIcon(eventType);
        
        return (
            <span className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-medium bg-${color}-100 text-${color}-800`}>
                <Icon className="w-3.5 h-3.5" />
                {eventType.replace(/_/g, ' ')}
            </span>
        );
    };

    const formatTimestamp = (timestamp) => {
        return new Date(timestamp).toLocaleString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });
    };

    return (
        <div>
            <div className="flex items-center justify-between mb-6">
                <div className="flex items-center gap-3">
                    <div className="p-2 bg-purple-100 rounded-lg">
                        <Shield className="w-6 h-6 text-purple-600" />
                    </div>
                    <div>
                        <h1 className="text-3xl font-bold text-gray-900">Security Audit Logs</h1>
                        <p className="text-sm text-gray-600 mt-1">Monitor all security events and authentication activities</p>
                    </div>
                </div>
                <div className="text-right">
                    <div className="text-2xl font-bold text-purple-600">{totalElements}</div>
                    <div className="text-xs text-gray-500">Total Events</div>
                </div>
            </div>

            {/* Filters */}
            <div className="card mb-6">
                <div className="flex items-center gap-2 mb-4">
                    <Filter className="w-5 h-5 text-gray-600" />
                    <h2 className="text-lg font-semibold text-gray-900">Filters</h2>
                </div>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            <User className="w-4 h-4 inline mr-1" />
                            Username
                        </label>
                        <input
                            type="text"
                            value={filters.username}
                            onChange={(e) => handleFilterChange('username', e.target.value)}
                            placeholder="Filter by username"
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                        />
                    </div>
                    
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            <Shield className="w-4 h-4 inline mr-1" />
                            Event Type
                        </label>
                        <select
                            value={filters.eventType}
                            onChange={(e) => handleFilterChange('eventType', e.target.value)}
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                        >
                            {eventTypes.map(type => (
                                <option key={type.value} value={type.value}>{type.label}</option>
                            ))}
                        </select>
                    </div>
                    
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            <Calendar className="w-4 h-4 inline mr-1" />
                            Start Date
                        </label>
                        <input
                            type="datetime-local"
                            value={filters.startDate}
                            onChange={(e) => handleFilterChange('startDate', e.target.value)}
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                        />
                    </div>
                    
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            <Calendar className="w-4 h-4 inline mr-1" />
                            End Date
                        </label>
                        <input
                            type="datetime-local"
                            value={filters.endDate}
                            onChange={(e) => handleFilterChange('endDate', e.target.value)}
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                        />
                    </div>
                </div>
                
                <div className="mt-4 flex gap-2">
                    <button
                        onClick={loadLogs}
                        className="btn btn-primary flex items-center gap-2"
                    >
                        <Search className="w-4 h-4" />
                        Apply Filters
                    </button>
                    <button
                        onClick={clearFilters}
                        className="btn btn-secondary"
                    >
                        Clear Filters
                    </button>
                </div>
            </div>

            {/* Logs Table */}
            <div className="card overflow-hidden">
                <div className="overflow-x-auto">
                    <table className="w-full">
                        <thead className="bg-gray-50">
                            <tr>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Timestamp</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Username</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Event Type</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">IP Address</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Details</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-200">
                            {loading ? (
                                <tr>
                                    <td colSpan="6" className="px-6 py-12 text-center">
                                        <div className="w-8 h-8 border-4 border-purple-600 border-t-transparent rounded-full animate-spin mx-auto"></div>
                                    </td>
                                </tr>
                            ) : logs.length === 0 ? (
                                <tr>
                                    <td colSpan="6" className="px-6 py-12 text-center text-gray-500">
                                        No audit logs found
                                    </td>
                                </tr>
                            ) : (
                                logs.map((log) => (
                                    <tr key={log.id} className="hover:bg-gray-50">
                                        <td className="px-6 py-4 text-sm text-gray-900 whitespace-nowrap">
                                            {formatTimestamp(log.timestamp)}
                                        </td>
                                        <td className="px-6 py-4 text-sm">
                                            <div className="flex items-center gap-2">
                                                <div className="h-8 w-8 bg-gray-100 rounded-full flex items-center justify-center">
                                                    <User className="w-4 h-4 text-gray-600" />
                                                </div>
                                                <span className="font-medium text-gray-900">
                                                    {log.username || 'Anonymous'}
                                                </span>
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 text-sm">
                                            {getEventBadge(log.eventType, log.success)}
                                        </td>
                                        <td className="px-6 py-4 text-sm text-gray-600 font-mono">
                                            {log.ipAddress || 'N/A'}
                                        </td>
                                        <td className="px-6 py-4 text-sm">
                                            {log.success === true ? (
                                                <span className="inline-flex items-center gap-1 text-green-600">
                                                    <CheckCircle className="w-4 h-4" />
                                                    Success
                                                </span>
                                            ) : log.success === false ? (
                                                <span className="inline-flex items-center gap-1 text-red-600">
                                                    <XCircle className="w-4 h-4" />
                                                    Failed
                                                </span>
                                            ) : (
                                                <span className="text-gray-400">N/A</span>
                                            )}
                                        </td>
                                        <td className="px-6 py-4 text-sm text-gray-600 max-w-xs truncate">
                                            {log.details || '-'}
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>

                {/* Pagination */}
                {totalPages > 1 && (
                    <div className="border-t border-gray-200 px-6 py-4 flex items-center justify-between">
                        <div className="text-sm text-gray-700">
                            Showing page {page + 1} of {totalPages}
                        </div>
                        <div className="flex gap-2">
                            <button
                                onClick={() => setPage(p => Math.max(0, p - 1))}
                                disabled={page === 0}
                                className="px-4 py-2 border border-gray-300 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
                            >
                                Previous
                            </button>
                            <button
                                onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                                disabled={page >= totalPages - 1}
                                className="px-4 py-2 border border-gray-300 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
                            >
                                Next
                            </button>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default SecurityAuditLogManagement;
