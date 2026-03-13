import { useState, useEffect, useRef } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { wishlistApi } from '../../api/wishlistApi'
import { wishlistItemApi } from '../../api/wishlistItemApi'
import { useAuth } from '../../context/AuthContext'
import { ArrowLeft, Plus, Globe, Lock, Edit2 } from 'lucide-react'
import { getDeviceSpecificImage } from '../../utils/cloudinary'
import WishlistItemCard from '../wishlistItem/WishlistItemCard'
import CreateWishlistItem from '../wishlistItem/CreateWishlistItem'
import EditWishlist from './EditWishlist'
import EmptyItemCard from '../wishlistItem/EmptyItemCard'
import PrivateWishlistMessage from './PrivateWishlistMessage'
import Loading from '../common/Loading'
import toast from 'react-hot-toast'

const WishlistDetail = () => {
  const { wishlistId } = useParams()
  const navigate = useNavigate()
  const { user } = useAuth()
  
  const [wishlist, setWishlist] = useState(null)
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [showCreate, setShowCreate] = useState(false)
  const [showEdit, setShowEdit] = useState(false)
  const [isPrivate, setIsPrivate] = useState(false)

  // Prevent duplicate requests with ref
  const hasFetched = useRef(false)

  const isOwner = user && wishlist && wishlist.userId === user.id

  useEffect(() => {
    // Reset fetch flag when wishlistId changes
    hasFetched.current = false
    fetchData()
  }, [wishlistId])

  const fetchData = async () => {
    if (hasFetched.current) return // Prevent duplicate fetch
    hasFetched.current = true

    try {
      // Fetch wishlist and items in parallel
      const [wishlistResponse, itemsResponse] = await Promise.all([
        wishlistApi.getWishlist(wishlistId),
        wishlistItemApi.getWishlistItems(wishlistId),
      ])

      setWishlist(wishlistResponse.data)
      setItems(itemsResponse.data.wishListItems || [])
      setIsPrivate(false)
    } catch (error) {
      // Check if it's a private wishlist (403 or 401 for anonymous user)
      if (error.response?.status === 403 || 
          (error.response?.status === 401 && !user)) {
        setIsPrivate(true)
      } else {
        toast.error('Failed to load wishlist')
        navigate('/wishlists')
      }
    } finally {
      setLoading(false)
    }
  }

  const handleItemCreated = () => {
    setShowCreate(false)
    // Refresh items only
    wishlistItemApi.getWishlistItems(wishlistId)
      .then(response => setItems(response.data.wishListItems || []))
      .catch(() => toast.error('Failed to refresh items'))
  }

  const handleItemDeleted = (itemId) => {
    setItems(items.filter(item => item.id !== itemId))
  }

  const handleItemUpdated = () => {
    wishlistItemApi.getWishlistItems(wishlistId)
      .then(response => setItems(response.data.wishListItems || []))
      .catch(() => toast.error('Failed to refresh items'))
  }

  const handleWishlistUpdated = () => {
    setShowEdit(false)
    wishlistApi.getWishlist(wishlistId)
      .then(response => setWishlist(response.data))
      .catch(() => toast.error('Failed to refresh wishlist'))
  }

  if (loading) return <Loading />
  if (isPrivate) return <PrivateWishlistMessage />
  if (!wishlist) return null

  return (
    <div className="page-container">
      {/* Back Button */}
      <button
        onClick={() => navigate(-1)}
        className="flex items-center gap-2 text-gray-600 hover:text-gray-900 mb-6 transition-colors"
      >
        <ArrowLeft className="w-5 h-5" />
        <span>Back</span>
      </button>

      {/* Wishlist Header */}
      <div className="card mb-8">
        <div className="flex flex-col md:flex-row gap-6">
          {wishlist.imageUrl ? (
            <div className="w-full md:w-64 aspect-video rounded-lg overflow-hidden bg-gray-100 flex-shrink-0">
              <img
                src={getDeviceSpecificImage(wishlist.imageUrl)}
                alt={wishlist.title}
                className="w-full h-full object-cover"
              />
            </div>
          ) : (
            <div className="w-full md:w-64 aspect-video rounded-lg bg-gradient-to-br from-primary-100 to-primary-200 flex items-center justify-center flex-shrink-0">
              <span className="text-5xl font-bold text-primary-600">
                {wishlist.title[0].toUpperCase()}
              </span>
            </div>
          )}
          
          <div className="flex-1">
            <div className="flex items-start justify-between mb-4">
              <div>
                <h1 className="text-3xl font-bold text-gray-900 mb-2">
                  {wishlist.title}
                </h1>
                <div className="flex items-center gap-2 text-sm text-gray-600">
                  {wishlist.isPublic ? (
                    <>
                      <Globe className="w-4 h-4" />
                      <span>Public wishlist</span>
                    </>
                  ) : (
                    <>
                      <Lock className="w-4 h-4" />
                      <span>Private wishlist</span>
                    </>
                  )}
                </div>
              </div>
              <div className="flex gap-2">
                {isOwner && (
                  <>
                    <button
                      onClick={() => setShowEdit(true)}
                      className="btn-secondary flex items-center gap-2"
                      title="Edit wishlist"
                    >
                      <Edit2 className="w-5 h-5" />
                      <span className="hidden sm:inline">Edit</span>
                    </button>
                    <button
                      onClick={() => setShowCreate(true)}
                      className="btn-primary flex items-center gap-2"
                    >
                      <Plus className="w-5 h-5" />
                      <span className="hidden sm:inline">Add Item</span>
                    </button>
                  </>
                )}
              </div>
            </div>
            <p className="text-gray-600">
              {items.length} {items.length === 1 ? 'item' : 'items'}
            </p>
          </div>
        </div>
      </div>

      {/* Items Grid */}
      <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
        {/* Empty state card (show first if owner) */}
        {isOwner && (
          <EmptyItemCard onClick={() => setShowCreate(true)} />
        )}
        
        {/* Items */}
        {items.map(item => (
          <WishlistItemCard
            key={item.id}
            item={item}
            wishlistId={wishlistId}
            isOwner={isOwner}
            onDelete={handleItemDeleted}
            onUpdate={handleItemUpdated}
          />
        ))}
      </div>

      {/* Create Item Modal */}
      {showCreate && (
        <CreateWishlistItem
          wishlistId={wishlistId}
          onClose={() => setShowCreate(false)}
          onCreated={handleItemCreated}
        />
      )}

      {/* Edit Wishlist Modal */}
      {showEdit && (
        <EditWishlist
          wishlist={wishlist}
          onClose={() => setShowEdit(false)}
          onUpdated={handleWishlistUpdated}
        />
      )}
    </div>
  )
}

export default WishlistDetail