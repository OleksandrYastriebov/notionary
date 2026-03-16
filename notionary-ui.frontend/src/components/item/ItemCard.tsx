import { useState } from 'react';
import { motion } from 'framer-motion';
import toast from 'react-hot-toast';
import { ExternalLink, MessageCircle, Pencil, Trash2, CheckCircle2, Circle, DollarSign, Lock } from 'lucide-react';
import type { WishListItemDto } from '../../types';
import { ImageFallback } from '../ui/ImageFallback';
import { ConfirmModal } from '../ui/ConfirmModal';
import { useDeleteItem, useToggleChecked } from '../../hooks/useWishlistItems';
import { useIsOverflowing } from '../../hooks/useIsOverflowing';
import type { AxiosError } from 'axios';

interface ItemCardProps {
  item: WishListItemDto;
  wishlistId: string;
  isOwner: boolean;
  currentUserId: number | null;
  onEdit: (item: WishListItemDto) => void;
  onOpenComments: (item: WishListItemDto) => void;
  onRequireAuth: () => void;
}

export function ItemCard({
  item,
  wishlistId,
  isOwner,
  currentUserId,
  onEdit,
  onOpenComments,
  onRequireAuth,
}: ItemCardProps) {
  const [confirmDelete, setConfirmDelete] = useState(false);
  const deleteMutation = useDeleteItem(wishlistId);
  const toggleMutation = useToggleChecked(wishlistId);
  const { ref: titleRef, isOverflowing: titleOverflowing } = useIsOverflowing<HTMLDivElement>();

  const isReservedByOther =
    !isOwner &&
    item.isChecked &&
    item.checkedByUserId !== null &&
    item.checkedByUserId !== currentUserId;

  const handleToggle = () => {
    if (isReservedByOther) return;
    toggleMutation.mutate(
      { itemId: item.id, isChecked: !item.isChecked, currentUserId },
      {
        onError: (err) => {
          const axiosErr = err as AxiosError;
          if (axiosErr.response?.status === 403) {
            if (item.isChecked) {
              toast.error('This item is already reserved by someone else.');
            } else {
              onRequireAuth();
            }
          }
        },
      }
    );
  };

  const handleDelete = () => {
    deleteMutation.mutate(item.id, {
      onSuccess: () => setConfirmDelete(false),
    });
  };

  return (
    <>
      <motion.div
        layout
        initial={{ opacity: 0, y: 10 }}
        animate={{ opacity: 1, y: 0 }}
        exit={{ opacity: 0, scale: 0.95 }}
        transition={{ duration: 0.2 }}
        className={`group rounded-2xl border transition-all duration-200 overflow-hidden ${
          item.isChecked
            ? 'bg-gray-100 border-gray-200 shadow-none opacity-75 saturate-50'
            : 'bg-white border-gray-100 shadow-sm hover:shadow-md'
        }`}
      >
        <div className="flex gap-0">
          {/* Image */}
          <div className="flex-shrink-0 w-28 sm:w-36 h-[130px]">
            <ImageFallback
              src={item.imageUrl}
              alt={item.title}
              initials={item.title[0]?.toUpperCase()}
              className="w-full h-full"
            />
          </div>

          {/* Content */}
          <div className="flex-1 min-w-0 p-4 flex flex-col justify-between">
            <div>
              <div className="flex items-start justify-between gap-2 min-w-0">
                <div ref={titleRef} className="relative min-w-0 overflow-hidden" title={titleOverflowing ? item.title : undefined}>
                  <h3
                    className={`font-semibold text-sm sm:text-base whitespace-nowrap ${
                      item.isChecked ? 'line-through text-gray-400' : 'text-gray-900'
                    }`}
                  >
                    {item.title}
                  </h3>
                  {titleOverflowing && (
                    <div className={`absolute inset-y-0 right-0 w-10 bg-gradient-to-l to-transparent pointer-events-none ${
                      item.isChecked ? 'from-gray-100' : 'from-white'
                    }`} />
                  )}
                </div>

                {/* Actions */}
                {isOwner && (
                  <div className="flex items-center gap-1 flex-shrink-0 opacity-0 group-hover:opacity-100 transition-opacity">
                    <button
                      onClick={() => onEdit(item)}
                      className="p-1.5 rounded-lg text-gray-400 hover:text-gray-700 hover:bg-gray-100 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-violet-500"
                      aria-label="Edit item"
                    >
                      <Pencil size={13} />
                    </button>
                    <button
                      onClick={() => setConfirmDelete(true)}
                      className="p-1.5 rounded-lg text-gray-400 hover:text-red-600 hover:bg-red-50 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-red-400"
                      aria-label="Delete item"
                    >
                      <Trash2 size={13} />
                    </button>
                  </div>
                )}
              </div>

              {item.price !== null && (
                <div className="flex items-center gap-1 text-sm font-semibold text-violet-600 mt-1">
                  <DollarSign size={13} />
                  {item.price.toFixed(2)}
                </div>
              )}

              {item.description && (
                <p className="text-xs text-gray-500 mt-1 line-clamp-2">{item.description}</p>
              )}
            </div>

            {/* Footer actions */}
            <div className="flex items-center gap-3">
              <button
                onClick={handleToggle}
                disabled={isReservedByOther || toggleMutation.isPending}
                className={`flex items-center gap-1.5 text-xs font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 rounded focus-visible:ring-violet-400 disabled:opacity-50 ${
                  isReservedByOther
                    ? 'text-gray-400 cursor-not-allowed'
                    : item.isChecked
                    ? 'text-emerald-600 hover:text-emerald-700'
                    : 'text-gray-500 hover:text-emerald-600'
                }`}
              >
                {isReservedByOther ? (
                  <Lock size={14} className="text-gray-400" />
                ) : item.isChecked ? (
                  <CheckCircle2 size={14} className="text-emerald-500" />
                ) : (
                  <Circle size={14} />
                )}
                {item.isChecked ? 'Reserved' : 'Mark as reserved'}
              </button>

              {item.url && (
                <a
                  href={item.url}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="flex items-center gap-1 text-xs text-violet-600 hover:text-violet-700 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-violet-400 rounded"
                >
                  <ExternalLink size={12} />
                  View
                </a>
              )}

              {!isOwner && (
                <button
                  onClick={() => onOpenComments(item)}
                  className="flex items-center gap-1 text-xs text-gray-500 hover:text-violet-600 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-violet-400 rounded ml-auto"
                >
                  <MessageCircle size={13} />
                  Discuss
                </button>
              )}
            </div>
          </div>
        </div>
      </motion.div>

      <ConfirmModal
        isOpen={confirmDelete}
        onClose={() => setConfirmDelete(false)}
        onConfirm={handleDelete}
        title="Delete item"
        message={`Remove "${item.title}" from this wishlist?`}
        confirmLabel="Delete"
        isDestructive
        isLoading={deleteMutation.isPending}
      />
    </>
  );
}
