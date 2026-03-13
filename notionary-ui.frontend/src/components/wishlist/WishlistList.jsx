import { useState, useEffect } from 'react'
import { wishlistApi } from '../../api/wishlistApi'
import { Plus } from 'lucide-react'
import WishlistCard from './WishlistCard'
import CreateWishlist from './CreateWishlist'
import Loading from '../common/Loading'
import toast from 'react-hot-toast'

const WishlistList = () => {
  const [wishlists, setWishlists] = useState([])
  const [loading, setLoading] = useState(true)
  const [showCreate, setShowCreate] = useState(false)

  useEffect(() => {
    fetchWishlists()
  }, [])

  const fetchWishlists = async () => {
    try {
      const response = await wishlistApi.getWishlists()
      setWishlists(response.data.wishLists || [])
    } catch (error) {
      toast.error('Failed to load wishlists')
    } finally {
      setLoading(false)
    }
  }

  const handleWishlistCreated = () => {
    setShowCreate(false)
    fetchWishlists()
  }

  const handleWishlistDeleted = (wishlistId) => {
    setWishlists(wishlists.filter(w => w.id !== wishlistId))
    toast.success('Wishlist deleted successfully')
  }

  if (loading) return <Loading />

  return (
    <div className="page-container">
      {/* Header */}
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">My Wishlists</h1>
          <p className="text-gray-600 mt-1">
            {wishlists.length} {wishlists.length === 1 ? 'wishlist' : 'wishlists'}
          </p>
        </div>
        <button
          onClick={() => setShowCreate(true)}
          className="btn-primary flex items-center gap-2"
        >
          <Plus className="w-5 h-5" />
          <span className="hidden sm:inline">Create Wishlist</span>
          <span className="sm:hidden">Create</span>
        </button>
      </div>

      {/* Wishlists Grid */}
      {wishlists.length === 0 ? (
        <div className="text-center py-20">
          <div className="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <Plus className="w-10 h-10 text-gray-400" />
          </div>
          <h3 className="text-xl font-semibold text-gray-900 mb-2">No wishlists yet</h3>
          <p className="text-gray-600 mb-6">Create your first wishlist to get started</p>
          <button
            onClick={() => setShowCreate(true)}
            className="btn-primary"
          >
            Create Your First Wishlist
          </button>
        </div>
      ) : (
        <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {wishlists.map(wishlist => (
            <WishlistCard
              key={wishlist.id}
              wishlist={wishlist}
              onDelete={handleWishlistDeleted}
            />
          ))}
        </div>
      )}

      {/* Create Wishlist Modal */}
      {showCreate && (
        <CreateWishlist
          onClose={() => setShowCreate(false)}
          onCreated={handleWishlistCreated}
        />
      )}
    </div>
  )
}

export default WishlistList