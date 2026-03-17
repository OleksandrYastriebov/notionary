import { type ReactNode } from 'react';
import { Navbar } from './Navbar';

interface LayoutProps {
  children: ReactNode;
  className?: string;
}

export function Layout({ children, className }: LayoutProps) {
  return (
    <div className="min-h-screen">
      {/* Dark base + aurora mesh — fixed behind all content */}
      <div className="fixed inset-0 -z-10 overflow-hidden pointer-events-none">
        <div className="absolute inset-0 bg-[#08080e]" />
        <div className="absolute -top-40 -left-40 w-[700px] h-[700px] rounded-full bg-violet-700/[0.20] blur-[160px]" />
        <div className="absolute top-[30%] -right-60 w-[550px] h-[550px] rounded-full bg-indigo-700/[0.15] blur-[180px]" />
        <div className="absolute -bottom-60 left-[40%] -translate-x-1/2 w-[600px] h-[380px] rounded-full bg-purple-800/[0.12] blur-[200px]" />
      </div>
      <Navbar />
      <main className={className ?? 'max-w-6xl mx-auto px-4 sm:px-6 py-8'}>
        {children}
      </main>
    </div>
  );
}
