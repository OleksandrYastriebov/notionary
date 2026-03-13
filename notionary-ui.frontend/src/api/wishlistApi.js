import axiosInstance from "./axiosConfig";

export const wishlistApi = {
  getWishlists: () => axiosInstance.get("/wishlists"),

  getWishlist: (wishlistId) => axiosInstance.get(`/wishlists/${wishlistId}`),

  createWishlist: (data) => axiosInstance.post("/wishlists", data),

  updateWishlist: (wishlistId, data) =>
    axiosInstance.patch(`/wishlists/${wishlistId}`, data),

  deleteWishlist: (wishlistId) =>
    axiosInstance.delete(`/wishlists/${wishlistId}`),
};
