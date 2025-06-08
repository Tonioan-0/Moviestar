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

    /**
     * Adds or updates a Content object in the cache.
     * If the content (by ID) already exists, it will be replaced.
     */
    public void put(Content content) {
        if (content  != null && content.getId()  != 0)
            contentCache.put(content.getId(),  content);
    }

    /**
     * Removes a Content object from the cache.
     *
     * @param contentId The ID of the content to remove.
     */
    public void remove(int contentId ) {
        contentCache.remove(contentId);
    }

    public void clearAll() {
        contentCache.clear();
        System.out.println("ContentCacheManager: Cache cleared." );
    }

    /**
     * Gets the current size of the cache.
     */
    public int size() {
        return  contentCache.size();
    }
}