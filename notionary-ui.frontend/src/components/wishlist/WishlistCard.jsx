import { Link } from 'react-router-dom'
import { MoreVertical, Edit2, Trash2, Lock, Globe, Gift } from 'lucide-react'
import { useState } from 'react'
import { wishlistApi } from '../../api/wishlistApi'
import { getDeviceSpecificImage } from '../../utils/cloudinary'
import EditWishlist from './EditWishlist'

const WishlistCard = ({ wishlist, onDelete }) => {
  const [showMenu, setShowMenu] = useState(false)
  const [showEdit, setShowEdit] = useState(false)
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false)
  const [deleting, setDeleting] = useState(false)
  const [imageError, setImageError] = useState(false)

  const handleDelete = async () => {
    setDeleting(true)
    try {
      await wishlistApi.deleteWishlist(wishlist.id)
      onDelete(wishlist.id)
    } catch (error) {
      // Error handled by interceptor
    } finally {
      setDeleting(false)
      setShowDeleteConfirm(false)
    }
  }

  const itemCount = wishlist.wishListItems?.length || 0

  return (
    <>
      <div className="card hover:shadow-lg transition-shadow duration-200 relative">
        {/* Menu */}
        <div className="absolute top-4 right-4 z-10">
          <button
            onClick={(e) => {
              e.preventDefault()
              setShowMenu(!showMenu)
            }}
            className="p-2 bg-white hover:bg-gray-100 rounded-full transition-colors shadow-md"
          >
            <MoreVertical className="w-5 h-5 text-gray-600" />
          </button>

          {showMenu && (
            <>
              <div 
                className="fixed inset-0" 
                onClick={() => setShowMenu(false)}
              />
              <div className="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border border-gray-200 py-1">
                <button
                  onClick={(e) => {
                    e.preventDefault()
                    setShowEdit(true)
                    setShowMenu(false)
                  }}
                  className="w-full px-4 py-2 text-left hover:bg-gray-50 flex items-center gap-2 text-gray-700"
                >
                  <Edit2 className="w-4 h-4" />
                  Edit Wishlist
                </button>
                <button
                  onClick={(e) => {
                    e.preventDefault()
                    setShowDeleteConfirm(true)
                    setShowMenu(false)
                  }}
                  className="w-full px-4 py-2 text-left hover:bg-red-50 flex items-center gap-2 text-red-600"
                >
                  <Trash2 className="w-4 h-4" />
                  Delete Wishlist
                </button>
              </div>
            </>
          )}
        </div>

        <Link to={`/wishlists/${wishlist.id}`} className="block">
          {/* Image with fallback */}
          {wishlist.imageUrl && !imageError ? (
            <div className="aspect-video rounded-lg overflow-hidden mb-4 bg-gray-100">
              <img
                src={getDeviceSpecificImage(wishlist.imageUrl)}
                alt={wishlist.title}
                className="w-full h-full object-cover"
                onError={() => setImageError(true)}
              />
            </div>
          ) : (
            <div className="aspect-video rounded-lg bg-gradient-to-br from-primary-100 to-primary-200 flex flex-col items-center justify-center mb-4">
              <Gift className="w-12 h-12 text-primary-600 mb-2" />
              <span className="text-2xl font-bold text-primary-700">
                {wishlist.title[0].toUpperCase()}
              </span>
            </div>
          )}

          {/* Content */}
          <h3 className="text-lg font-semibold text-gray-900 mb-2 pr-8">
            {wishlist.title}
          </h3>

          <div className="flex items-center justify-between text-sm">
            <span className="text-gray-600">
              {itemCount} {itemCount === 1 ? 'item' : 'items'}
            </span>
            <div className="flex items-center gap-1 text-gray-500">
              {wishlist.isPublic ? (
                <>
                  <Globe className="w-4 h-4" />
                  <span>Public</span>
                </>
              ) : (
                <>
                  <Lock className="w-4 h-4" />
                  <span>Private</span>
                </>
              )}
            </div>
          </div>
        </Link>
      </div>

      {/* Edit Modal */}
      {showEdit && (
        <EditWishlist
          wishlist={wishlist}
          onClose={() => setShowEdit(false)}
          onUpdated={() => {
            setShowEdit(false)
            window.location.reload()
          }}
        />
      )}

      {/* Delete Confirmation */}
      {showDeleteConfirm && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg p-6 max-w-md w-full">
            <h3 className="text-lg font-bold text-gray-900 mb-2">Delete Wishlist?</h3>
            <p className="text-gray-600 mb-6">
              This will permanently delete "{wishlist.title}" and all its items. This action cannot be undone.
            </p>
            <div className="flex gap-3">
              <button
                onClick={handleDelete}
                disabled={deleting}
                className="flex-1 btn-danger"
              >
                {deleting ? 'Deleting...' : 'Delete'}
              </button>
              <button
                onClick={() => setShowDeleteConfirm(false)}
                className="flex-1 btn-secondary"
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  )
}

export default WishlistCard