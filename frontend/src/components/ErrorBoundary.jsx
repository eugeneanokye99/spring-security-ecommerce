import React from 'react';
import { AlertTriangle, RefreshCw, Home } from 'lucide-react';

class ErrorBoundary extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            hasError: false,
            error: null,
            errorInfo: null
        };
    }

    static getDerivedStateFromError(error) {
        return { hasError: true };
    }

    componentDidCatch(error, errorInfo) {
        console.error('Error caught by boundary:', error, errorInfo);
        this.setState({
            error: error,
            errorInfo: errorInfo
        });

        // You can log to an error reporting service here
        // logErrorToService(error, errorInfo);
    }

    handleReload = () => {
        window.location.reload();
    };

    handleGoHome = () => {
        window.location.href = '/';
    };

    render() {
        if (this.state.hasError) {
            return (
                <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
                    <div className="max-w-md w-full">
                        <div className="bg-white rounded-2xl shadow-xl p-8 text-center">
                            {/* Error Icon */}
                            <div className="w-20 h-20 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-6">
                                <AlertTriangle className="w-10 h-10 text-red-600" />
                            </div>

                            {/* Error Message */}
                            <h1 className="text-2xl font-bold text-gray-900 mb-3">
                                Oops! Something went wrong
                            </h1>
                            <p className="text-gray-600 mb-6">
                                We encountered an unexpected error. Don't worry, our team has been notified and we're working on it.
                            </p>

                            {/* Error Details (Development only) */}
                            {process.env.NODE_ENV === 'development' && this.state.error && (
                                <div className="bg-red-50 border border-red-200 rounded-lg p-4 mb-6 text-left">
                                    <p className="text-sm font-semibold text-red-800 mb-2">
                                        Error Details (Development):
                                    </p>
                                    <p className="text-xs text-red-700 font-mono break-all">
                                        {this.state.error.toString()}
                                    </p>
                                    {this.state.errorInfo?.componentStack && (
                                        <details className="mt-2">
                                            <summary className="text-xs text-red-600 cursor-pointer hover:text-red-800">
                                                Component Stack
                                            </summary>
                                            <pre className="text-xs text-red-600 mt-2 overflow-auto max-h-40">
                                                {this.state.errorInfo.componentStack}
                                            </pre>
                                        </details>
                                    )}
                                </div>
                            )}

                            {/* Action Buttons */}
                            <div className="flex flex-col sm:flex-row gap-3 justify-center">
                                <button
                                    onClick={this.handleReload}
                                    className="flex items-center justify-center gap-2 px-6 py-3 bg-blue-600 text-white font-medium rounded-lg hover:bg-blue-700 transition-colors"
                                >
                                    <RefreshCw className="w-4 h-4" />
                                    Reload Page
                                </button>
                                <button
                                    onClick={this.handleGoHome}
                                    className="flex items-center justify-center gap-2 px-6 py-3 bg-gray-200 text-gray-700 font-medium rounded-lg hover:bg-gray-300 transition-colors"
                                >
                                    <Home className="w-4 h-4" />
                                    Go Home
                                </button>
                            </div>

                            {/* Support Message */}
                            <p className="text-xs text-gray-500 mt-6">
                                If this problem persists, please contact our support team
                            </p>
                        </div>
                    </div>
                </div>
            );
        }

        return this.props.children;
    }
}

export default ErrorBoundary;
