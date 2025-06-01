package com.esa.moviestar.libraries;

import com.esa.moviestar.database.ContentDao;
import com.esa.moviestar.model.Content;
import com.esa.moviestar.model.ContentCacheManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TMDbApiManager {
    private static final String BASE_URL = "https://api.themoviedb.org/3";
    private static final String API_KEY = "";
    private static final String LANGUAGE = "en-US";
    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/";
    private static final String DEFAULT_IMAGE_SIZE = "w500";

    private static volatile TMDbApiManager instance;
    private final OkHttpClient client;

    private final ExecutorService executor;
    private ContentDao contentDao;
    private final ContentCacheManager cacheManager;

    public enum ContentType {
        MOVIE, TV
    }


    private TMDbApiManager() {
        this.client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor())
                .build();
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.cacheManager = ContentCacheManager.getInstance();
    }


    public static TMDbApiManager getInstance() {
        if (instance == null) {
            synchronized (TMDbApiManager.class) {
                if (instance == null) {
                    instance = new TMDbApiManager();
                }
            }
        }
        return instance;
    }

    // Setter for ContentDao dependency injection
    public void setContentDao(ContentDao contentDao) {
        this.contentDao = contentDao;
    }

    private static class AuthInterceptor implements Interceptor {
        @NotNull
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();
            HttpUrl originalHttpUrl = original.url();

            HttpUrl url = originalHttpUrl.newBuilder()
                    // Authorization header is now preferred for the API key by TMDb
                    // .addQueryParameter("api_key", API_KEY) // Deprecated way
                    .build();

            Request.Builder requestBuilder = original.newBuilder()
                    .url(url)
                    .header("accept", "application/json")
                    .header("Authorization", "Bearer " + API_KEY); // Correct way

            Request request = requestBuilder.build();
            return chain.proceed(request);
        }
    }

    private Request buildRequest(String endpointPathAndQuery) {
        HttpUrl parsedUrl = HttpUrl.parse(BASE_URL + endpointPathAndQuery);
        if (parsedUrl == null) {
            throw new IllegalArgumentException("Malformed URL for endpoint: " + endpointPathAndQuery);
        }

        HttpUrl.Builder urlBuilder = parsedUrl.newBuilder();
        if (parsedUrl.queryParameter("language") == null) {
            urlBuilder.addQueryParameter("language", LANGUAGE);
        }
        // API key is added by AuthInterceptor via header

        return new Request.Builder()
                .url(urlBuilder.build())
                .get()
                .build();
    }

    public String makeRequest(String endpoint) throws IOException {
        Request request = buildRequest(endpoint);
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                ResponseBody errorBody = response.body();
                String errorDetails = errorBody != null ? errorBody.string() : "No error body";
                System.err.println("TMDbApiManager: Request URL: " + request.url());
                System.err.println("TMDbApiManager: Response Code: " + response.code());
                System.err.println("TMDbApiManager: Error Details: " + errorDetails);
                throw new IOException("Unexpected response code: " + response.code() + " for URL: " + request.url() + ". Details: " + errorDetails);
            }
            ResponseBody body = response.body();
            return body != null ? body.string() : "";
        }
    }

    public CompletableFuture<String> makeRequestAsync(String endpoint) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return makeRequest(endpoint);
            } catch (IOException e) {
                // Log the error with more details
                System.err.println("TMDbApiManager: API request failed for endpoint: " + endpoint + ". Error: " + e.getMessage());
                throw new RuntimeException("API request failed for endpoint: " + endpoint, e);
            }
        }, executor);
    }

    // fetchAsMoviestarContentList remains largely the same, but ensure it maps fields correctly
    // to your Content model, especially if you've added/changed fields.
    // The default Film_Url is now handled by the database schema or ContentDao.insertContents.
    public CompletableFuture<List<Content>> fetchAsMoviestarContentList(String endpoint, ContentType expectedType) {
        return makeRequestAsync(endpoint).thenApplyAsync(responseString -> {
            List<Content> contentList = new ArrayList<>();
            if (responseString == null || responseString.isEmpty()) {
                System.err.println("TMDbApiManager: Empty response for endpoint: " + endpoint);
                return contentList;
            }
            try {
                JsonObject jsonResponse = JsonParser.parseString(responseString).getAsJsonObject();
                JsonArray resultsArray = jsonResponse.getAsJsonArray("results");

                if (resultsArray != null) {
                    for (JsonElement element : resultsArray) {
                        if (element.isJsonObject()) {
                            JsonObject TMDbObjectJson = element.getAsJsonObject();
                            int id = TMDbObjectJson.has( "id") ? TMDbObjectJson.get("id").getAsInt() : 0;

                            if ( id == 0) continue;

                            Content content = cacheManager.get(id);
                            boolean isNewInCache  = false;
                            if (content == null) {
                                content = new Content();
                                content.setId(id );

                                isNewInCache = true;
                            }

                            // Populate or update fields from API data
                            content.setPlot(getStringOrNull(TMDbObjectJson, "overview"));
                            content.setRating(TMDbObjectJson.has("vote_average") ? TMDbObjectJson.get("vote_average").getAsDouble() : 0.0);
                            content.setPopularity(TMDbObjectJson.has("popularity") ? (int) TMDbObjectJson.get("popularity").getAsDouble() : 0);

                            String posterPath = getStringOrNull(TMDbObjectJson, "poster_path" );
                            String backdropPath = getStringOrNull(TMDbObjectJson, "backdrop_path");
                            content.setImageUrl(getImageUrl(backdropPath, DEFAULT_IMAGE_SIZE));
                            content.setPosterUrl(getImageUrl(posterPath, DEFAULT_IMAGE_SIZE));

                            String releaseDateStr = null;
                            if (expectedType == ContentType.MOVIE) {
                                content.setTitle(getStringOrNull(TMDbObjectJson, "title"));
                                releaseDateStr = getStringOrNull(TMDbObjectJson, "release_date");
                                content.Series(false);
                            } else if (expectedType == ContentType.TV) {
                                content.setTitle(getStringOrNull(TMDbObjectJson, "name"));
                                releaseDateStr = getStringOrNull(TMDbObjectJson, "first_air_date");
                                content.Series(true);
                            }

                            content.setReleaseDate(releaseDateStr);
                            if (releaseDateStr != null && !releaseDateStr.isEmpty()) {
                                try {
                                    LocalDate date = LocalDate.parse(releaseDateStr, DateTimeFormatter.ISO_LOCAL_DATE);
                                    content.setYear(date.getYear());
                                } catch (DateTimeParseException e) {
                                    System.err.println("TMDbApiManager: Could not parse year from date: " + releaseDateStr + " for " + content.getTitle());
                                    content.setYear(0); // Default year if parsing fails
                                }
                            } else {
                                content.setYear(0); // Default year if no release date
                            }

                            if (TMDbObjectJson.has("genre_ids") && TMDbObjectJson.get("genre_ids").isJsonArray()) {
                                JsonArray genreIdsArray = TMDbObjectJson.getAsJsonArray("genre_ids");
                                List<Integer> categoryIds = new ArrayList<>();
                                for (JsonElement genreIdElement : genreIdsArray) {
                                    categoryIds.add(genreIdElement.getAsInt());
                                }
                                content.setCategories(categoryIds); // Overwrites existing categories from API
                            }

                            if (isNewInCache) {
                                content.setVideoUrl("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4"); ///////////////////////////////////////////////// Default video URL
                            }

                            if (isNewInCache)
                                cacheManager.put(content);

                            contentList.add(content);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("TMDbApiManager: Failed to parse/map content from JSON for: " + endpoint + ". Error: " + e.getMessage() + ". Response: " + responseString.substring(0, Math.min(responseString.length(), 500)));
            }
            return contentList;
        }, executor);
    }

    private String getStringOrNull(JsonObject jsonObject, String memberName) {
        if (jsonObject.has(memberName) && !jsonObject.get(memberName).isJsonNull()) {
            return jsonObject.get(memberName).getAsString();
        }
        return null;
    }

    // --- Methods for fetching different content types ---
    public CompletableFuture<List<Content>> getTrendingMoviesAsMoviestarContent(String timeWindow) {
        return fetchAsMoviestarContentList("/trending/movie/" + timeWindow, ContentType.MOVIE);
    }

    public CompletableFuture<List<Content>> getTrendingTvShowsAsMoviestarContent(String timeWindow) {
        return fetchAsMoviestarContentList("/trending/tv/" + timeWindow, ContentType.TV);
    }

    public CompletableFuture<List<Content>> getPopularMoviesAsMoviestarContent() {
        return fetchAsMoviestarContentList("/movie/popular", ContentType.MOVIE);
    }

    public CompletableFuture<List<Content>> getPopularTvShowsAsMoviestarContent() {
        return fetchAsMoviestarContentList("/tv/popular", ContentType.TV);
    }

    public CompletableFuture<Void> updateAllContentInDatabase() {
        if (contentDao == null) {
            System.err.println("TMDbApiManager: ContentDao not initialized. Cannot update database.");
            return CompletableFuture.failedFuture(new IllegalStateException("ContentDao not initialized."));
        }

        // Fetching operations will now use the cache-aware fetchAsMoviestarContentList
        CompletableFuture<List<Content>> popularMoviesFuture = getPopularMoviesAsMoviestarContent();
        CompletableFuture<List<Content>> trendingMoviesDayFuture = getTrendingMoviesAsMoviestarContent("day");
        CompletableFuture<List<Content>> popularTvShowsFuture = getPopularTvShowsAsMoviestarContent();
        CompletableFuture<List<Content>> trendingTvDayFuture = getTrendingTvShowsAsMoviestarContent("day");

        List<CompletableFuture<List<Content>>> futures = List.of(
                popularMoviesFuture, trendingMoviesDayFuture,
                popularTvShowsFuture, trendingTvDayFuture
        );

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenComposeAsync(voidResult -> {
                    List<Content> allContentFromApi = new ArrayList<>();
                    for (CompletableFuture<List<Content>> future : futures) {
                        try {
                            allContentFromApi.addAll(future.join());
                        } catch (Exception e) {
                            System.err.println("TMDbApiManager: Error joining future during content update: " + e.getMessage());
                        }
                    }

                    List<Content> distinctContentToStore = allContentFromApi.stream()
                            .filter(Objects::nonNull)
                            .filter(c -> c.getId() != 0)
                            .filter(distinctByKey(Content::getId))
                            .collect(Collectors.toList());

                    if (!distinctContentToStore.isEmpty()) {
                        try {
                            System.out.println("TMDbApiManager: Passing " + distinctContentToStore.size() + " distinct content items (from cache/API) to DAO for DB update.");
                            contentDao.insertContents(distinctContentToStore);
                            System.out.println("TMDbApiManager: Database update process complete via DAO.");
                        } catch (Exception e) {
                            System.err.println("TMDbApiManager: Error during contentDao.insertContents: " + e.getMessage());
                            return CompletableFuture.failedFuture(e);
                        }
                    } else {
                        System.out.println("TMDbApiManager: No distinct content from API to update in the database.");
                    }
                    return CompletableFuture.completedFuture(null);
                }, executor);
    }

    // Helper for distinctByKey using a ConcurrentHashMap for thread-safety if stream is parallel (though not here)
    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor ) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    // --- Search logic (remains, with URL encoding for query) ---
    public CompletableFuture<List<Content>> searchMoviesAsMoviestarContent (String query) {
        String encodedQuery;
        encodedQuery =  URLEncoder.encode( query, StandardCharsets.UTF_8);
        String  endpointWithQuery = "/search/movie?query=" + encodedQuery;
        return fetchAsMoviestarContentList(endpointWithQuery, ContentType.MOVIE );
    }

    public CompletableFuture<List<Content>> searchTvShowsAsMoviestarContent(String query) {
        String encodedQuery;
        encodedQuery = URLEncoder.encode( query, StandardCharsets.UTF_8);
        String endpointWithQuery = "/search/tv?query=" + encodedQuery;
        return fetchAsMoviestarContentList(endpointWithQuery, ContentType.TV);
    }

    @Nullable
    public String getImageUrl(@Nullable String imagePath, String size) {
        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }
        return IMAGE_BASE_URL + size + imagePath;
    }


    @Nullable
    public String getPosterUrl(@Nullable String posterPath, String size) {
        return getImageUrl(posterPath, size);
    }

    @Nullable
    public String getBackdropUrl(@Nullable String backdropPath, String size) {
        return getImageUrl(backdropPath, size);
    }

}