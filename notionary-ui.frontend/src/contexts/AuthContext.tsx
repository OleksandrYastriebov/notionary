import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useState,
  type ReactNode,
} from 'react';
import { getMe, signIn as apiSignIn, signOut as apiSignOut } from '../api/endpoints';
import { setAccessToken, setAuthFailureHandler, refreshAccessToken } from '../api/axios';
import type { SignInRequest, UserProfileDto } from '../types';

interface AuthContextValue {
  user: UserProfileDto | null;
  isLoading: boolean;
  login: (credentials: SignInRequest) => Promise<void>;
  logout: () => Promise<void>;
  updateUser: (user: UserProfileDto) => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserProfileDto | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  const clearAuth = useCallback(() => {
    setUser(null);
    setAccessToken(null);
  }, []);

  // Register the failure handler so axios can clear auth on refresh failure
  useEffect(() => {
    setAuthFailureHandler(clearAuth);
  }, [clearAuth]);

  // On mount: refresh access token first, then fetch the user — avoids spurious 401 on /me
  useEffect(() => {
    const bootstrap = async () => {
      try {
        const { accessToken: token } = await refreshAccessToken();
        setAccessToken(token);
        const me = await getMe();
        setUser(me);
      } catch {
        // No valid refresh cookie — user is not authenticated, that's fine
        clearAuth();
      } finally {
        setIsLoading(false);
      }
    };
    void bootstrap();
  }, [clearAuth]);

  const login = useCallback(async (credentials: SignInRequest) => {
    const data = await apiSignIn(credentials);
    setAccessToken(data.jwtToken);
    const me = await getMe();
    setUser(me);
  }, []);

  const logout = useCallback(async () => {
    try {
      await apiSignOut();
    } finally {
      clearAuth();
    }
  }, [clearAuth]);

  const updateUser = useCallback((updatedUser: UserProfileDto) => {
    setUser(updatedUser);
  }, []);

  return (
    <AuthContext.Provider value={{ user, isLoading, login, logout, updateUser }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuthContext(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuthContext must be used within AuthProvider');
  }
  return ctx;
}
