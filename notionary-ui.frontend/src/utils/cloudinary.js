/**
 * Generate responsive Cloudinary image URL based on screen size
 * @param {string} imageUrl - Original Cloudinary URL
 * @param {string} size - Size preset: 'small', 'medium', 'large'
 * @returns {string} Transformed URL
 */
export const getResponsiveImageUrl = (imageUrl, size = "medium") => {
  if (!imageUrl || !imageUrl.includes("cloudinary.com")) {
    return imageUrl;
  }

  const transformations = {
    small: "c_fill,w_300,h_300,q_auto,f_auto",
    medium: "c_fill,w_600,h_600,q_auto,f_auto",
    large: "c_fill,w_1200,h_1200,q_auto,f_auto",
    thumbnail: "c_fill,w_150,h_150,q_auto,f_auto",
  };

  const transformation = transformations[size] || transformations.medium;

  // Insert transformation into Cloudinary URL
  return imageUrl.replace("/upload/", `/upload/${transformation}/`);
};

/**
 * Get image URL based on device type
 */
export const getDeviceSpecificImage = (imageUrl) => {
  if (!imageUrl) return imageUrl;

  const isMobile = window.matchMedia("(max-width: 640px)").matches;
  const isTablet = window.matchMedia(
    "(min-width: 641px) and (max-width: 1024px)"
  ).matches;

  if (isMobile) return getResponsiveImageUrl(imageUrl, "small");
  if (isTablet) return getResponsiveImageUrl(imageUrl, "medium");
  return getResponsiveImageUrl(imageUrl, "large");
};
