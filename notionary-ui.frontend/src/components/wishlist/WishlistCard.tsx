import { useState } from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Globe, Lock, Pencil, Trash2, ChevronRight, UserPlus } from 'lucide-react';
import type { WishListDto } from '../../types';
import { ImageFallback } from '../ui/ImageFallback';
import { ConfirmModal } from '../ui/ConfirmModal';
import { useDeleteWishlist } from '../../hooks/useWishlists';

interface WishlistCardProps {
  wishlist: WishListDto;
  isOwner: boolean;
  onEdit: (wishlist: WishListDto) => void;
  onShare: (wishlist: WishListDto) => void;
}

export function WishlistCard({ wishlist, isOwner, onEdit, onShare }: WishlistCardProps) {
  const [confirmDelete, setConfirmDelete] = useState(false);
  const deleteMutation = useDeleteWishlist();

  const handleDelete = () => {
    deleteMutation.mutate(wishlist.id, {
      onSuccess: () => setConfirmDelete(false),
    });
  };

  const itemCount = wishlist.wishListItems.length;
  const checkedCount = wishlist.wishListItems.filter((i) => i.isChecked).length;

  return (
    <>
      <motion.div
        layout
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        exit={{ opacity: 0, scale: 0.95 }}
        transition={{ duration: 0.25 }}
        className="group bg-white rounded-2xl overflow-hidden border border-gray-100 shadow-sm hover:shadow-md hover:-translate-y-0.5 transition-all duration-200"
      >
        {/* Cover image */}
        <Link to={`/wishlists/${wishlist.id}`} className="block">
          <ImageFallback
            src={wishlist.imageUrl}
            alt={wishlist.title}
            initials={wishlist.title.slice(0, 2).toUpperCase()}
            className="w-full h-44"
          />
        </Link>

        <div className="p-4">
          <div className="flex items-start justify-between gap-2">
            <Link
              to={`/wishlists/${wishlist.id}`}
              className="flex-1 min-w-0 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-violet-500 rounded"
            >
              <h3 className="font-semibold text-gray-900 truncate hover:text-violet-600 transition-colors flex items-center gap-1">
                {wishlist.title}
                <ChevronRight size={14} className="text-gray-400 flex-shrink-0 opacity-0 group-hover:opacity-100 transition-opacity" />
              </h3>
            </Link>

            {isOwner && (
              <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                {!wishlist.isPublic && (
                  <button
                    onClick={() => onShare(wishlist)}
                    className="p-1.5 rounded-lg text-gray-400 hover:text-violet-600 hover:bg-violet-50 active:bg-violet-100 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-violet-500"
                    aria-label="Share wishlist"
                  >
                    <UserPlus size={14} />
                  </button>
                )}
                <button
                  onClick={() => onEdit(wishlist)}
                  className="p-1.5 rounded-lg text-gray-400 hover:text-gray-700 hover:bg-gray-100 active:bg-gray-200 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-violet-500"
                  aria-label="Edit wishlist"
                >
                  <Pencil size={14} />
                </button>
                <button
                  onClick={() => setConfirmDelete(true)}
                  className="p-1.5 rounded-lg text-gray-400 hover:text-red-600 hover:bg-red-50 active:bg-red-100 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-red-400"
                  aria-label="Delete wishlist"
                >
                  <Trash2 size={14} />
                </button>
              </div>
            )}
          </div>

          <div className="flex items-center gap-3 mt-2">
            <span className="flex items-center gap-1 text-xs text-gray-500">
              {wishlist.isPublic ? (
                <Globe size={12} className="text-emerald-500" />
              ) : (
                <Lock size={12} className="text-gray-400" />
              )}
              {wishlist.isPublic ? 'Public' : 'Private'}
            </span>
            <span className="text-xs text-gray-400">
              {itemCount} {itemCount === 1 ? 'item' : 'items'}
            </span>
            {itemCount > 0 && (
              <span className="text-xs text-gray-400">
                {checkedCount} reserved
              </span>
            )}
          </div>
        </div>
      </motion.div>

      <ConfirmModal
        isOpen={confirmDelete}
        onClose={() => setConfirmDelete(false)}
        onConfirm={handleDelete}
        title="Delete wishlist"
        message={`Are you sure you want to delete "${wishlist.title}"? This action cannot be undone.`}
        confirmLabel="Delete"
        isDestructive
        isLoading={deleteMutation.isPending}
      />
    </>
  );
}
