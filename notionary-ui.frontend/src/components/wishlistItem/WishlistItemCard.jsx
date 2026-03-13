import { useState } from 'react'
import { MoreVertical, Edit2, Trash2, ExternalLink, Check, Package } from 'lucide-react'
import { wishlistItemApi } from '../../api/wishlistItemApi'
import { getResponsiveImageUrl } from '../../utils/cloudinary'
import EditWishlistItem from './EditWishlistItem'
import toast from 'react-hot-toast'

const WishlistItemCard = ({ item, wishlistId, isOwner, onDelete, onUpdate }) => {
  const [showMenu, setShowMenu] = useState(false)
  const [showEdit, setShowEdit] = useState(false)
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false)
  const [deleting, setDeleting] = useState(false)
  const [isChecked, setIsChecked] = useState(item.isChecked)
  const [updating, setUpdating] = useState(false)
  const [imageError, setImageError] = useState(false)

  const handleToggleCheck = async () => {
    if (updating) return
    
    setUpdating(true)
    const newCheckedState = !isChecked
    
    try {
      await wishlistItemApi.toggleItemCheck(wishlistId, item.id, {
        isChecked: newCheckedState
      })
      setIsChecked(newCheckedState)
    } catch (error) {
      setIsChecked(!newCheckedState)
    } finally {
      setUpdating(false)
    }
  }

  const handleDelete = async () => {
    setDeleting(true)
    try {
      await wishlistItemApi.deleteWishlistItem(wishlistId, item.id)
      onDelete(item.id)
      toast.success('Item deleted successfully')
    } catch (error) {
      // Error handled by interceptor
    } finally {
      setDeleting(false)
      setShowDeleteConfirm(false)
    }
  }

  return (
    <>
      <div className={`card hover:shadow-lg transition-all duration-200 relative ${isChecked ? 'opacity-60' : ''}`}>
        {/* Checkmark & Menu - MORE VISIBLE */}
        <div className="absolute top-3 right-3 z-10 flex gap-2">
          {/* Check Button */}
          <button
            onClick={handleToggleCheck}
            disabled={updating}
            className={`p-2.5 rounded-full transition-all shadow-md ${
              isChecked 
                ? 'bg-green-500 text-white hover:bg-green-600' 
                : 'bg-white text-gray-400 hover:bg-gray-100 border border-gray-300'
            }`}
            title={isChecked ? 'Mark as needed' : 'Mark as purchased'}
          >
            <Check className="w-5 h-5" />
          </button>

          {/* Menu (Owner Only) - MORE VISIBLE */}
          {isOwner && (
            <div className="relative">
              <button
                onClick={() => setShowMenu(!showMenu)}
                className="p-2.5 bg-white hover:bg-gray-100 rounded-full transition-colors shadow-md border border-gray-300"
              >
                <MoreVertical className="w-5 h-5 text-gray-700" />
              </button>

              {showMenu && (
                <>
                  <div 
                    className="fixed inset-0" 
                    onClick={() => setShowMenu(false)}
                  />
                  <div className="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border border-gray-200 py-1 z-20">
                    <button
                      onClick={() => {
                        setShowEdit(true)
                        setShowMenu(false)
                      }}
                      className="w-full px-4 py-2 text-left hover:bg-gray-50 flex items-center gap-2 text-gray-700"
                    >
                      <Edit2 className="w-4 h-4" />
                      Edit Item
                    </button>
                    <button
                      onClick={() => {
                        setShowDeleteConfirm(true)
                        setShowMenu(false)
                      }}
                      className="w-full px-4 py-2 text-left hover:bg-red-50 flex items-center gap-2 text-red-600"
                    >
                      <Trash2 className="w-4 h-4" />
                      Delete Item
                    </button>
                  </div>
                </>
              )}
            </div>
          )}
        </div>

        {/* Image with fallback */}
        {item.imageUrl && !imageError ? (
          <div className="aspect-square rounded-lg overflow-hidden mb-4 bg-gray-100">
            <img
              src={getResponsiveImageUrl(item.imageUrl, 'medium')}
              alt={item.title}
              className="w-full h-full object-cover"
              onError={() => setImageError(true)}
            />
          </div>
        ) : (
          <div className="aspect-square rounded-lg bg-gradient-to-br from-gray-100 to-gray-200 flex flex-col items-center justify-center mb-4">
            <Package className="w-16 h-16 text-gray-400 mb-2" />
            <span className="text-3xl font-bold text-gray-500">
              {item.title[0].toUpperCase()}
            </span>
          </div>
        )}

        {/* Content */}
        <div className={isChecked ? 'line-through text-gray-500' : ''}>
          <h3 className="text-lg font-semibold text-gray-900 mb-2 pr-20">
            {item.title}
          </h3>

          {item.price && (
            <p className="text-xl font-bold text-primary-600 mb-2">
              ${Number(item.price).toFixed(2)}
            </p>
          )}

          {item.description && (
            <p className="text-sm text-gray-600 mb-3 line-clamp-2">
              {item.description}
            </p>
          )}

          {item.url && (
            <a
              href={item.url}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-1 text-sm text-primary-600 hover:text-primary-700 transition-colors"
            >
              <ExternalLink className="w-4 h-4" />
              <span>View Product</span>
            </a>
          )}
        </div>
      </div>

      {/* Edit Modal */}
      {showEdit && (
        <EditWishlistItem
          item={item}
          wishlistId={wishlistId}
          onClose={() => setShowEdit(false)}
          onUpdated={() => {
            setShowEdit(false)
            onUpdate()
          }}
        />
      )}

      {/* Delete Confirmation */}
      {showDeleteConfirm && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg p-6 max-w-md w-full">
            <h3 className="text-lg font-bold text-gray-900 mb-2">Delete Item?</h3>
            <p className="text-gray-600 mb-6">
              This will permanently delete "{item.title}". This action cannot be undone.
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

export default WishlistItemCard