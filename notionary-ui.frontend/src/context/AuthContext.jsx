import { createContext, useContext, useState, useEffect } from 'react'
import { authApi } from '../api/authApi'
import { tokenManager } from '../utils/tokenManager'
import { userApi } from '../api/userApi'

const AuthContext = createContext(null)

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    initAuth()
    
    // Listen for auto-refresh events
    window.addEventListener('token-refresh-needed', handleAutoRefresh)
    
    return () => {
      window.removeEventListener('token-refresh-needed', handleAutoRefresh)
    }
  }, [])

  const initAuth = async () => {
    // Try to get current user (cookie will be sent automatically)
    try {
      const response = await userApi.getCurrentUser()
      setUser({
        id: response.data.id,
        email: response.data.email,
        firstName: response.data.firstName,
        lastName: response.data.lastName,
      })
    } catch (error) {
      // No valid session
      tokenManager.clearTokens()
    } finally {
      setLoading(false)
    }
  }

  const handleAutoRefresh = async () => {
    try {
      const response = await authApi.refreshToken()
      tokenManager.setAccessToken(response.data.accessToken)
    } catch (error) {
      console.error('Auto-refresh failed:', error)
      signOut()
    }
  }

  const signIn = async (credentials) => {
    const response = await authApi.signIn(credentials)
    const { jwtToken, id, email } = response.data
    
    tokenManager.setAccessToken(jwtToken)
    setUser({ id, email })
    
    return response.data
  }

  const signUp = async (userData) => {
    return await authApi.signUp(userData)
  }

  const signOut = async () => {
    try {
      await authApi.signOut()
    } catch (error) {
      // Ignore errors during sign out (token might be expired)
      console.log('Sign out error (ignored):', error)
    } finally {
      tokenManager.clearTokens()
      setUser(null)
    }
  }

  return (
    <AuthContext.Provider value={{
      user,
      loading,
      signIn,
      signUp,
      signOut,
    }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return context
}