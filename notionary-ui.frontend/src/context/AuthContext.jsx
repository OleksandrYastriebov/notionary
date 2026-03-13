import { createContext, useContext, useState, useEffect } from 'react'
import { authApi } from '../api/authApi'
import { tokenManager } from '../utils/tokenManager'

const AuthContext = createContext(null)

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    // Check for existing session on mount
    const initAuth = async () => {
      const accessToken = tokenManager.getAccessToken()
      const refreshToken = tokenManager.getRefreshToken()
      
      if (accessToken && refreshToken) {
        const userData = tokenManager.getUserData()
        setUser(userData)
      }
      setLoading(false)
    }

    initAuth()
  }, [])

  const signIn = async (credentials) => {
    const response = await authApi.signIn(credentials)
    const { jwtToken, refreshToken, id, email } = response.data
    
    tokenManager.setTokens(jwtToken, refreshToken, { id, email })
    setUser({ id, email })
    
    return response.data
  }

  const signUp = async (userData) => {
    return await authApi.signUp(userData)
  }

  const signOut = async () => {
    const refreshToken = tokenManager.getRefreshToken()
    if (refreshToken) {
      try {
        await authApi.signOut({ refreshToken })
      } catch (error) {
        console.error('Sign out error:', error)
      }
    }
    
    tokenManager.clearTokens()
    setUser(null)
  }

  const refreshAccessToken = async () => {
    const refreshToken = tokenManager.getRefreshToken()
    if (!refreshToken) {
      throw new Error('No refresh token available')
    }

    const response = await authApi.refreshToken({ refreshToken })
    const { accessToken, refreshToken: newRefreshToken } = response.data
    
    tokenManager.setTokens(accessToken, newRefreshToken)
    return accessToken
  }

  return (
    <AuthContext.Provider value={{
      user,
      loading,
      signIn,
      signUp,
      signOut,
      refreshAccessToken
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