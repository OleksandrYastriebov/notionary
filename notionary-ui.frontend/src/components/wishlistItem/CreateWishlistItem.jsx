import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { wishlistItemApi } from '../../api/wishlistItemApi'
import { validators } from '../../utils/validation'
import { X, Upload, AlertCircle } from 'lucide-react'
import ImageUpload from '../common/ImageUpload'
import toast from 'react-hot-toast'

const CreateWishlistItem = ({ wishlistId, onClose, onCreated }) => {
  const [isLoading, setIsLoading] = useState(false)
  const [showImageUpload, setShowImageUpload] = useState(false)
  const [imageUrl, setImageUrl] = useState('')

  const { register, handleSubmit, formState: { errors } } = useForm()

  const onSubmit = async (data) => {
    setIsLoading(true)
    try {
      await wishlistItemApi.createWishlistItem(wishlistId, {
        title: data.title,
        url: data.url || null,
        price: data.price ? Number(data.price) : null,
        description: data.description || null,
        imageUrl: imageUrl || null,
      })
      toast.success('Item added successfully')
      onCreated()
    } catch (error) {
      // Error handled by interceptor
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg p-6 max-w-md w-full max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-2xl font-bold text-gray-900">Add Item</h2>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600">
            <X className="w-6 h-6" />
          </button>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          {/* Title */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Item Title *
            </label>
            <input
              {...register('title', { validate: validators.title })}
              className={`input-field ${errors.title ? 'border-red-500' : ''}`}
              placeholder="e.g., Wireless Headphones"
            />
            {errors.title && (
              <p className="mt-1 text-sm text-red-600 flex items-center gap-1">
                <AlertCircle className="w-4 h-4" />
                {errors.title.message}
              </p>
            )}
          </div>

          {/* Price */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Price (Optional)
            </label>
            <div className="relative">
              <span className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500">$</span>
              <input
                {...register('price', { validate: validators.price })}
                type="number"
                step="0.01"
                min="0"
                className={`input-field pl-8 ${errors.price ? 'border-red-500' : ''}`}
                placeholder="0.00"
              />
            </div>
            {errors.price && (
              <p className="mt-1 text-sm text-red-600 flex items-center gap-1">
                <AlertCircle className="w-4 h-4" />
                {errors.price.message}
              </p>
            )}
          </div>

          {/* URL */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Product URL (Optional)
            </label>
            <input
              {...register('url', { validate: validators.url })}
              type="url"
              className={`input-field ${errors.url ? 'border-red-500' : ''}`}
              placeholder="https://example.com/product"
            />
            {errors.url && (
              <p className="mt-1 text-sm text-red-600 flex items-center gap-1">
                <AlertCircle className="w-4 h-4" />
                {errors.url.message}
              </p>
            )}
          </div>

          {/* Description */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Description (Optional)
            </label>
            <textarea
              {...register('description', { 
                validate: (value) => validators.description(value, 1000)
              })}
              rows={3}
              className={`input-field resize-none ${errors.description ? 'border-red-500' : ''}`}
              placeholder="Add details about this item..."
            />
            {errors.description && (
              <p className="mt-1 text-sm text-red-600 flex items-center gap-1">
                <AlertCircle className="w-4 h-4" />
                {errors.description.message}
              </p>
            )}
          </div>

          {/* Image */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Image (Optional)
            </label>
            {imageUrl ? (
              <div className="relative aspect-square rounded-lg overflow-hidden mb-2">
                <img src={imageUrl} alt="Item" className="w-full h-full object-cover" />
                <button
                  type="button"
                  onClick={() => setImageUrl('')}
                  className="absolute top-2 right-2 bg-red-600 text-white p-2 rounded-full hover:bg-red-700"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>
            ) : (
              <button
                type="button"
                onClick={() => setShowImageUpload(true)}
                className="w-full btn-secondary flex items-center justify-center gap-2"
              >
                <Upload className="w-5 h-5" />
                Upload Image
              </button>
            )}
          </div>

          {/* Actions */}
          <div className="flex gap-3 pt-4 border-t">
            <button
              type="submit"
              disabled={isLoading}
              className="flex-1 btn-primary"
            >
              {isLoading ? 'Adding...' : 'Add Item'}
            </button>
            <button
              type="button"
              onClick={onClose}
              className="flex-1 btn-secondary"
            >
              Cancel
            </button>
          </div>
        </form>
      </div>

      {/* Image Upload Modal */}
      {showImageUpload && (
        <ImageUpload
          onImageUploaded={(url) => {
            setImageUrl(url)
            setShowImageUpload(false)
          }}
          onClose={() => setShowImageUpload(false)}
        />
      )}
    </div>
  )
}

export default CreateWishlistItem