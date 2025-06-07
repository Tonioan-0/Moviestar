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

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
//Hint for the prof: we have ordered all methods in a way they could be easily readable collapsed
public class TMDbApiManager {

    // Constants
    private static final String BASE_URL = "https://api.themoviedb.org/3";
    private static final String API_KEY = "";
    private static final String LANGUAGE = "en-US"; // The language we have chose for owr project (the content in it-IT )
    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/";
    private static final String DEFAULT_IMAGE_SIZE = "w500";
    private static final String BACKDROP_IMAGE_SIZE_DETAILS = "w1280"; // For film scene background
    private static final String STILL_IMAGE_SIZE = "w300"; // For episode stills

    // Enum and classes to make code more readable and usable
    public enum ContentType {
        MOVIE, TV, UNKNOWN
    }
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

        public String getFullPosterUrl(String size) {
            return TMDbApiManager.getInstance().getImageUrl(posterPath, size);
        }
    }
    public static class ApiEpisodeDetails {
        public int episodeNumber;
        public String name;
        public String overview;
        public String stillPath;
        public int runtime; // in minutes
        public String airDate;
        public double voteAverage;
        public int id;

        public String getFullStillUrl(String size) {
            return TMDbApiManager.getInstance().getImageUrl(stillPath, size);
        }
    }

    // Singleton instance
    private static volatile TMDbApiManager  instance;
    private final OkHttpClient client;
    private final ExecutorService executor;
    private ContentDao contentDao;
    private final ContentCacheManager cacheManager;

    // Constructor and singleton implementation
    private TMDbApiManager() {
        this.client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor())
                .build();
        this.executor = Executors.newFixedThreadPool(10);
        this.cacheManager = ContentCacheManager.getInstance();
    }
    public static TMDbApiManager getInstance() {
        if (instance == null) {
            synchronized (TMDbApiManager.class) {
                if (instance == null) {
                    instance =  new TMDbApiManager();
                }
            }
        }
        return instance;
    }
    public ExecutorService getExecutor() {
        return executor;
    }
    private static class AuthInterceptor implements Interceptor {
        @NotNull
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();
            HttpUrl originalHttpUrl = original.url();
            HttpUrl url = originalHttpUrl.newBuilder().build(); // No need to add API key as query param with Bearer token
            Request.Builder requestBuilder  = original.newBuilder()
                    .url(url)
                    .header("accept", "application/json")
                    // Use the Bearer token method which is preferred by TMDb v3/v4
                    .header("Authorization", "Bearer " + API_KEY);

            Request request = requestBuilder.build();

            return chain.proceed(request);
        }
    }

    //Requests
    private Request buildRequest(String endpointPathAndQuery) {
        HttpUrl parsedUrl  = HttpUrl.parse(BASE_URL + endpointPathAndQuery);
        if (parsedUrl == null) {
            // Log the malformed URL and throw
            System.err.println( "TMDbApiManager: Malformed URL attempted: " + BASE_URL + endpointPathAndQuery);
            throw new IllegalArgumentException( "Malformed URL for endpoint: " + endpointPathAndQuery);
        }
        HttpUrl.Builder urlBuilder = parsedUrl.newBuilder();
        // Add language parameter unless it's an image URL or already present
        if (!endpointPathAndQuery.startsWith( "/t/p/") && parsedUrl.queryParameter("language") ==  null) {
            urlBuilder.addQueryParameter("language", LANGUAGE);
        }
        return new Request.Builder().url(urlBuilder.build()).get().build();
    }
    public String makeRequest(String endpoint) throws IOException {
        Request request = buildRequest(endpoint);
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                ResponseBody errorBody = response.body();
                String errorDetails = errorBody != null ?  errorBody.string() : "No error body";
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

    public CompletableFuture<List<Content>> searchMultiContent(String query, int page) {
        if (query == null || query.trim().isEmpty()) {
            return CompletableFuture.completedFuture(new ArrayList<>()); // Return empty list for empty query
        }
        String encodedQuery;
        try {
            encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            System.err.println("TMDbApiManager: UTF-8 encoding error for query '" + query + "': " + e.getMessage());
            return CompletableFuture.failedFuture(new RuntimeException("Failed to encode query.", e));
        }
        String endpointWithQuery = "/search/multi?query=" + encodedQuery + "&page=" + page ;
        return fetchAsMoviestarContentList(endpointWithQuery, ContentType.UNKNOWN);
    }
    @Nullable
    public String getImageUrl(@Nullable String imagePath, String size) {
        if (imagePath == null || imagePath.trim().isEmpty() || "null".equalsIgnoreCase(imagePath.trim())) {
            return null;
        }
        String finalSize = (size != null && !size.trim().isEmpty()) ? size : DEFAULT_IMAGE_SIZE;
        String finalImagePath = imagePath.startsWith("/") ? imagePath : "/" + imagePath;
        return IMAGE_BASE_URL + finalSize + finalImagePath;
    }

    //Receives
    public CompletableFuture<List<Content>> fetchAsMoviestarContentList(String endpoint, @Nullable ContentType expectedType) {
        return makeRequestAsync(endpoint).thenApplyAsync(responseString -> {
            List<Content> contentList = new ArrayList<>();
            if (responseString == null || responseString.isEmpty()) {
                System.err.println("TMDbApiManager: Empty or null response for endpoint: " + endpoint);
                return contentList;
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
                                    continue;
                                }
                            }
                            if (currentItemType == null) {
                                continue;
                            }
                            // We have noted the api has a little problem, everyone can add their films, to prevent this problem, and give at the users a clear page and a correct experience
                            if (tmdbObjectJson.get("vote_count").getAsDouble()<500||tmdbObjectJson.get("vote_average").isJsonNull())
                                continue;
                            Content content = cacheManager.get(id);
                            if (content == null) {
                                content = new Content();
                                content.setId(id);
                                // Populate basic fields from the list result
                                content.setPlot(getStringOrNull(tmdbObjectJson, "overview"));
                                content.setRating(tmdbObjectJson.get("vote_average").getAsDouble());
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
                                content.setVideoUrl("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4"); // Default placeholder

                                cacheManager.put(content); // Cache the partially populated content

                            }
                            contentList.add(content);
                        }
                    }
                }
            } catch (Exception e) {
                String responseSnippet = responseString.length() > 500 ? responseString.substring(0, 500) + "..." : responseString;
                System.err.println("TMDbApiManager: Failed to parse/map content list from JSON for endpoint: " + endpoint + ". Error: " + e.getMessage() + ". Response snippet: " + responseSnippet);
            }
            if (contentDao != null && !contentList.isEmpty()) {
                CompletableFuture.runAsync(() -> {
                    try {
                        contentDao.insertContents(contentList);
                        System.out.println("TMDbApiManager: Successfully inserted " + contentList.size() + " contents to database asynchronously.");
                    } catch (Exception e) {
                        System.err.println("TMDbApiManager: Error inserting contents to database: " + e.getMessage());
                    }
                }, executor);
            }
            return contentList;
        }, executor);
    }
    private String getStringOrNull(JsonObject jsonObject, String memberName) {
        if (jsonObject != null && jsonObject.has(memberName) && !jsonObject.get(memberName).isJsonNull()) {
            JsonElement element = jsonObject.get(memberName);
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                return element.getAsString();
            }
            System.err.println("TMDbApiManager: Expected string for member '" + memberName + "', but found " + element.getClass().getSimpleName());
        }
        return null;
    }
    public void setContentDao(ContentDao contentDao) {
        this.contentDao = contentDao;
    }
    public CompletableFuture<Void> updateAllContentInDatabase() {
        if (contentDao == null) {
            System.err.println("TMDbApiManager: ContentDao not initialized. Cannot update database.");
            return CompletableFuture.failedFuture(new IllegalStateException("ContentDao not initialized."));
        }
        contentDao.deleteExpiredContent();
        List<CompletableFuture<List<Content>>> futures = List.of(
                getDiscoverFilmsAsMoviestarContent(1),
                getDiscoverTvShowsAsMoviestarContent(1),
                getDiscoverFilmsAsMoviestarContent(2),
                getDiscoverTvShowsAsMoviestarContent(2),
                getDiscoverFilmsAsMoviestarContent(3),
                getDiscoverTvShowsAsMoviestarContent(3)
        );

        // Use handle to collect results or exceptions from all futures
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenComposeAsync(voidResult -> {
                    List<Content> allContentFromApi = futures.stream()
                            .flatMap(future -> {
                                try {
                                    return future.join().stream();
                                } catch (Exception e) {
                                    System.err.println("TMDbApiManager: Error fetching content for one category: " + e.getMessage());
                                    return Stream.empty();
                                }
                            })
                            .filter(Objects::nonNull).filter(c -> c.getId() != 0)
                            .filter(distinctByKey(Content::getId))
                            .toList();

                    if (!allContentFromApi.isEmpty()) {
                        try {
                            return CompletableFuture.completedFuture(null); // Indicate success
                        } catch (Exception e) {
                            System.err.println("TMDbApiManager: Error during DAO insert: " + e.getMessage());
                            return CompletableFuture.failedFuture(e);
                        }
                    } else {
                        System.out.println("TMDbApiManager: No content fetched from API to update database.");
                        return CompletableFuture.completedFuture(null); // Indicate success (no content to insert)
                    }
                }, executor);
    }

    private CompletableFuture<List<Content>> getDiscoverTvShowsAsMoviestarContent(int page) {
        return fetchAsMoviestarContentList("/discover/tv?include_adult=false&sort_by=popularity.desc&page=" + page, ContentType.TV);

    }

    private CompletableFuture<List<Content>> getDiscoverFilmsAsMoviestarContent(int page) {
        return fetchAsMoviestarContentList("/discover/movie?include_adult=false&sort_by=popularity.desc&page=" + page, ContentType.MOVIE);
    }

    // Helper to filter distinct elements in a stream
    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }



}