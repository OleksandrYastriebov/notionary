import { useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { Plus, Globe, Lock, ArrowLeft, UserPlus, Package, LogIn, UserPlus as UserPlusIcon } from 'lucide-react';
import { useWishlistDetail } from '../hooks/useWishlistDetail';
import { useAuth } from '../hooks/useAuth';
import { Layout } from '../components/layout/Layout';
import { ItemCard } from '../components/item/ItemCard';
import { ItemModal } from '../components/item/ItemModal';
import { CommentsSection } from '../components/item/CommentsSection';
import { ShareModal } from '../components/wishlist/ShareModal';
import { WishlistModal } from '../components/wishlist/WishlistModal';
import { Modal } from '../components/ui/Modal';
import { Button } from '../components/ui/Button';
import { ImageFallback } from '../components/ui/ImageFallback';
import { ItemCardSkeleton } from '../components/ui/SkeletonLoader';
import { EmptyState } from '../components/ui/EmptyState';
import type { WishListItemDto } from '../types';

const MAX_ITEMS = 50;

export default function WishlistDetailPage() {
  const { wishlistId } = useParams<{ wishlistId: string }>();
  const { user } = useAuth();
  const navigate = useNavigate();

  const { data: wishlist, isLoading, isError } = useWishlistDetail(wishlistId ?? '');

  const [isAddItemOpen, setIsAddItemOpen] = useState(false);
  const [editItem, setEditItem] = useState<WishListItemDto | null>(null);
  const [isShareOpen, setIsShareOpen] = useState(false);
  const [isEditWishlistOpen, setIsEditWishlistOpen] = useState(false);
  const [commentsItem, setCommentsItem] = useState<WishListItemDto | null>(null);
  const [requireAuthOpen, setRequireAuthOpen] = useState(false);

  if (isError) {
    return (
      <Layout>
        <div className="text-center py-16">
          <p className="text-gray-500">Wishlist not found or access denied.</p>
          <Link to="/wishlists" className="text-violet-600 hover:underline mt-2 inline-block text-sm">
            Back to wishlists
          </Link>
        </div>
      </Layout>
    );
  }

  const isOwner = wishlist?.userId === user?.id;
  const items = wishlist?.wishListItems ?? [];
  const atLimit = items.length >= MAX_ITEMS;

  return (
    <Layout>
      {/* Back button */}
      <button
        onClick={() => navigate(-1)}
        className="flex items-center gap-1.5 text-sm text-gray-500 hover:text-gray-700 transition-colors mb-4 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-violet-500 rounded"
      >
        <ArrowLeft size={15} />
        Back
      </button>

      {isLoading ? (
        <div className="space-y-6">
          {/* Header skeleton */}
          <div className="bg-white rounded-2xl overflow-hidden border border-gray-100 shadow-sm">
            <div className="w-full h-40 bg-gray-200 animate-pulse" />
            <div className="p-5 space-y-2">
              <div className="h-6 w-1/3 bg-gray-200 rounded animate-pulse" />
              <div className="h-4 w-1/4 bg-gray-200 rounded animate-pulse" />
            </div>
          </div>
          <div className="space-y-3">
            {Array.from({ length: 3 }).map((_, i) => (
              <ItemCardSkeleton key={i} />
            ))}
          </div>
        </div>
      ) : wishlist ? (
        <div className="space-y-6">
          {/* Wishlist header */}
          <motion.div
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.3 }}
            className="bg-white rounded-2xl overflow-hidden border border-gray-100 shadow-sm"
          >
            <ImageFallback
              src={wishlist.imageUrl}
              alt={wishlist.title}
              initials={wishlist.title.slice(0, 2).toUpperCase()}
              className="w-full h-40 sm:h-52"
            />
            <div className="p-5 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
              <div>
                <div className="flex items-center gap-2">
                  <h1 className="text-xl font-bold text-gray-900">{wishlist.title}</h1>
                  <span
                    className={`inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded-full font-medium ${
                      wishlist.isPublic
                        ? 'bg-emerald-50 text-emerald-700'
                        : 'bg-gray-100 text-gray-600'
                    }`}
                  >
                    {wishlist.isPublic ? (
                      <Globe size={10} />
                    ) : (
                      <Lock size={10} />
                    )}
                    {wishlist.isPublic ? 'Public' : 'Private'}
                  </span>
                </div>
                <p className="text-sm text-gray-500 mt-0.5">
                  {items.length} {items.length === 1 ? 'item' : 'items'}
                  {items.filter((i) => i.isChecked).length > 0 &&
                    ` · ${items.filter((i) => i.isChecked).length} reserved`}
                </p>
              </div>

              <div className="flex items-center gap-2">
                {isOwner && !wishlist.isPublic && (
                  <Button
                    variant="secondary"
                    size="sm"
                    onClick={() => setIsShareOpen(true)}
                    leftIcon={<UserPlus size={14} />}
                  >
                    Share
                  </Button>
                )}
                {isOwner && (
                  <Button
                    variant="secondary"
                    size="sm"
                    onClick={() => setIsEditWishlistOpen(true)}
                  >
                    Edit
                  </Button>
                )}
                {isOwner && !atLimit && (
                  <Button
                    size="sm"
                    onClick={() => setIsAddItemOpen(true)}
                    leftIcon={<Plus size={14} />}
                  >
                    Add item
                  </Button>
                )}
              </div>
            </div>
          </motion.div>

          {/* Items list */}
          {items.length === 0 ? (
            <EmptyState
              icon={<Package size={26} />}
              title="No items yet"
              description={
                isOwner
                  ? 'Add items to your wishlist so others know what to get you.'
                  : 'This wishlist is empty.'
              }
              action={
                isOwner ? (
                  <Button
                    onClick={() => setIsAddItemOpen(true)}
                    leftIcon={<Plus size={15} />}
                  >
                    Add first item
                  </Button>
                ) : undefined
              }
            />
          ) : (
            <div className="space-y-3">
              <AnimatePresence>
                {items.map((item) => (
                  <ItemCard
                    key={item.id}
                    item={item}
                    wishlistId={wishlist.id}
                    isOwner={isOwner}
                    onEdit={(i) => setEditItem(i)}
                    onOpenComments={(i) => setCommentsItem(i)}
                    onRequireAuth={() => setRequireAuthOpen(true)}
                  />
                ))}
              </AnimatePresence>
              {isOwner && !atLimit && (
                <button
                  onClick={() => setIsAddItemOpen(true)}
                  className="w-full rounded-2xl border-2 border-dashed border-gray-200 hover:border-violet-400 hover:bg-violet-50 transition-all duration-200 flex items-center justify-center gap-2 text-gray-400 hover:text-violet-500 py-5"
                >
                  <Plus size={20} strokeWidth={1.5} />
                  <span className="text-sm font-medium">Add item</span>
                </button>
              )}
            </div>
          )}

          {atLimit && isOwner && (
            <p className="text-sm text-amber-600 text-center">
              You&apos;ve reached the maximum of {MAX_ITEMS} items per wishlist.
            </p>
          )}
        </div>
      ) : null}

      {/* Modals */}
      {wishlist && (
        <>
          <ItemModal
            isOpen={isAddItemOpen}
            onClose={() => setIsAddItemOpen(false)}
            wishlistId={wishlist.id}
          />
          <ItemModal
            isOpen={!!editItem}
            onClose={() => setEditItem(null)}
            wishlistId={wishlist.id}
            editItem={editItem}
          />
          <WishlistModal
            isOpen={isEditWishlistOpen}
            onClose={() => setIsEditWishlistOpen(false)}
            editWishlist={wishlist}
          />
          {!wishlist.isPublic && isOwner && (
            <ShareModal
              isOpen={isShareOpen}
              onClose={() => setIsShareOpen(false)}
              wishlistId={wishlist.id}
              wishlistTitle={wishlist.title}
            />
          )}

          {/* Comments modal */}
          <Modal
            isOpen={!!commentsItem}
            onClose={() => setCommentsItem(null)}
            title={commentsItem ? `Discuss: ${commentsItem.title}` : ''}
            size="md"
          >
            {commentsItem && (
              <div className="p-5">
                <CommentsSection
                  wishlistId={wishlist.id}
                  itemId={commentsItem.id}
                />
              </div>
            )}
          </Modal>
        </>
      )}

      {/* Require auth modal */}
      <Modal
        isOpen={requireAuthOpen}
        onClose={() => setRequireAuthOpen(false)}
        title="Sign in required"
        size="sm"
      >
        <div className="px-6 py-5 text-center">
          <p className="text-sm text-gray-600 mb-5">
            You need to sign in or create an account to mark items as reserved.
          </p>
          <div className="flex gap-3">
            <Link
              to="/sign-in"
              className="flex-1 inline-flex items-center justify-center gap-2 px-4 py-2.5 text-sm font-medium text-white bg-violet-600 rounded-xl hover:bg-violet-700 transition-colors"
            >
              <LogIn size={15} />
              Sign in
            </Link>
            <Link
              to="/sign-up"
              className="flex-1 inline-flex items-center justify-center gap-2 px-4 py-2.5 text-sm font-medium text-gray-700 bg-gray-100 rounded-xl hover:bg-gray-200 transition-colors"
            >
              <UserPlusIcon size={15} />
              Sign up
            </Link>
          </div>
        </div>
      </Modal>
    </Layout>
  );
}
