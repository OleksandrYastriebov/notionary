import { Lock, ArrowLeft } from 'lucide-react'
import { useNavigate } from 'react-router-dom'

const PrivateWishlistMessage = () => {
  const navigate = useNavigate()

  return (
    <div className="page-container">
      <div className="max-w-md mx-auto text-center py-20">
        <div className="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-6">
          <Lock className="w-10 h-10 text-gray-400" />
        </div>
        <h2 className="text-2xl font-bold text-gray-900 mb-3">
          This wishlist is private
        </h2>
        <p className="text-gray-600 mb-8">
          You don't have permission to view this wishlist. 
          Only the owner can see private wishlists.
        </p>
        <button
          onClick={() => navigate('/')}
          className="btn-primary inline-flex items-center gap-2"
        >
          <ArrowLeft className="w-5 h-5" />
          Go Home
        </button>
      </div>
    </div>
  )
}

export default PrivateWishlistMessage