import axiosInstance from "./axiosConfig";

export const userApi = {
  getCurrentUser: () => axiosInstance.get("/users/me"),

  updateUser: (data) => axiosInstance.patch("/users/me", data),

  deleteUser: (userId) => axiosInstance.delete(`/users/${userId}`),
};
