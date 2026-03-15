import { useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { createItem, updateItem, deleteItem, toggleItemChecked } from '../api/endpoints';
import type { CreateItemRequest, UpdateItemRequest } from '../types';

const wishlistKey = (id: string) => ['wishlist', id] as const;

export function useCreateItem(wishlistId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateItemRequest) => createItem(wishlistId, data),
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: wishlistKey(wishlistId) });
      toast.success('Item added!');
    },
    onError: () => {
      toast.error('Failed to add item.');
    },
  });
}

export function useUpdateItem(wishlistId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ itemId, data }: { itemId: string; data: UpdateItemRequest }) =>
      updateItem(wishlistId, itemId, data),
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: wishlistKey(wishlistId) });
      toast.success('Item updated!');
    },
    onError: () => {
      toast.error('Failed to update item.');
    },
  });
}

export function useDeleteItem(wishlistId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (itemId: string) => deleteItem(wishlistId, itemId),
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: wishlistKey(wishlistId) });
      toast.success('Item removed.');
    },
    onError: () => {
      toast.error('Failed to delete item.');
    },
  });
}

export function useToggleChecked(wishlistId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ itemId, isChecked }: { itemId: string; isChecked: boolean }) =>
      toggleItemChecked(wishlistId, itemId, { isChecked }),
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: wishlistKey(wishlistId) });
    },
  });
}
