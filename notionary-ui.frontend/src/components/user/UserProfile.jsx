import { useState, useEffect } from 'react'
import { userApi } from '../../api/userApi'
import { useAuth } from '../../context/AuthContext'
import { User, Mail, Calendar, Edit2, Trash2 } from 'lucide-react'
import Loading from '../common/Loading'
import EditProfile from './EditProfile'
import toast from 'react-hot-toast'
import { useNavigate } from 'react-router-dom'

const UserProfile = () => {
  const { user, signOut } = useAuth()
  const navigate = useNavigate()
  const [profile, setProfile] = useState(null)
  const [loading, setLoading] = useState(true)
  const [isEditing, setIsEditing] = useState(false)
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false)
  const [avatarError, setAvatarError] = useState(false)

  useEffect(() => {
    fetchProfile()
  }, [])

  const fetchProfile = async () => {
    try {
      const response = await userApi.getCurrentUser()
      setProfile(response.data)
    } catch (error) {
      toast.error('Failed to load profile')
    } finally {
      setLoading(false)
    }
  }

  const handleDeleteAccount = async () => {
    try {
      await userApi.deleteUser(user.id)
      toast.success('Account deleted successfully')
      await signOut()
      navigate('/')
    } catch (error) {
      toast.error('Failed to delete account')
    }
    setShowDeleteConfirm(false)
  }

  if (loading) return <Loading />

  if (isEditing) {
    return <EditProfile profile={profile} onClose={() => { setIsEditing(false); fetchProfile() }} />
  }

  return (
    <div className="page-container">
      <div className="max-w-3xl mx-auto">
        <div className="card">
          {/* Header */}
          <div className="flex items-start justify-between mb-6">
            <div className="flex items-center space-x-4">
              {/* Avatar with fallback */}
              {profile.avatarUrl && !avatarError ? (
                <img 
                  src={profile.avatarUrl} 
                  alt={profile.firstName}
                  className="w-20 h-20 rounded-full object-cover border-2 border-gray-200"
                  onError={() => setAvatarError(true)}
                />
              ) : (
                <div className="w-20 h-20 bg-gradient-to-br from-primary-400 to-primary-600 rounded-full flex items-center justify-center border-2 border-primary-200">
                  <span className="text-white font-bold text-2xl">
                    {profile.firstName[0]}{profile.lastName[0]}
                  </span>
                </div>
              )}
              <div>
                <h1 className="text-2xl font-bold text-gray-900">
                  {profile.firstName} {profile.lastName}
                </h1>
                <p className="text-gray-600">{profile.email}</p>
              </div>
            </div>
            <button
              onClick={() => setIsEditing(true)}
              className="btn-secondary flex items-center gap-2"
            >
              <Edit2 className="w-4 h-4" />
              Edit Profile
            </button>
          </div>

          {/* Profile Info */}
          <div className="space-y-4 border-t border-gray-200 pt-6">
            <div className="flex items-center space-x-3 text-gray-700">
              <Mail className="w-5 h-5 text-gray-400" />
              <span>{profile.email}</span>
            </div>
            <div className="flex items-center space-x-3 text-gray-700">
              <Calendar className="w-5 h-5 text-gray-400" />
              <span>Joined {new Date(profile.createdAt).toLocaleDateString()}</span>
            </div>
          </div>

          {/* Danger Zone */}
          <div className="border-t border-gray-200 mt-8 pt-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-2">Danger Zone</h3>
            <p className="text-sm text-gray-600 mb-4">
              Once you delete your account, there is no going back. All your wishlists and data will be permanently deleted.
            </p>
            <button
              onClick={() => setShowDeleteConfirm(true)}
              className="btn-danger flex items-center gap-2"
            >
              <Trash2 className="w-4 h-4" />
              Delete Account
            </button>
          </div>
        </div>

        {/* Delete Confirmation Modal */}
        {showDeleteConfirm && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-lg p-6 max-w-md w-full">
              <h3 className="text-lg font-bold text-gray-900 mb-2">Confirm Account Deletion</h3>
              <p className="text-gray-600 mb-6">
                Are you absolutely sure? This action cannot be undone.
              </p>
              <div className="flex gap-3">
                <button
                  onClick={handleDeleteAccount}
                  className="flex-1 btn-danger"
                >
                  Yes, Delete My Account
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
      </div>
    </div>
  )
}

export default UserProfile