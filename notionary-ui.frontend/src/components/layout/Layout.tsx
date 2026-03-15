import { type ReactNode } from 'react';
import { Navbar } from './Navbar';

interface LayoutProps {
  children: ReactNode;
  className?: string;
}

export function Layout({ children, className }: LayoutProps) {
  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <main className={className ?? 'max-w-6xl mx-auto px-4 sm:px-6 py-8'}>
        {children}
      </main>
    </div>
  );
}
