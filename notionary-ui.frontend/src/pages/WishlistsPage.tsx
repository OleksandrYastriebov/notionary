import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Plus, Gift } from 'lucide-react';
import { useWishlists } from '../hooks/useWishlists';
import { useAuth } from '../hooks/useAuth';
import { Layout } from '../components/layout/Layout';
import { WishlistCard } from '../components/wishlist/WishlistCard';
import { WishlistModal } from '../components/wishlist/WishlistModal';
import { ShareModal } from '../components/wishlist/ShareModal';
import { WishlistCardSkeleton } from '../components/ui/SkeletonLoader';
import { EmptyState } from '../components/ui/EmptyState';
import { Button } from '../components/ui/Button';
import type { WishListDto } from '../types';

const MAX_WISHLISTS = 50;

export default function WishlistsPage() {
  const { user } = useAuth();
  const { data, isLoading, isError } = useWishlists();
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [editWishlist, setEditWishlist] = useState<WishListDto | null>(null);
  const [shareWishlist, setShareWishlist] = useState<WishListDto | null>(null);

  const wishlists = data?.wishLists ?? [];
  const atLimit = wishlists.length >= MAX_WISHLISTS;

  return (
    <Layout>
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">
            {user?.firstName ? `${user.firstName}'s Wishlists` : 'My Wishlists'}
          </h1>
          {wishlists.length > 0 && (
            <p className="text-sm text-gray-500 mt-0.5">
              {wishlists.length} / {MAX_WISHLISTS} wishlists
            </p>
          )}
        </div>

        {!atLimit && (
          <Button
            onClick={() => setIsCreateOpen(true)}
            leftIcon={<Plus size={16} />}
          >
            New wishlist
          </Button>
        )}
      </div>

      {/* Content */}
      {isLoading ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {Array.from({ length: 6 }).map((_, i) => (
            <WishlistCardSkeleton key={i} />
          ))}
        </div>
      ) : isError ? (
        <div className="text-center py-16">
          <p className="text-gray-500">Failed to load wishlists. Please refresh.</p>
        </div>
      ) : wishlists.length === 0 ? (
        <EmptyState
          icon={<Gift size={28} />}
          title="No wishlists yet"
          description="Create your first wishlist and start adding items you'd love to receive."
          action={
            <Button onClick={() => setIsCreateOpen(true)} leftIcon={<Plus size={15} />}>
              Create wishlist
            </Button>
          }
        />
      ) : (
        <motion.div
          layout
          className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4"
        >
          <AnimatePresence>
            {wishlists.map((wl) => (
              <WishlistCard
                key={wl.id}
                wishlist={wl}
                isOwner={wl.userId === user?.id}
                onEdit={(w) => setEditWishlist(w)}
                onShare={(w) => setShareWishlist(w)}
              />
            ))}
          </AnimatePresence>
          {!atLimit && (
            <button
              onClick={() => setIsCreateOpen(true)}
              className="rounded-2xl border-2 border-dashed border-gray-200 hover:border-violet-400 hover:bg-violet-50 transition-all duration-200 flex flex-col items-center justify-center gap-2 text-gray-400 hover:text-violet-500 min-h-[248px]"
            >
              <Plus size={28} strokeWidth={1.5} />
              <span className="text-sm font-medium">Add wishlist</span>
            </button>
          )}
        </motion.div>
      )}

      {atLimit && (
        <p className="text-sm text-amber-600 text-center mt-4">
          You&apos;ve reached the maximum of {MAX_WISHLISTS} wishlists.
        </p>
      )}

      {/* Modals */}
      <WishlistModal isOpen={isCreateOpen} onClose={() => setIsCreateOpen(false)} />
      <WishlistModal
        isOpen={!!editWishlist}
        onClose={() => setEditWishlist(null)}
        editWishlist={editWishlist}
      />
      {shareWishlist && (
        <ShareModal
          isOpen={!!shareWishlist}
          onClose={() => setShareWishlist(null)}
          wishlistId={shareWishlist.id}
          wishlistTitle={shareWishlist.title}
        />
      )}
    </Layout>
  );
}
