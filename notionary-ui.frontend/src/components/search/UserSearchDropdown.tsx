import { useState, useRef, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { Search, X, Loader2 } from 'lucide-react';
import { useUserSearch } from '../../hooks/useUserSearch';
import { Avatar } from '../ui/Avatar';
import { cn } from '../../utils/cn';

function useDebounce(value: string, delay: number): string {
  const [debounced, setDebounced] = useState(value);

  useEffect(() => {
    const timer = setTimeout(() => setDebounced(value), delay);
    return () => clearTimeout(timer);
  }, [value, delay]);

  return debounced;
}

export function UserSearchDropdown() {
  const [inputValue, setInputValue] = useState('');
  const [isOpen, setIsOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);
  const navigate = useNavigate();

  const debouncedQuery = useDebounce(inputValue, 350);
  const { data: results = [], isFetching } = useUserSearch(debouncedQuery);

  const showDropdown = isOpen && debouncedQuery.trim().length >= 2;
  const showResults = showDropdown && results.length > 0;
  const showEmpty = showDropdown && !isFetching && results.length === 0;

  // Close on outside click
  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) {
        setIsOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleClear = useCallback(() => {
    setInputValue('');
    setIsOpen(false);
    inputRef.current?.focus();
  }, []);

  const handleSelectUser = useCallback(
    (userId: number) => {
      setIsOpen(false);
      setInputValue('');
      navigate(`/profile/${userId}`);
    },
    [navigate]
  );

  const handleKeyDown = useCallback(
    (e: React.KeyboardEvent<HTMLInputElement>) => {
      if (e.key === 'Escape') {
        setIsOpen(false);
        setInputValue('');
      }
    },
    []
  );

  return (
    <div ref={containerRef} className="relative">
      {/* Search input */}
      <div
        className={cn(
          'flex items-center gap-2 h-9 px-3 rounded-xl border transition-all duration-200',
          'bg-gray-50 border-gray-200',
          'focus-within:bg-white focus-within:border-violet-400 focus-within:shadow-sm focus-within:shadow-violet-100'
        )}
      >
        <Search size={14} className="text-gray-400 flex-shrink-0" />
        <input
          ref={inputRef}
          type="text"
          value={inputValue}
          onChange={(e) => {
            setInputValue(e.target.value);
            setIsOpen(true);
          }}
          onFocus={() => setIsOpen(true)}
          onKeyDown={handleKeyDown}
          placeholder="Search people..."
          className="bg-transparent text-sm text-gray-800 placeholder-gray-400 outline-none w-32 sm:w-44 md:w-52"
          aria-label="Search users"
          aria-expanded={showDropdown}
          aria-autocomplete="list"
          role="combobox"
        />
        <AnimatePresence>
          {inputValue && (
            <motion.button
              initial={{ opacity: 0, scale: 0.7 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.7 }}
              transition={{ duration: 0.1 }}
              onClick={handleClear}
              className="text-gray-400 hover:text-gray-600 active:text-gray-800 transition-colors focus-visible:outline-none rounded"
              aria-label="Clear search"
            >
              <X size={13} />
            </motion.button>
          )}
        </AnimatePresence>
        {isFetching && debouncedQuery.trim().length >= 2 && (
          <Loader2 size={13} className="text-violet-400 animate-spin flex-shrink-0" />
        )}
      </div>

      {/* Dropdown */}
      <AnimatePresence>
        {showDropdown && (
          <motion.div
            initial={{ opacity: 0, y: -6, scale: 0.97 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: -6, scale: 0.97 }}
            transition={{ duration: 0.15 }}
            className="absolute left-0 top-full mt-1.5 w-64 bg-white rounded-xl shadow-lg border border-gray-100 overflow-hidden z-50"
            role="listbox"
            aria-label="Search results"
          >
            {showResults && (
              <ul className="py-1.5 max-h-64 overflow-y-auto">
                {results.map((user, index) => (
                  <motion.li
                    key={user.id}
                    initial={{ opacity: 0, x: -8 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ duration: 0.12, delay: index * 0.04 }}
                    role="option"
                  >
                    <button
                      onClick={() => handleSelectUser(user.id)}
                      className="w-full flex items-center gap-3 px-3 py-2.5 hover:bg-violet-50 active:bg-violet-100 transition-colors text-left focus-visible:outline-none focus-visible:bg-violet-50"
                    >
                      <Avatar
                        src={user.avatarUrl}
                        firstName={user.firstName}
                        lastName={user.lastName}
                        size="sm"
                      />
                      <div className="min-w-0">
                        <p className="text-sm font-medium text-gray-900 truncate">
                          {user.firstName} {user.lastName}
                        </p>
                      </div>
                    </button>
                  </motion.li>
                ))}
              </ul>
            )}

            {showEmpty && (
              <div className="px-4 py-4 text-center">
                <p className="text-sm text-gray-400">No users found</p>
              </div>
            )}

            {isFetching && !showResults && (
              <div className="px-4 py-4 flex items-center justify-center gap-2">
                <Loader2 size={14} className="text-violet-400 animate-spin" />
                <span className="text-sm text-gray-400">Searching...</span>
              </div>
            )}
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
