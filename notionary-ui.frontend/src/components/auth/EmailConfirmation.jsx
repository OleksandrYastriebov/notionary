import { useEffect, useState } from 'react'
import { useSearchParams, Link } from 'react-router-dom'
import { authApi } from '../../api/authApi'
import { CheckCircle, XCircle, Loader2 } from 'lucide-react'

const EmailConfirmation = () => {
  const [searchParams] = useSearchParams()
  const [status, setStatus] = useState('loading') // loading, success, error
  const [message, setMessage] = useState('')

  useEffect(() => {
    const confirmEmail = async () => {
      const token = searchParams.get('token')
      
      if (!token) {
        setStatus('error')
        setMessage('Invalid confirmation link')
        return
      }

      try {
        const response = await authApi.confirmEmail(token)
        setStatus('success')
        setMessage(response.data.message || 'Email confirmed successfully!')
      } catch (error) {
        setStatus('error')
        setMessage(error.response?.data?.message || 'Confirmation failed')
      }
    }

    confirmEmail()
  }, [searchParams])

  return (
    <div className="min-h-[calc(100vh-4rem)] flex items-center justify-center py-12 px-4">
      <div className="max-w-md w-full">
        <div className="card text-center">
          {status === 'loading' && (
            <>
              <Loader2 className="w-16 h-16 text-primary-600 animate-spin mx-auto mb-4" />
              <h2 className="text-2xl font-bold text-gray-900 mb-2">Confirming your email...</h2>
              <p className="text-gray-600">Please wait</p>
            </>
          )}

          {status === 'success' && (
            <>
              <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <CheckCircle className="w-10 h-10 text-green-600" />
              </div>
              <h2 className="text-2xl font-bold text-gray-900 mb-2">Email Confirmed!</h2>
              <p className="text-gray-600 mb-6">{message}</p>
              <Link to="/sign-in" className="btn-primary inline-block">
                Sign In Now
              </Link>
            </>
          )}

          {status === 'error' && (
            <>
              <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <XCircle className="w-10 h-10 text-red-600" />
              </div>
              <h2 className="text-2xl font-bold text-gray-900 mb-2">Confirmation Failed</h2>
              <p className="text-gray-600 mb-6">{message}</p>
              <Link to="/sign-up" className="btn-primary inline-block">
                Sign Up Again
              </Link>
            </>
          )}
        </div>
      </div>
    </div>
  )
}

export default EmailConfirmation