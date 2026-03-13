import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { userApi } from '../../api/userApi'
import { validators } from '../../utils/validation'
import { X, Upload, AlertCircle } from 'lucide-react'
import ImageUpload from '../common/ImageUpload'
import toast from 'react-hot-toast'

const EditProfile = ({ profile, onClose }) => {
  const [isLoading, setIsLoading] = useState(false)
  const [showImageUpload, setShowImageUpload] = useState(false)
  
  const { register, handleSubmit, setValue, formState: { errors } } = useForm({
    defaultValues: {
      firstName: profile.firstName,
      lastName: profile.lastName,
      avatarUrl: profile.avatarUrl || ''
    }
  })

  const onSubmit = async (data) => {
    setIsLoading(true)
    try {
      // Only send fields that have values
      const updateData = {}
      if (data.firstName) updateData.firstName = data.firstName
      if (data.lastName) updateData.lastName = data.lastName
      if (data.avatarUrl) updateData.avatarUrl = data.avatarUrl

      await userApi.updateUser(updateData)
      toast.success('Profile updated successfully')
      onClose()
    } catch (error) {
      // Error handled by interceptor
    } finally {
      setIsLoading(false)
    }
  }

  const handleImageUpload = (imageUrl) => {
    setValue('avatarUrl', imageUrl)
    setShowImageUpload(false)
    toast.success('Avatar uploaded successfully')
  }

  return (
    <div className="page-container">
      <div className="max-w-2xl mx-auto">
        <div className="card">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-2xl font-bold text-gray-900">Edit Profile</h2>
            <button onClick={onClose} className="text-gray-400 hover:text-gray-600">
              <X className="w-6 h-6" />
            </button>
          </div>

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
            {/* Avatar */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Profile Picture
              </label>
              <div className="flex items-center gap-4">
                {profile.avatarUrl && (
                  <img 
                    src={profile.avatarUrl}
                    alt="Avatar"
                    className="w-20 h-20 rounded-full object-cover"
                  />
                )}
                <button
                  type="button"
                  onClick={() => setShowImageUpload(true)}
                  className="btn-secondary flex items-center gap-2"
                >
                  <Upload className="w-4 h-4" />
                  Upload New Picture
                </button>
              </div>
            </div>

            {/* First Name */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                First Name
              </label>
              <input
                {...register('firstName', { validate: validators.firstName })}
                className={`input-field ${errors.firstName ? 'border-red-500' : ''}`}
              />
              {errors.firstName && (
                <p className="mt-1 text-sm text-red-600 flex items-center gap-1">
                  <AlertCircle className="w-4 h-4" />
                  {errors.firstName.message}
                </p>
              )}
            </div>

            {/* Last Name */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Last Name
              </label>
              <input
                {...register('lastName', { validate: validators.lastName })}
                className={`input-field ${errors.lastName ? 'border-red-500' : ''}`}
              />
              {errors.lastName && (
                <p className="mt-1 text-sm text-red-600 flex items-center gap-1">
                  <AlertCircle className="w-4 h-4" />
                  {errors.lastName.message}
                </p>
              )}
            </div>

            {/* Email (Read-only) */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Email
              </label>
              <input
                type="email"
                value={profile.email}
                disabled
                className="input-field bg-gray-100 cursor-not-allowed"
              />
              <p className="mt-1 text-xs text-gray-500">Email cannot be changed</p>
            </div>

            {/* Actions */}
            <div className="flex gap-3">
              <button
                type="submit"
                disabled={isLoading}
                className="flex-1 btn-primary"
              >
                {isLoading ? 'Saving...' : 'Save Changes'}
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
      </div>

      {/* Image Upload Modal */}
      {showImageUpload && (
        <ImageUpload
          onImageUploaded={handleImageUpload}
          onClose={() => setShowImageUpload(false)}
        />
      )}
    </div>
  )
}

export default EditProfile