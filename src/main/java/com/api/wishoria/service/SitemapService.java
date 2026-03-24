package com.api.wishoria.service;

import com.api.wishoria.config.CacheConfig;
import com.api.wishoria.entity.WishList;
import com.api.wishoria.repository.WishListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.EMPTY;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SitemapService {

    private final WishListRepository wishListRepository;

    @Value("${app.url.frontend}")
    private String frontendUrl;

    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    private static final String URLSET_OPEN = "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n";
    private static final String URLSET_CLOSE = "</urlset>";

    @Cacheable(value = CacheConfig.SITEMAP_CACHE)
    public String generateSitemapXml() {
        StringBuilder xmlBuilder = new StringBuilder();
        xmlBuilder.append(XML_HEADER);
        xmlBuilder.append(URLSET_OPEN);

        xmlBuilder.append(createUrlTag(frontendUrl + "/", "1.0", "daily"));
        List<WishList> publicWishlists = wishListRepository.findAllByIsPublicTrue();

        for (WishList wishlist : publicWishlists) {
            String wishlistUrl = frontendUrl + "/wishlists/" + wishlist.getId();
            xmlBuilder.append(createUrlTag(wishlistUrl, "0.8", "weekly"));
        }

        xmlBuilder.append(URLSET_CLOSE);
        return xmlBuilder.toString();
    }

    private String createUrlTag(String url, String priority, String changeFreq) {
        return String.format("""
                <url>
                    <loc>%s</loc>
                    <changefreq>%s</changefreq>
                    <priority>%s</priority>
                </url>
                """, escapeXml(url), changeFreq, priority
        );
    }


    private String escapeXml(String url) {
        if (url == null) {
            return EMPTY;
        }
        return url.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}