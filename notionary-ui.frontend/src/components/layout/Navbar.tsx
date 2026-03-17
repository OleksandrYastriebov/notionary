import { Link, useNavigate } from 'react-router-dom';
import { Sparkles, LogOut, User, List } from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { Avatar } from '../ui/Avatar';
import { UserSearchDropdown } from '../search/UserSearchDropdown';
import toast from 'react-hot-toast';
import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';

export function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);

  const handleLogout = async () => {
    try {
      await logout();
      toast.success('Signed out successfully.');
      navigate('/');
    } catch {
      toast.error('Failed to sign out.');
    }
  };

  return (
    <nav className="sticky top-0 z-40 px-3 sm:px-5 pt-3 pb-1.5">
      <div className="relative max-w-6xl mx-auto bg-[#13131f]/90 backdrop-blur-xl border border-white/[0.08] rounded-2xl shadow-xl shadow-black/30 px-4 sm:px-5 flex items-center justify-between h-16 overflow-hidden">
        {/* Left glow — violet */}
        <div className="absolute -left-2 top-1/2 -translate-y-1/2 w-72 h-20 rounded-full bg-violet-500/30 blur-2xl pointer-events-none" />
        {/* Right glow — tiffany */}
        <div className="absolute -right-2 top-1/2 -translate-y-1/2 w-72 h-20 rounded-full bg-[#0abfbc]/25 blur-2xl pointer-events-none" />
        {/* Logo */}
        <Link
          to={user ? '/wishlists' : '/'}
          className="flex items-center gap-2.5 font-bold text-white hover:text-violet-400 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-violet-500 rounded-lg"
        >
          <div className="w-8 h-8 rounded-xl bg-gradient-to-br from-violet-500 to-purple-700 flex items-center justify-center shadow-lg shadow-violet-500/30">
            <Sparkles size={16} className="text-white" />
          </div>
          <span className="text-lg tracking-tight">Wishoria</span>
        </Link>

        {/* Search — only when authenticated */}
        {user && (
          <div className="flex-1 flex justify-center px-4 sm:px-8 max-w-xs sm:max-w-sm mx-auto">
            <UserSearchDropdown />
          </div>
        )}

        {/* Right side */}
        {user ? (
          <div className="relative">
            <button
              onClick={() => setMenuOpen((p) => !p)}
              className="flex items-center gap-2.5 px-2 py-1 rounded-xl hover:bg-white/[0.06] active:bg-white/[0.1] transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-violet-500"
              aria-label="Open user menu"
            >
              <Avatar src={user.avatarUrl} firstName={user.firstName} lastName={user.lastName} size="md" />
              <span className="hidden sm:block text-sm font-medium text-[#c8c8da] max-w-[140px] truncate">
                {user.firstName}
              </span>
            </button>

            <AnimatePresence>
              {menuOpen && (
                <>
                  <div className="fixed inset-0 z-40" onClick={() => setMenuOpen(false)} />
                  <motion.div
                    initial={{ opacity: 0, scale: 0.95, y: -4 }}
                    animate={{ opacity: 1, scale: 1, y: 0 }}
                    exit={{ opacity: 0, scale: 0.95, y: -4 }}
                    transition={{ duration: 0.15 }}
                    className="absolute right-0 mt-2 w-52 bg-[#18181f] rounded-xl shadow-2xl shadow-black/50 border border-white/[0.08] overflow-hidden z-50"
                  >
                    <div className="px-4 py-3 border-b border-white/[0.06]">
                      <p className="text-sm font-semibold text-white truncate">{user.firstName} {user.lastName}</p>
                      <p className="text-xs text-[#9898b4] truncate mt-0.5">{user.email}</p>
                    </div>
                    <div className="py-1.5">
                      <Link to="/wishlists" onClick={() => setMenuOpen(false)} className="flex items-center gap-2.5 px-4 py-2.5 text-sm text-[#c8c8da] hover:bg-white/[0.05] hover:text-white transition-colors">
                        <List size={15} className="text-[#9898b4]" />
                        My Wishlists
                      </Link>
                      <Link to="/profile" onClick={() => setMenuOpen(false)} className="flex items-center gap-2.5 px-4 py-2.5 text-sm text-[#c8c8da] hover:bg-white/[0.05] hover:text-white transition-colors">
                        <User size={15} className="text-[#9898b4]" />
                        Profile
                      </Link>
                      <div className="my-1 border-t border-white/[0.06]" />
                      <button onClick={() => { setMenuOpen(false); void handleLogout(); }} className="flex w-full items-center gap-2.5 px-4 py-2.5 text-sm text-red-400 hover:bg-red-500/10 transition-colors">
                        <LogOut size={15} />
                        Sign out
                      </button>
                    </div>
                  </motion.div>
                </>
              )}
            </AnimatePresence>
          </div>
        ) : (
          <div className="flex items-center gap-2">
            <Link to="/sign-in" className="px-3 py-1.5 text-sm font-medium text-[#9898b4] hover:text-white transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-violet-500 rounded-lg">
              Sign in
            </Link>
            <Link to="/sign-up" className="px-3.5 py-1.5 text-sm font-medium text-white bg-violet-600 rounded-xl hover:bg-violet-500 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-violet-500 focus-visible:ring-offset-2 focus-visible:ring-offset-[#08080e]">
              Get started
            </Link>
          </div>
        )}
      </div>
    </nav>
  );
}
