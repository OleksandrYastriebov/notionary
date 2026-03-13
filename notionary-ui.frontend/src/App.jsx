import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from './context/AuthContext'
import Navbar from './components/common/Navbar'
import SignIn from './components/auth/SignIn'
import SignUp from './components/auth/SignUp'
import EmailConfirmation from './components/auth/EmailConfirmation'
import UserProfile from './components/user/UserProfile'
import WishlistList from './components/wishlist/WishlistList'
import WishlistDetail from './components/wishlist/WishlistDetail'
import Landing from './pages/Landing'
import Loading from './components/common/Loading'

function App() {
  const { user, loading } = useAuth()

  if (loading) {
    return <Loading fullScreen />
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <Routes>
        {/* Public routes */}
        <Route path="/" element={<Landing />} />
        <Route path="/sign-in" element={!user ? <SignIn /> : <Navigate to="/wishlists" />} />
        <Route path="/sign-up" element={!user ? <SignUp /> : <Navigate to="/wishlists" />} />
        <Route path="/confirm-email" element={<EmailConfirmation />} />
        
        {/* Public wishlist view */}
        <Route path="/wishlists/:wishlistId" element={<WishlistDetail />} />
        
        {/* Protected routes */}
        <Route 
          path="/profile" 
          element={user ? <UserProfile /> : <Navigate to="/sign-in" />} 
        />
        <Route 
          path="/wishlists" 
          element={user ? <WishlistList /> : <Navigate to="/sign-in" />} 
        />
        
        {/* Catch all */}
        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
    </div>
  )
}

export default App