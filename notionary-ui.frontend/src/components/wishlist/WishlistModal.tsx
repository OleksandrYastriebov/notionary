import { useEffect, useRef, useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { ImageIcon, Upload } from 'lucide-react';
import { Modal } from '../ui/Modal';
import { Input } from '../ui/Input';
import { Button } from '../ui/Button';
import { useCreateWishlist, useUpdateWishlist } from '../../hooks/useWishlists';
import { useUploadImage } from '../../hooks/useUploadImage';
import type { WishListDto } from '../../types';

const schema = z.object({
  title: z.string().min(1, 'Title is required').max(100, 'Max 100 characters'),
  isPublic: z.boolean(),
  imageUrl: z.string().url('Must be a valid URL').or(z.literal('')).optional(),
});

type FormData = z.infer<typeof schema>;

interface WishlistModalProps {
  isOpen: boolean;
  onClose: () => void;
  editWishlist?: WishListDto | null;
}

export function WishlistModal({ isOpen, onClose, editWishlist }: WishlistModalProps) {
  const isEdit = !!editWishlist;
  const createMutation = useCreateWishlist();
  const updateMutation = useUpdateWishlist();
  const uploadMutation = useUploadImage();
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [imageRemoved, setImageRemoved] = useState(false);

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors },
  } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { title: '', isPublic: true, imageUrl: '' },
  });

  const imageUrlValue = watch('imageUrl');

  useEffect(() => {
    if (isOpen) {
      if (editWishlist) {
        reset({
          title: editWishlist.title,
          isPublic: editWishlist.isPublic,
          imageUrl: editWishlist.imageUrl ?? '',
        });
        setPreviewUrl(editWishlist.imageUrl);
      } else {
        reset({ title: '', isPublic: true, imageUrl: '' });
        setPreviewUrl(null);
      }
      setImageRemoved(false);
    }
  }, [isOpen, editWishlist, reset]);

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    const result = await uploadMutation.mutateAsync(file);
    setValue('imageUrl', result.url);
    setPreviewUrl(result.url);
    setImageRemoved(false);
  };

  const onSubmit = async (data: FormData) => {
    const payload = {
      title: data.title,
      isPublic: data.isPublic,
      imageUrl: data.imageUrl ? data.imageUrl : (imageRemoved ? '' : undefined),
    };

    if (isEdit && editWishlist) {
      updateMutation.mutate(
        { id: editWishlist.id, data: payload },
        { onSuccess: () => { onClose(); reset(); } }
      );
    } else {
      createMutation.mutate(payload, {
        onSuccess: () => { onClose(); reset(); },
      });
    }
  };

  const isLoading = createMutation.isPending || updateMutation.isPending || uploadMutation.isPending;

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={isEdit ? 'Edit Wishlist' : 'New Wishlist'}>
      <form onSubmit={(e) => void handleSubmit(onSubmit)(e)} className="p-6 space-y-4">
        {/* Image upload */}
        <div>
          <label className="text-sm font-medium text-gray-700 block mb-2">Cover Image</label>
          <div className="relative">
            {(previewUrl ?? imageUrlValue) ? (
              <div className="relative w-full h-32 rounded-xl overflow-hidden border border-gray-200 mb-2">
                <img
                  src={previewUrl ?? imageUrlValue}
                  alt="Preview"
                  className="w-full h-full object-cover"
                  onError={() => setPreviewUrl(null)}
                />
                <button
                  type="button"
                  onClick={() => {
                    setPreviewUrl(null);
                    setValue('imageUrl', '');
                    setImageRemoved(true);
                  }}
                  className="absolute top-2 right-2 p-1 rounded-lg bg-black/40 text-white hover:bg-black/60 transition-colors text-xs px-2"
                >
                  Remove
                </button>
              </div>
            ) : (
              <button
                type="button"
                onClick={() => fileInputRef.current?.click()}
                className="w-full h-24 rounded-xl border-2 border-dashed border-gray-200 flex flex-col items-center justify-center gap-1.5 text-gray-400 hover:text-violet-500 hover:border-violet-300 transition-colors"
              >
                <ImageIcon size={20} />
                <span className="text-xs">Upload cover image</span>
              </button>
            )}
            <input
              ref={fileInputRef}
              type="file"
              accept="image/*"
              className="hidden"
              onChange={(e) => void handleFileChange(e)}
            />
          </div>
          {(previewUrl ?? imageUrlValue) && (
            <button
              type="button"
              onClick={() => fileInputRef.current?.click()}
              className="flex items-center gap-1.5 text-xs text-violet-600 hover:text-violet-700 mt-1"
            >
              <Upload size={12} />
              Replace image
            </button>
          )}
        </div>

        <Input
          label="Title"
          placeholder="My birthday wishlist"
          error={errors.title?.message}
          {...register('title')}
        />

        <div className="flex items-center justify-between p-3.5 rounded-xl border border-gray-200 hover:border-gray-300 transition-colors">
          <div>
            <p className="text-sm font-medium text-gray-800">Public wishlist</p>
            <p className="text-xs text-gray-500 mt-0.5">Anyone with the link can view it</p>
          </div>
          <label className="relative inline-flex items-center cursor-pointer">
            <input
              type="checkbox"
              className="sr-only peer"
              {...register('isPublic')}
            />
            <div className="w-10 h-6 bg-gray-200 peer-focus:ring-2 peer-focus:ring-violet-500 rounded-full peer peer-checked:after:translate-x-full after:content-[''] after:absolute after:top-0.5 after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-violet-600" />
          </label>
        </div>

        <div className="flex gap-3 pt-1">
          <Button
            type="button"
            variant="secondary"
            onClick={() => { onClose(); reset(); }}
            className="flex-1"
            disabled={isLoading}
          >
            Cancel
          </Button>
          <Button
            type="submit"
            className="flex-1"
            isLoading={isLoading}
          >
            {isEdit ? 'Save changes' : 'Create'}
          </Button>
        </div>
      </form>
    </Modal>
  );
}
