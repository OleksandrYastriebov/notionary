import { uploadImage } from '../api/endpoints';

export const handleImageUpload = async (file: File): Promise<string> => {
  const result = await uploadImage(file);
  return result.url;
};
