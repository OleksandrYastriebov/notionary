import axiosInstance from "./axiosConfig";

export const imageApi = {
  uploadImage: (file) => {
    const formData = new FormData();
    formData.append("file", file);

    return axiosInstance.post("/images", formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });
  },
};
