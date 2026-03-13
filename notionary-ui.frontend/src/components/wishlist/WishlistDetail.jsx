import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { wishlistApi } from '../../api/wishlistApi'
import { wishlistItemApi } from '../../api/wishlistItemApi'
import { useAuth } from '../../context/AuthContext'
import { ArrowLeft, Plus, Globe, Lock } from 'lucide-react'
import { getDeviceSpecificImage } from '../../utils/cloudinary'
import WishlistItemCard from '../wishlistItem/WishlistItemCard'
import CreateWishlistItem from '../wishlistItem/CreateWishlistItem'
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

  const isOwner = user && wishlist && wishlist.userId === user.id

  useEffect(() => {
    fetchWishlist()
    fetchItems()
  }, [wishlistId])

  const fetchWishlist = async () => {
    try {
      const response = await wishlistApi.getWishlist(wishlistId)
      setWishlist(response.data)
    } catch (error) {
      toast.error('Failed to load wishlist')
      navigate('/wishlists')
    }
  }

  const fetchItems = async () => {
    try {
      const response = await wishlistItemApi.getWishlistItems(wishlistId)
      setItems(response.data.wishListItems || [])
    } catch (error) {
      toast.error('Failed to load items')
    } finally {
      setLoading(false)
    }
  }

  const handleItemCreated = () => {
    setShowCreate(false)
    fetchItems()
  }

  const handleItemDeleted = (itemId) => {
    setItems(items.filter(item => item.id !== itemId))
  }

  const handleItemUpdated = () => {
    fetchItems()
  }

  if (loading) return <Loading />
  if (!wishlist) return null

  return (
    <div className="page-container">
      {/* Back Button */}
      <button
        onClick={() => navigate(-1)}
        className="flex items-center gap-2 text-gray-600 hover:text-gray-900 mb-6"
      >
        <ArrowLeft className="w-5 h-5" />
        <span>Back</span>
      </button>

      {/* Wishlist Header */}
      <div className="card mb-8">
        <div className="flex flex-col md:flex-row gap-6">
          {wishlist.imageUrl && (
            <div className="w-full md:w-64 aspect-video rounded-lg overflow-hidden bg-gray-100 flex-shrink-0">
              <img
                src={getDeviceSpecificImage(wishlist.imageUrl)}
                alt={wishlist.title}
                className="w-full h-full object-cover"
              />
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
              {isOwner && (
                <button
                  onClick={() => setShowCreate(true)}
                  className="btn-primary flex items-center gap-2"
                >
                  <Plus className="w-5 h-5" />
                  <span className="hidden sm:inline">Add Item</span>
                </button>
              )}
            </div>
            <p className="text-gray-600">
              {items.length} {items.length === 1 ? 'item' : 'items'}
            </p>
          </div>
        </div>
      </div>

      {/* Items Grid */}
      {items.length === 0 ? (
        <div className="text-center py-20 card">
          <div className="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <Plus className="w-10 h-10 text-gray-400" />
          </div>
          <h3 className="text-xl font-semibold text-gray-900 mb-2">No items yet</h3>
          <p className="text-gray-600 mb-6">
            {isOwner 
              ? 'Add your first item to this wishlist'
              : 'This wishlist is empty'}
          </p>
          {isOwner && (
            <button
              onClick={() => setShowCreate(true)}
              className="btn-primary"
            >
              Add First Item
            </button>
          )}
        </div>
      ) : (
        <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
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
      )}

      {/* Create Item Modal */}
      {showCreate && (
        <CreateWishlistItem
          wishlistId={wishlistId}
          onClose={() => setShowCreate(false)}
          onCreated={handleItemCreated}
        />
      )}
    </div>
  )
}

export default WishlistDetail