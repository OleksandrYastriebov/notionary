package com.api.wishoria.controller;

import com.api.wishoria.service.SitemapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/seo")
public class SitemapController {

    private final SitemapService sitemapService;

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public String generateSitemap() {
        return sitemapService.generateSitemapXml();
    }

}