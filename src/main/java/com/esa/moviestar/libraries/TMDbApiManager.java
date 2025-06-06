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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
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
import java.util.stream.Stream;

public class TMDbApiManager {
    private static final String BASE_URL = "https://api.themoviedb.org/3";
    private static final String API_KEY = "";
    private static final String LANGUAGE = "en-US"; // Default language
    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/";
    private static final String DEFAULT_IMAGE_SIZE = "w500";
    private static final String BACKDROP_IMAGE_SIZE_DETAILS = "w1280"; // For film scene background
    private static final String STILL_IMAGE_SIZE = "w300"; // For episode stills


    private static volatile TMDbApiManager instance;
    private final OkHttpClient client;
    private final ExecutorService executor;
    private ContentDao contentDao; // Keep ContentDao, but handle its potential nullness
    private final ContentCacheManager cacheManager;

    public enum ContentType {
        MOVIE, TV, UNKNOWN
    }

    // Helper classes for TV Season/Episode details from TMDb API
    public static class ApiSeasonDetails {
        public int seasonNumber;
        public String name;
        public String overview;
        public String posterPath; // Relative path
        public List<ApiEpisodeDetails> episodes;
        public String airDate;
        public int id; // TMDb season ID

        public ApiSeasonDetails() {
            episodes = new ArrayList<>();
        }

        // Convenience method to get full poster URL
        public String getFullPosterUrl(String size) {
            // Use the instance to get the image URL, handling potential null path
            return TMDbApiManager.getInstance().getImageUrl(posterPath, size);
        }
    }

    public static class ApiEpisodeDetails {
        public int episodeNumber;
        public String name;
        public String overview;
        public String stillPath; // Relative path
        public int runtime; // in minutes
        public String airDate;
        public double voteAverage;
        public int id; // TMDb episode ID

        // Convenience method to get full still URL
        public String getFullStillUrl(String size) {
            // Use the instance to get the image URL, handling potential null path
            return TMDbApiManager.getInstance().getImageUrl(stillPath, size);
        }
    }


    private TMDbApiManager() {
        this.client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor())
                .build();
        // Using a fixed thread pool for API calls is generally better than cached
        // to prevent unbounded thread creation under heavy load. Adjust size as needed.
        this.executor = Executors.newFixedThreadPool(10);
        this.cacheManager = ContentCacheManager.getInstance();
    }

    public static TMDbApiManager getInstance() {
        if (API_KEY.trim().isEmpty()) {
            System.err.println("FATAL ERROR: TMDb API Key is not set in TMDbApiManager.java. API calls will fail.");
        }
        if (instance == null) {
            synchronized (TMDbApiManager.class) {
                if (instance == null) {
                    instance = new TMDbApiManager();
                }
            }
        }
        return instance;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public void setContentDao(ContentDao contentDao) {
        this.contentDao = contentDao;
    }

    private static class AuthInterceptor implements Interceptor {
        @NotNull
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();
            HttpUrl originalHttpUrl = original.url();
            HttpUrl url = originalHttpUrl.newBuilder().build(); // No need to add API key as query param with Bearer token
            Request.Builder requestBuilder = original.newBuilder()
                    .url(url)
                    .header("accept", "application/json")
                    // Use the Bearer token method which is preferred by TMDb v3/v4
                    .header("Authorization", "Bearer " + API_KEY);

            Request request = requestBuilder.build();

            if (API_KEY.trim().isEmpty()) {
                // Or return a specific error response if you don't throw in getInstance
                return new Response.Builder()
                        .request(request)
                        .protocol(Protocol.HTTP_1_1)
                        .code(500) // Internal Server Error
                        .message("API Key Not Set")
                        .body(ResponseBody.create("{\"status_code\":500,\"status_message\":\"TMDb API Key is not set.\"}", MediaType.get("application/json")))
                        .build();
            }

            return chain.proceed(request);
        }
    }

    private Request buildRequest(String endpointPathAndQuery) {
        HttpUrl parsedUrl = HttpUrl.parse(BASE_URL + endpointPathAndQuery);
        if (parsedUrl == null) {
            // Log the malformed URL and throw
            System.err.println("TMDbApiManager: Malformed URL attempted: " + BASE_URL + endpointPathAndQuery);
            throw new IllegalArgumentException("Malformed URL for endpoint: " + endpointPathAndQuery);
        }
        HttpUrl.Builder urlBuilder = parsedUrl.newBuilder();
        // Add language parameter unless it's an image URL or already present
        if (!endpointPathAndQuery.startsWith("/t/p/") && parsedUrl.queryParameter("language") == null) {
            urlBuilder.addQueryParameter("language", LANGUAGE);
        }
        return new Request.Builder().url(urlBuilder.build()).get().build();
    }

    public String makeRequest(String endpoint) throws IOException {
        Request request = buildRequest(endpoint);
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                ResponseBody errorBody = response.body();
                String errorDetails = errorBody != null ? errorBody.string() : "No error body";
                // Log the full request URL for debugging
                System.err.println("TMDbApiManager: Request URL: " + request.url());
                System.err.println("TMDbApiManager: Response Code: " + response.code());
                System.err.println("TMDbApiManager: Error Details: " + errorDetails);
                // Include the URL in the exception message
                throw new IOException("Unexpected response code: " + response.code() + " for URL: " + request.url() + ". Details: " + errorDetails);
            }
            ResponseBody body = response.body();
            return body != null ? body.string() : "";
        }
    }

    public CompletableFuture<String> makeRequestAsync(String endpoint) {
        // Use handle to process both success and failure, then propagate failure
        return CompletableFuture.supplyAsync(() -> {
            try {
                return makeRequest(endpoint);
            } catch (IOException e) {
                // Wrap the IOException in a RuntimeException or a custom exception
                // to allow CompletableFuture's exceptionally/handle to catch it.
                throw new RuntimeException("API request failed for endpoint: " + endpoint, e);
            }
        }, executor);
    }

    public CompletableFuture<List<Content>> fetchAsMoviestarContentList(String endpoint, @Nullable ContentType expectedType) {
        return makeRequestAsync(endpoint).thenApplyAsync(responseString -> {
            List<Content> contentList = new ArrayList<>();
            if (responseString == null || responseString.isEmpty()) {
                System.err.println("TMDbApiManager: Empty or null response for endpoint: " + endpoint);
                return contentList; // Return empty list on empty response
            }
            try {
                JsonObject jsonResponse = JsonParser.parseString(responseString).getAsJsonObject();
                JsonArray resultsArray = jsonResponse.getAsJsonArray("results");

                if (resultsArray != null) {
                    for (JsonElement element : resultsArray) {
                        if (element.isJsonObject()) {
                            JsonObject tmdbObjectJson = element.getAsJsonObject();
                            // Basic validation: must have an ID
                            if (!tmdbObjectJson.has("id") || tmdbObjectJson.get("id").isJsonNull()) {
                                System.err.println("TMDbApiManager: Skipping content item with no ID in response from " + endpoint);
                                continue;
                            }
                            int id = tmdbObjectJson.get("id").getAsInt();

                            ContentType currentItemType = expectedType;
                            if (expectedType == ContentType.UNKNOWN) {
                                String mediaTypeStr = getStringOrNull(tmdbObjectJson, "media_type");
                                if ("movie".equalsIgnoreCase(mediaTypeStr)) currentItemType = ContentType.MOVIE;
                                else if ("tv".equalsIgnoreCase(mediaTypeStr)) currentItemType = ContentType.TV;
                                else {
                                    // Log unknown media types
                                    System.err.println("TMDbApiManager: Skipping content item with unknown media_type '" + mediaTypeStr + "' for ID " + id + " from " + endpoint);
                                    continue; // Skip items with unknown media type
                                }
                            }
                            if (currentItemType == null) {
                                System.err.println("TMDbApiManager: Skipping content item with null determined type for ID " + id + " from " + endpoint);
                                continue;
                            }

                            Content content = cacheManager.get(id);
                            if (content == null) {
                                content = new Content();
                                content.setId(id);
                                // Populate basic fields from the list result
                                content.setPlot(getStringOrNull(tmdbObjectJson, "overview"));
                                content.setRating(tmdbObjectJson.has("vote_average") && !tmdbObjectJson.get("vote_average").isJsonNull() ? tmdbObjectJson.get("vote_average").getAsDouble() : 0.0);
                                content.setPopularity(tmdbObjectJson.has("popularity") && !tmdbObjectJson.get("popularity").isJsonNull() ? (int) tmdbObjectJson.get("popularity").getAsDouble() : 0);

                                String posterPath = getStringOrNull(tmdbObjectJson, "poster_path");
                                String backdropPath = getStringOrNull(tmdbObjectJson, "backdrop_path");
                                // Use DEFAULT_IMAGE_SIZE for list items
                                content.setImageUrl(getImageUrl(backdropPath, DEFAULT_IMAGE_SIZE));
                                content.setPosterUrl(getImageUrl(posterPath, DEFAULT_IMAGE_SIZE));

                                String releaseDateStr = null;
                                String title = null;
                                String originalTitle = null;

                                switch (currentItemType) {
                                    case MOVIE:
                                        title = getStringOrNull(tmdbObjectJson, "title");
                                        originalTitle = getStringOrNull(tmdbObjectJson, "original_title");
                                        releaseDateStr = getStringOrNull(tmdbObjectJson, "release_date");
                                        content.setIsSeries(false);
                                        break;
                                    case TV:
                                        title = getStringOrNull(tmdbObjectJson, "name");
                                        originalTitle = getStringOrNull(tmdbObjectJson, "original_name");
                                        releaseDateStr = getStringOrNull(tmdbObjectJson, "first_air_date");
                                        content.setIsSeries(true);
                                        break;
                                }
                                content.setTitle(title);
                                content.setOriginalTitle(originalTitle);
                                content.setReleaseDate(releaseDateStr);

                                if (releaseDateStr != null && !releaseDateStr.isEmpty()) {
                                    try {
                                        // Use a more flexible date parser if needed, but ISO_LOCAL_DATE is common
                                        LocalDate date = LocalDate.parse(releaseDateStr, DateTimeFormatter.ISO_LOCAL_DATE);
                                        content.setYear(date.getYear());
                                    } catch (DateTimeParseException e) {
                                        System.err.println("TMDbApiManager: Failed to parse date '" + releaseDateStr + "' for content ID " + id + ": " + e.getMessage());
                                        content.setYear(0); // Set year to 0 on parse failure
                                    }
                                } else { content.setYear(0); }

                                if (tmdbObjectJson.has("genre_ids") && tmdbObjectJson.get("genre_ids").isJsonArray()) {
                                    JsonArray genreIdsArray = tmdbObjectJson.getAsJsonArray("genre_ids");
                                    List<Integer> categoryIds = new ArrayList<>();
                                    for (JsonElement genreIdElement : genreIdsArray) {
                                        if (genreIdElement.isJsonPrimitive() && genreIdElement.getAsJsonPrimitive().isNumber()) {
                                            categoryIds.add(genreIdElement.getAsInt());
                                        }
                                    }
                                    content.setCategories(categoryIds);
                                }
                                // Video URL is typically fetched with full details, not list results
                                // content.setVideoUrl("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4"); // Default placeholder

                                cacheManager.put(content); // Cache the partially populated content
                            }
                            contentList.add(content);
                        }
                    }
                }
            } catch (Exception e) {
                // Log the exception details including the endpoint and response snippet
                String responseSnippet = responseString.length() > 500 ? responseString.substring(0, 500) + "..." : responseString;
                System.err.println("TMDbApiManager: Failed to parse/map content list from JSON for endpoint: " + endpoint + ". Error: " + e.getMessage() + ". Response snippet: " + responseSnippet);
                // Do NOT re-throw here, just return the potentially empty list.
                // The caller should handle the case of an empty list.
            }
            return contentList;
        }, executor); // Use the API manager's executor for processing
    }


    private String getStringOrNull(JsonObject jsonObject, String memberName) {
        if (jsonObject != null && jsonObject.has(memberName) && !jsonObject.get(memberName).isJsonNull()) {
            JsonElement element = jsonObject.get(memberName);
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                return element.getAsString();
            }
            // Handle cases where the member exists but is not a string primitive (e.g., number, boolean, object, array)
            System.err.println("TMDbApiManager: Expected string for member '" + memberName + "', but found " + element.getClass().getSimpleName());
        }
        return null;
    }

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
            // Return a completed future, but log the error. Or return a failed future.
            // Returning a failed future is better if the caller needs to know this failed.
            return CompletableFuture.failedFuture(new IllegalStateException("ContentDao not initialized."));
        }

        List<CompletableFuture<List<Content>>> futures = List.of(
                getPopularMoviesAsMoviestarContent(),
                getTrendingMoviesAsMoviestarContent("day"),
                getPopularTvShowsAsMoviestarContent(),
                getTrendingTvShowsAsMoviestarContent("day")
        );

        // Use handle to collect results or exceptions from all futures
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenComposeAsync(voidResult -> {
                    List<Content> allContentFromApi = futures.stream()
                            .flatMap(future -> {
                                try {
                                    // Get the result. If the future completed exceptionally, join() will throw.
                                    return future.join().stream();
                                } catch (Exception e) {
                                    // Log the error for this specific future but allow others to proceed
                                    System.err.println("TMDbApiManager: Error fetching content for one category: " + e.getMessage());
                                    return Stream.empty(); // Return empty stream for this failed category
                                }
                            })
                            .filter(Objects::nonNull).filter(c -> c.getId() != 0)
                            .filter(distinctByKey(Content::getId)) // Filter out duplicates by ID
                            .collect(Collectors.toList());

                    if (!allContentFromApi.isEmpty()) {
                        try {
                            // Perform database insert on the executor thread pool
                            contentDao.insertContents(allContentFromApi);
                            return CompletableFuture.completedFuture(null); // Indicate success
                        } catch (Exception e) {
                            System.err.println("TMDbApiManager: Error during DAO insert: " + e.getMessage());
                            // Return a failed future if the database operation fails
                            return CompletableFuture.failedFuture(e);
                        }
                    } else {
                        System.out.println("TMDbApiManager: No content fetched from API to update database.");
                        return CompletableFuture.completedFuture(null); // Indicate success (no content to insert)
                    }
                }, executor); // Perform the stream processing and DAO insert on the executor
    }

    // Helper to filter distinct elements in a stream
    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    public CompletableFuture<List<Content>> searchMultiContent(String query, int page) {
        if (query == null || query.trim().isEmpty()) {
            return CompletableFuture.completedFuture(new ArrayList<>()); // Return empty list for empty query
        }
        String encodedQuery;
        try {
            encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            System.err.println("TMDbApiManager: UTF-8 encoding error for query '" + query + "': " + e.getMessage());
            // Return a failed future to signal the encoding error
            return CompletableFuture.failedFuture(new RuntimeException("Failed to encode query.", e));
        }
        String endpointWithQuery = "/search/multi?query=" + encodedQuery + "&page=" + page + "&include_adult=false";
        return fetchAsMoviestarContentList(endpointWithQuery, ContentType.UNKNOWN);
    }

    @Nullable
    public String getImageUrl(@Nullable String imagePath, String size) {
        if (imagePath == null || imagePath.trim().isEmpty() || "null".equalsIgnoreCase(imagePath.trim())) {
            return null; // Return null if path is null, empty, or "null" string
        }
        // Ensure size is not null or empty, use default if needed
        String finalSize = (size != null && !size.trim().isEmpty()) ? size : DEFAULT_IMAGE_SIZE;
        // Ensure path starts with '/'
        String finalImagePath = imagePath.startsWith("/") ? imagePath : "/" + imagePath;
        return IMAGE_BASE_URL + finalSize + finalImagePath;
    }

    // Add a setCast method to Content model
    // (This requires modifying Content.java)
}