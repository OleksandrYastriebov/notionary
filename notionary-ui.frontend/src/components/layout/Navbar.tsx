import { Link, useNavigate } from 'react-router-dom';
import { Gift, LogOut, User, List } from 'lucide-react';
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
    <nav className="sticky top-0 z-40 bg-white/80 backdrop-blur-md border-b border-gray-100">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 flex items-center justify-between h-16">
        {/* Logo */}
        <Link
          to={user ? '/wishlists' : '/'}
          className="flex items-center gap-2.5 font-bold text-gray-900 hover:text-violet-600 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-violet-500 rounded-lg"
        >
          <Gift size={24} className="text-violet-600" />
          <span className="text-lg">Notionary</span>
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
              className="flex items-center gap-2.5 p-1 rounded-xl hover:bg-gray-100 active:bg-gray-200 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-violet-500"
              aria-label="Open user menu"
            >
              <Avatar
                src={user.avatarUrl}
                firstName={user.firstName}
                lastName={user.lastName}
                size="md"
              />
              <span className="hidden sm:block text-base font-medium text-gray-700 max-w-[140px] truncate">
                {user.firstName}
              </span>
            </button>

            <AnimatePresence>
              {menuOpen && (
                <>
                  <div
                    className="fixed inset-0 z-40"
                    onClick={() => setMenuOpen(false)}
                  />
                  <motion.div
                    initial={{ opacity: 0, scale: 0.95, y: -4 }}
                    animate={{ opacity: 1, scale: 1, y: 0 }}
                    exit={{ opacity: 0, scale: 0.95, y: -4 }}
                    transition={{ duration: 0.15 }}
                    className="absolute right-0 mt-1 w-48 bg-white rounded-xl shadow-lg border border-gray-100 overflow-hidden z-50"
                  >
                    <div className="px-3 py-2.5 border-b border-gray-100">
                      <p className="text-sm font-semibold text-gray-900 truncate">
                        {user.firstName} {user.lastName}
                      </p>
                      <p className="text-xs text-gray-500 truncate">{user.email}</p>
                    </div>
                    <div className="py-1">
                      <Link
                        to="/wishlists"
                        onClick={() => setMenuOpen(false)}
                        className="flex items-center gap-2.5 px-3 py-2 text-sm text-gray-700 hover:bg-gray-50 transition-colors"
                      >
                        <List size={15} />
                        My Wishlists
                      </Link>
                      <Link
                        to="/profile"
                        onClick={() => setMenuOpen(false)}
                        className="flex items-center gap-2.5 px-3 py-2 text-sm text-gray-700 hover:bg-gray-50 transition-colors"
                      >
                        <User size={15} />
                        Profile
                      </Link>
                      <button
                        onClick={() => {
                          setMenuOpen(false);
                          void handleLogout();
                        }}
                        className="flex w-full items-center gap-2.5 px-3 py-2 text-sm text-red-600 hover:bg-red-50 transition-colors"
                      >
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
            <Link
              to="/sign-in"
              className="px-3 py-1.5 text-sm font-medium text-gray-700 hover:text-gray-900 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-violet-500 rounded-lg"
            >
              Sign in
            </Link>
            <Link
              to="/sign-up"
              className="px-3 py-1.5 text-sm font-medium text-white bg-violet-600 rounded-xl hover:bg-violet-700 active:bg-violet-800 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-violet-500 focus-visible:ring-offset-2"
            >
              Get started
            </Link>
          </div>
        )}
      </div>
    </nav>
  );
}
