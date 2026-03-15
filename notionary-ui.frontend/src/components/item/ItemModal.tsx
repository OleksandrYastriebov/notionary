import { useEffect, useRef, useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { ImageIcon, Upload } from 'lucide-react';
import { Modal } from '../ui/Modal';
import { Input } from '../ui/Input';
import { Textarea } from '../ui/Textarea';
import { Button } from '../ui/Button';
import { useCreateItem, useUpdateItem } from '../../hooks/useWishlistItems';
import { useUploadImage } from '../../hooks/useUploadImage';
import type { WishListItemDto } from '../../types';

const schema = z.object({
  title: z.string().min(1, 'Title is required').max(200),
  url: z.string().url('Must be a valid URL').or(z.literal('')).optional(),
  price: z
    .string()
    .optional()
    .refine((v) => !v || !isNaN(parseFloat(v)), { message: 'Must be a number' }),
  description: z.string().max(500).optional(),
  imageUrl: z.string().url().or(z.literal('')).optional(),
});

type FormData = z.infer<typeof schema>;

interface ItemModalProps {
  isOpen: boolean;
  onClose: () => void;
  wishlistId: string;
  editItem?: WishListItemDto | null;
}

export function ItemModal({ isOpen, onClose, wishlistId, editItem }: ItemModalProps) {
  const isEdit = !!editItem;
  const createMutation = useCreateItem(wishlistId);
  const updateMutation = useUpdateItem(wishlistId);
  const uploadMutation = useUploadImage();
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors },
  } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { title: '', url: '', price: '', description: '', imageUrl: '' },
  });

  const imageUrlValue = watch('imageUrl');

  useEffect(() => {
    if (isOpen) {
      if (editItem) {
        reset({
          title: editItem.title,
          url: editItem.url ?? '',
          price: editItem.price?.toString() ?? '',
          description: editItem.description ?? '',
          imageUrl: editItem.imageUrl ?? '',
        });
        setPreviewUrl(editItem.imageUrl);
      } else {
        reset({ title: '', url: '', price: '', description: '', imageUrl: '' });
        setPreviewUrl(null);
      }
    }
  }, [isOpen, editItem, reset]);

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    const result = await uploadMutation.mutateAsync(file);
    setValue('imageUrl', result.url);
    setPreviewUrl(result.url);
  };

  const onSubmit = (data: FormData) => {
    const payload = {
      title: data.title,
      url: data.url || undefined,
      price: data.price ? parseFloat(data.price) : undefined,
      description: data.description || undefined,
      imageUrl: data.imageUrl || undefined,
    };

    if (isEdit && editItem) {
      updateMutation.mutate(
        { itemId: editItem.id, data: payload },
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
    <Modal isOpen={isOpen} onClose={onClose} title={isEdit ? 'Edit Item' : 'Add Item'} size="md">
      <form onSubmit={(e) => void handleSubmit(onSubmit)(e)} className="p-6 space-y-4">
        {/* Image upload */}
        <div>
          <label className="text-sm font-medium text-gray-700 block mb-2">Image</label>
          {(previewUrl ?? imageUrlValue) ? (
            <div className="relative w-full h-28 rounded-xl overflow-hidden border border-gray-200 mb-2">
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
              className="w-full h-20 rounded-xl border-2 border-dashed border-gray-200 flex flex-col items-center justify-center gap-1 text-gray-400 hover:text-violet-500 hover:border-violet-300 transition-colors"
            >
              <ImageIcon size={18} />
              <span className="text-xs">Upload image</span>
            </button>
          )}
          {(previewUrl ?? imageUrlValue) && (
            <button
              type="button"
              onClick={() => fileInputRef.current?.click()}
              className="flex items-center gap-1.5 text-xs text-violet-600 hover:text-violet-700 mt-1"
            >
              <Upload size={12} />
              Replace
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

        <Input
          label="Title"
          placeholder="AirPods Pro"
          error={errors.title?.message}
          {...register('title')}
        />

        <div className="grid grid-cols-2 gap-3">
          <Input
            label="Price (USD)"
            type="number"
            step="0.01"
            placeholder="99.99"
            error={errors.price?.message}
            {...register('price')}
          />
          <Input
            label="Link (optional)"
            type="url"
            placeholder="https://..."
            error={errors.url?.message}
            {...register('url')}
          />
        </div>

        <Textarea
          label="Description (optional)"
          placeholder="Any color, just not white"
          rows={2}
          error={errors.description?.message}
          {...register('description')}
        />

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
          <Button type="submit" className="flex-1" isLoading={isLoading}>
            {isEdit ? 'Save' : 'Add item'}
          </Button>
        </div>
      </form>
    </Modal>
  );
}
