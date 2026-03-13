import { useState, useRef } from 'react'
import { imageApi } from '../../api/imageApi'
import { X, Upload, Loader2, Image as ImageIcon } from 'lucide-react'
import toast from 'react-hot-toast'

const ImageUpload = ({ onImageUploaded, onClose, currentImage = null }) => {
  const [uploading, setUploading] = useState(false)
  const [preview, setPreview] = useState(currentImage)
  const fileInputRef = useRef(null)

  const handleFileSelect = async (e) => {
    const selectedFile = e.target.files[0]
    if (!selectedFile) return

    // Validate file type
    if (!selectedFile.type.startsWith('image/')) {
      toast.error('Please select an image file')
      return
    }

    // Validate file size (max 5MB)
    if (selectedFile.size > 5 * 1024 * 1024) {
      toast.error('Image must be less than 5MB')
      return
    }

    // Show preview
    setPreview(URL.createObjectURL(selectedFile))

    // Upload immediately
    setUploading(true)
    try {
      const response = await imageApi.uploadImage(selectedFile)
      onImageUploaded(response.data.url)
      toast.success('Image uploaded successfully')
    } catch (error) {
      toast.error('Failed to upload image')
      setPreview(currentImage) // Revert to original
    } finally {
      setUploading(false)
    }
  }

  const handleRemoveImage = () => {
    setPreview(null)
    onImageUploaded(null) // Clear image
    toast.success('Image removed')
  }

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg p-6 max-w-md w-full">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-lg font-bold text-gray-900">
            {preview ? 'Change Image' : 'Upload Image'}
          </h3>
          <button 
            onClick={onClose} 
            className="text-gray-400 hover:text-gray-600"
            disabled={uploading}
          >
            <X className="w-6 h-6" />
          </button>
        </div>

        <div className="space-y-4">
          {/* Preview */}
          {preview ? (
            <div className="relative">
              <div className="aspect-square rounded-lg overflow-hidden bg-gray-100">
                <img 
                  src={preview} 
                  alt="Preview" 
                  className="w-full h-full object-cover"
                />
              </div>
              {!uploading && (
                <button
                  onClick={handleRemoveImage}
                  className="absolute top-2 right-2 bg-red-600 text-white p-2 rounded-full hover:bg-red-700 transition-colors shadow-lg"
                  title="Remove image"
                >
                  <X className="w-4 h-4" />
                </button>
              )}
            </div>
          ) : (
            <div className="aspect-square rounded-lg bg-gradient-to-br from-gray-100 to-gray-200 flex flex-col items-center justify-center border-2 border-dashed border-gray-300">
              <ImageIcon className="w-16 h-16 text-gray-400 mb-2" />
              <p className="text-sm text-gray-500">No image selected</p>
            </div>
          )}

          {/* File Input */}
          <input
            ref={fileInputRef}
            type="file"
            accept="image/*"
            onChange={handleFileSelect}
            className="hidden"
            disabled={uploading}
          />

          {/* Upload/Change Button */}
          <button
            onClick={() => fileInputRef.current?.click()}
            disabled={uploading}
            className="w-full btn-primary flex items-center justify-center gap-2"
          >
            {uploading ? (
              <>
                <Loader2 className="w-5 h-5 animate-spin" />
                Uploading...
              </>
            ) : (
              <>
                <Upload className="w-5 h-5" />
                {preview ? 'Change Image' : 'Choose Image'}
              </>
            )}
          </button>

          <p className="text-xs text-gray-500 text-center">
            Supported formats: JPG, PNG, GIF (max 5MB)
            <br />
            Image will be uploaded automatically after selection
          </p>
        </div>
      </div>
    </div>
  )
}

export default ImageUpload