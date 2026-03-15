import { useMutation } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { uploadImage } from '../api/endpoints';

export function useUploadImage() {
  return useMutation({
    mutationFn: (file: File) => uploadImage(file),
    onError: () => {
      toast.error('Failed to upload image.');
    },
  });
}
