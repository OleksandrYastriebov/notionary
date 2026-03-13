import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { wishlistApi } from '../../api/wishlistApi'
import { validators } from '../../utils/validation'
import { X, Upload, Globe, Lock, AlertCircle } from 'lucide-react'
import ImageUpload from '../common/ImageUpload'
import toast from 'react-hot-toast'

const CreateWishlist = ({ onClose, onCreated }) => {
  const [isLoading, setIsLoading] = useState(false)
  const [showImageUpload, setShowImageUpload] = useState(false)
  const [imageUrl, setImageUrl] = useState('')

  const { register, handleSubmit, formState: { errors } } = useForm({
    defaultValues: {
      title: '',
      isPublic: false,
    }
  })

  const onSubmit = async (data) => {
    setIsLoading(true)
    try {
      await wishlistApi.createWishlist({
        ...data,
        imageUrl: imageUrl || null
      })
      toast.success('Wishlist created successfully')
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
          <h2 className="text-2xl font-bold text-gray-900">Create Wishlist</h2>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600">
            <X className="w-6 h-6" />
          </button>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
          {/* Title */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Wishlist Title *
            </label>
            <input
              {...register('title', { validate: validators.title })}
              className={`input-field ${errors.title ? 'border-red-500' : ''}`}
              placeholder="e.g., Birthday 2024, Dream Gadgets"
            />
            {errors.title && (
              <p className="mt-1 text-sm text-red-600 flex items-center gap-1">
                <AlertCircle className="w-4 h-4" />
                {errors.title.message}
              </p>
            )}
          </div>

          {/* Image */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Cover Image (Optional)
            </label>
            {imageUrl ? (
              <div className="relative aspect-video rounded-lg overflow-hidden mb-2">
                <img src={imageUrl} alt="Cover" className="w-full h-full object-cover" />
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
                Upload Cover Image
              </button>
            )}
          </div>

          {/* Privacy */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Privacy
            </label>
            <div className="space-y-2">
              <label className="flex items-center gap-3 p-3 border border-gray-200 rounded-lg cursor-pointer hover:bg-gray-50">
                <input
                  {...register('isPublic')}
                  type="radio"
                  value="false"
                  className="w-4 h-4 text-primary-600"
                />
                <Lock className="w-5 h-5 text-gray-400" />
                <div>
                  <div className="font-medium text-gray-900">Private</div>
                  <div className="text-sm text-gray-600">Only you can see this wishlist</div>
                </div>
              </label>
              <label className="flex items-center gap-3 p-3 border border-gray-200 rounded-lg cursor-pointer hover:bg-gray-50">
                <input
                  {...register('isPublic')}
                  type="radio"
                  value="true"
                  className="w-4 h-4 text-primary-600"
                />
                <Globe className="w-5 h-5 text-gray-400" />
                <div>
                  <div className="font-medium text-gray-900">Public</div>
                  <div className="text-sm text-gray-600">Anyone with the link can view</div>
                </div>
              </label>
            </div>
          </div>

          {/* Actions */}
          <div className="flex gap-3 pt-4 border-t">
            <button
              type="submit"
              disabled={isLoading}
              className="flex-1 btn-primary"
            >
              {isLoading ? 'Creating...' : 'Create Wishlist'}
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

export default CreateWishlist