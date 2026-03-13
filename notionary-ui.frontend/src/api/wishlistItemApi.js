import axiosInstance from "./axiosConfig";

export const wishlistItemApi = {
  getWishlistItems: (wishlistId) =>
    axiosInstance.get(`/wishlists/${wishlistId}/wishes`),

  getWishlistItem: (wishlistId, itemId) =>
    axiosInstance.get(`/wishlists/${wishlistId}/wishes/${itemId}`),

  createWishlistItem: (wishlistId, data) =>
    axiosInstance.post(`/wishlists/${wishlistId}`, data),

  updateWishlistItem: (wishlistId, itemId, data) =>
    axiosInstance.patch(`/wishlists/${wishlistId}/wishes/${itemId}`, data),

  toggleItemCheck: (wishlistId, itemId, data) =>
    axiosInstance.patch(
      `/wishlists/${wishlistId}/wishes/${itemId}/checked`,
      data
    ),

  deleteWishlistItem: (wishlistId, itemId) =>
    axiosInstance.delete(`/wishlists/${wishlistId}/wishes/${itemId}`),
};
