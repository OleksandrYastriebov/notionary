import { useState, useRef } from 'react'
import { imageApi } from '../../api/imageApi'
import { X, Upload, Loader2 } from 'lucide-react'
import toast from 'react-hot-toast'

const ImageUpload = ({ onImageUploaded, onClose }) => {
  const [uploading, setUploading] = useState(false)
  const [preview, setPreview] = useState(null)
  const [file, setFile] = useState(null)
  const fileInputRef = useRef(null)

  const handleFileSelect = (e) => {
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

    setFile(selectedFile)
    setPreview(URL.createObjectURL(selectedFile))
  }

  const handleUpload = async () => {
    if (!file) return

    setUploading(true)
    try {
      const response = await imageApi.uploadImage(file)
      onImageUploaded(response.data.url)
      toast.success('Image uploaded successfully')
    } catch (error) {
      toast.error('Failed to upload image')
    } finally {
      setUploading(false)
    }
  }

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg p-6 max-w-md w-full">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-lg font-bold text-gray-900">Upload Image</h3>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600">
            <X className="w-6 h-6" />
          </button>
        </div>

        <div className="space-y-4">
          {/* Preview */}
          {preview && (
            <div className="relative aspect-square rounded-lg overflow-hidden bg-gray-100">
              <img 
                src={preview} 
                alt="Preview" 
                className="w-full h-full object-cover"
              />
            </div>
          )}

          {/* File Input */}
          <input
            ref={fileInputRef}
            type="file"
            accept="image/*"
            onChange={handleFileSelect}
            className="hidden"
          />

          <button
            onClick={() => fileInputRef.current?.click()}
            className="w-full btn-secondary flex items-center justify-center gap-2"
            disabled={uploading}
          >
            <Upload className="w-5 h-5" />
            {file ? 'Choose Different Image' : 'Choose Image'}
          </button>

          {/* Upload Button */}
          <button
            onClick={handleUpload}
            disabled={!file || uploading}
            className="w-full btn-primary flex items-center justify-center gap-2"
          >
            {uploading ? (
              <>
                <Loader2 className="w-5 h-5 animate-spin" />
                Uploading...
              </>
            ) : (
              'Upload Image'
            )}
          </button>

          <p className="text-xs text-gray-500 text-center">
            Supported formats: JPG, PNG, GIF (max 5MB)
          </p>
        </div>
      </div>
    </div>
  )
}

export default ImageUpload