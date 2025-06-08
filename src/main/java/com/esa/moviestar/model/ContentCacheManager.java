package com.esa.moviestar.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ContentCacheManager {
    private static volatile ContentCacheManager instance;
    private final Map<Integer, Content> contentCache;

    private ContentCacheManager() {
        contentCache = new ConcurrentHashMap<>();
    }

    public static ContentCacheManager getInstance() {
        if (instance == null) {
            synchronized (ContentCacheManager.class) {
                if (instance == null)
                    instance = new ContentCacheManager();
            }
        }
        return instance;
    }


    public Content get(int contentId) {
        return contentCache.get(contentId);
    }

    public void put(Content content) {
        if (content  != null && content.getId()  != 0)
            contentCache.put(content.getId(),  content);
    }


    public void remove(int contentId ) {
        contentCache.remove(contentId);
    }

    public void clearAll() {
        contentCache.clear();
        System.out.println("ContentCacheManager: Cache cleared." );
    }


    public int size() {
        return  contentCache.size();
    }
}