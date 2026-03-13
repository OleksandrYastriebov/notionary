import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { Gift, Users, Lock, Sparkles } from 'lucide-react'

const Landing = () => {
  const { user } = useAuth()

  return (
    <div className="min-h-screen bg-gradient-to-b from-white to-primary-50">
      {/* Hero Section */}
      <div className="page-container">
        <div className="text-center max-w-4xl mx-auto py-20">
          <div className="inline-flex items-center gap-2 bg-primary-100 text-primary-700 px-4 py-2 rounded-full text-sm font-medium mb-8">
            <Sparkles className="w-4 h-4" />
            The easiest way to manage wishlists
          </div>

          <h1 className="text-5xl md:text-6xl font-bold text-gray-900 mb-6">
            Create & Share Your
            <span className="block text-primary-600">Dream Wishlists</span>
          </h1>

          <p className="text-xl text-gray-600 mb-12 max-w-2xl mx-auto">
            Keep track of everything you want. Share with friends and family. Never forget a gift idea again.
          </p>

          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            {user ? (
              <Link to="/wishlists" className="btn-primary text-lg px-8 py-3">
                Go to My Wishlists
              </Link>
            ) : (
              <>
                <Link to="/sign-up" className="btn-primary text-lg px-8 py-3">
                  Get Started Free
                </Link>
                <Link to="/sign-in" className="btn-secondary text-lg px-8 py-3">
                  Sign In
                </Link>
              </>
            )}
          </div>
        </div>

        {/* Features */}
        <div className="grid md:grid-cols-3 gap-8 pb-20">
          <div className="card text-center">
            <div className="w-12 h-12 bg-primary-100 rounded-lg flex items-center justify-center mx-auto mb-4">
              <Gift className="w-6 h-6 text-primary-600" />
            </div>
            <h3 className="text-xl font-semibold text-gray-900 mb-2">Easy to Organize</h3>
            <p className="text-gray-600">
              Create multiple wishlists for different occasions. Add items with images, prices, and links.
            </p>
          </div>

          <div className="card text-center">
            <div className="w-12 h-12 bg-primary-100 rounded-lg flex items-center justify-center mx-auto mb-4">
              <Users className="w-6 h-6 text-primary-600" />
            </div>
            <h3 className="text-xl font-semibold text-gray-900 mb-2">Share with Anyone</h3>
            <p className="text-gray-600">
              Make wishlists public or private. Share with friends and family effortlessly.
            </p>
          </div>

          <div className="card text-center">
            <div className="w-12 h-12 bg-primary-100 rounded-lg flex items-center justify-center mx-auto mb-4">
              <Lock className="w-6 h-6 text-primary-600" />
            </div>
            <h3 className="text-xl font-semibold text-gray-900 mb-2">Secure & Private</h3>
            <p className="text-gray-600">
              Your data is encrypted and secure. Control who sees your wishlists.
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Landing