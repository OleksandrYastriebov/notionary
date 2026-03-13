import { useState, useEffect } from 'react'
import { wishlistApi } from '../../api/wishlistApi'
import WishlistCard from './WishlistCard'
import CreateWishlist from './CreateWishlist'
import EmptyWishlistCard from './EmptyWishlistCard'
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
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">My Wishlists</h1>
        <p className="text-gray-600 mt-1">
          {wishlists.length} {wishlists.length === 1 ? 'wishlist' : 'wishlists'}
        </p>
      </div>

      {/* Wishlists Grid */}
      <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
        {/* Empty state card - always show first */}
        <EmptyWishlistCard onClick={() => setShowCreate(true)} />
        
        {/* Wishlist cards */}
        {wishlists.map(wishlist => (
          <WishlistCard
            key={wishlist.id}
            wishlist={wishlist}
            onDelete={handleWishlistDeleted}
          />
        ))}
      </div>

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