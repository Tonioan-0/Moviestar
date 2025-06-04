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
    private static final String API_KEY = ""; // IMPORTANT: Set your TMDb API Key here!
    private static final String LANGUAGE = "en-US"; // Default language
    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/";
    private static final String DEFAULT_IMAGE_SIZE = "w500";
    private static final String BACKDROP_IMAGE_SIZE_DETAILS = "w1280"; // For film scene background
    private static final String STILL_IMAGE_SIZE = "w300"; // For episode stills


    private static volatile TMDbApiManager instance;
    private final OkHttpClient client;
    private final ExecutorService executor;
    private ContentDao contentDao;
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
            return TMDbApiManager.getInstance().getImageUrl(stillPath, size);
        }
    }


    private TMDbApiManager() {
        this.client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor())
                .build();
        this.executor = Executors.newCachedThreadPool();
        this.cacheManager = ContentCacheManager.getInstance();
    }

    public static TMDbApiManager getInstance() {
        if (API_KEY == null || API_KEY.trim().isEmpty()) {
            System.err.println("FATAL ERROR: TMDb API Key is not set in TMDbApiManager.java");
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
            HttpUrl url = originalHttpUrl.newBuilder().build();
            Request.Builder requestBuilder = original.newBuilder()
                    .url(url)
                    .header("accept", "application/json")
                    .header("Authorization", "Bearer " + API_KEY);
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
        if (!endpointPathAndQuery.startsWith(IMAGE_BASE_URL) && parsedUrl.queryParameter("language") == null) {
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
                System.err.println("TMDbApiManager: API request failed for endpoint: " + endpoint + ". Error: " + e.getMessage());
                throw new RuntimeException("API request failed for endpoint: " + endpoint, e);
            }
        }, executor);
    }

    public CompletableFuture<List<Content>> fetchAsMoviestarContentList(String endpoint, @Nullable ContentType expectedType) {
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
                            JsonObject tmdbObjectJson = element.getAsJsonObject();
                            int id = tmdbObjectJson.has("id") ? tmdbObjectJson.get("id").getAsInt() : 0;
                            if (id == 0) continue;

                            ContentType currentItemType = expectedType;
                            if (expectedType == ContentType.UNKNOWN) {
                                String mediaTypeStr = getStringOrNull(tmdbObjectJson, "media_type");
                                if ("movie".equalsIgnoreCase(mediaTypeStr)) currentItemType = ContentType.MOVIE;
                                else if ("tv".equalsIgnoreCase(mediaTypeStr)) currentItemType = ContentType.TV;
                                else continue;
                            }
                            if (currentItemType == null) continue;

                            Content content = cacheManager.get(id);
                            if (content == null) {
                                content = new Content();
                                content.setId(id);
                                content.setPlot(getStringOrNull(tmdbObjectJson, "overview"));
                                content.setRating(tmdbObjectJson.has("vote_average") ? tmdbObjectJson.get("vote_average").getAsDouble() : 0.0);
                                content.setPopularity(tmdbObjectJson.has("popularity") ? (int) tmdbObjectJson.get("popularity").getAsDouble() : 0);

                                String posterPath = getStringOrNull(tmdbObjectJson, "poster_path");
                                String backdropPath = getStringOrNull(tmdbObjectJson, "backdrop_path");
                                content.setImageUrl(getImageUrl(backdropPath, DEFAULT_IMAGE_SIZE));
                                content.setPosterUrl(getImageUrl(posterPath, DEFAULT_IMAGE_SIZE));

                                String releaseDateStr = null;
                                String title = null;

                                switch (currentItemType) {
                                    case MOVIE:
                                        title = getStringOrNull(tmdbObjectJson, "title");
                                        releaseDateStr = getStringOrNull(tmdbObjectJson, "release_date");
                                        content.setIsSeries(false);
                                        break;
                                    case TV:
                                        title = getStringOrNull(tmdbObjectJson, "name");
                                        releaseDateStr = getStringOrNull(tmdbObjectJson, "first_air_date");
                                        content.setIsSeries(true);
                                        break;
                                }
                                content.setTitle(title);
                                content.setOriginalTitle(getStringOrNull(tmdbObjectJson, (currentItemType == ContentType.TV ? "original_name" : "original_title")));
                                content.setReleaseDate(releaseDateStr);

                                if (releaseDateStr != null && !releaseDateStr.isEmpty()) {
                                    try {
                                        content.setYear(LocalDate.parse(releaseDateStr, DateTimeFormatter.ISO_LOCAL_DATE).getYear());
                                    } catch (DateTimeParseException e) { content.setYear(0); }
                                } else { content.setYear(0); }

                                if (tmdbObjectJson.has("genre_ids") && tmdbObjectJson.get("genre_ids").isJsonArray()) {
                                    JsonArray genreIdsArray = tmdbObjectJson.getAsJsonArray("genre_ids");
                                    List<Integer> categoryIds = new ArrayList<>();
                                    for (JsonElement genreIdElement : genreIdsArray) categoryIds.add(genreIdElement.getAsInt());
                                    content.setCategories(categoryIds);
                                }
                                content.setVideoUrl("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4"); // Default
                                cacheManager.put(content);
                            }
                            contentList.add(content);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("TMDbApiManager: Failed to parse/map content from JSON for: " + endpoint + ". Error: " + e.getMessage() + ". Response snippet: " + responseString.substring(0, Math.min(responseString.length(), 500)));
            }
            return contentList;
        }, executor);
    }

    public CompletableFuture<Content> fetchFullContentDetails(int contentId, boolean isSeries) {
        String typePath = isSeries ? "tv" : "movie";
        String endpoint = "/" + typePath + "/" + contentId + "?append_to_response=credits,videos";
        return makeRequestAsync(endpoint).thenApplyAsync(responseString -> {
            if (responseString == null || responseString.isEmpty()) {
                System.err.println("TMDbApiManager: Empty response for full details: " + endpoint);
                throw new RuntimeException("Empty response for full details: " + contentId);
            }
            try {
                JsonObject tmdbObjectJson = JsonParser.parseString(responseString).getAsJsonObject();
                Content content = cacheManager.get(contentId);
                if (content == null) {
                    content = new Content();
                    content.setId(contentId);
                }

                content.setIsSeries(isSeries);
                content.setPlot(getStringOrNull(tmdbObjectJson, "overview"));
                content.setRating(tmdbObjectJson.has("vote_average") ? tmdbObjectJson.get("vote_average").getAsDouble() : 0.0);
                content.setPopularity(tmdbObjectJson.has("popularity") ? (int) tmdbObjectJson.get("popularity").getAsDouble() : 0);

                String posterPath = getStringOrNull(tmdbObjectJson, "poster_path");
                String backdropPath = getStringOrNull(tmdbObjectJson, "backdrop_path");
                content.setImageUrl(getImageUrl(backdropPath, BACKDROP_IMAGE_SIZE_DETAILS));
                content.setPosterUrl(getImageUrl(posterPath, DEFAULT_IMAGE_SIZE));

                List<String> genreNames = new ArrayList<>();
                if (tmdbObjectJson.has("genres") && tmdbObjectJson.get("genres").isJsonArray()) {
                    for (JsonElement genreElement : tmdbObjectJson.getAsJsonArray("genres")) {
                        genreNames.add(genreElement.getAsJsonObject().get("name").getAsString());
                    }
                }
                content.setGenreNames(genreNames);

                if (isSeries) {
                    content.setTitle(getStringOrNull(tmdbObjectJson, "name"));
                    content.setOriginalTitle(getStringOrNull(tmdbObjectJson, "original_name"));
                    String firstAirDate = getStringOrNull(tmdbObjectJson, "first_air_date");
                    content.setReleaseDate(firstAirDate);
                    if (firstAirDate != null && !firstAirDate.isEmpty()) {
                        try { content.setYear(LocalDate.parse(firstAirDate).getYear()); } catch (DateTimeParseException e) { content.setYear(0); }
                    }
                    content.setNumberOfSeasons(tmdbObjectJson.has("number_of_seasons") ? tmdbObjectJson.get("number_of_seasons").getAsInt() : 0);
                    content.setNumberOfEpisodes(tmdbObjectJson.has("number_of_episodes") ? tmdbObjectJson.get("number_of_episodes").getAsInt() : 0);
                    if (tmdbObjectJson.has("episode_run_time") && tmdbObjectJson.get("episode_run_time").isJsonArray()) {
                        JsonArray runtimes = tmdbObjectJson.getAsJsonArray("episode_run_time");
                        if (runtimes.size() > 0) content.setRuntimeMinutes(runtimes.get(0).getAsInt());
                    }
                } else { // Movie
                    content.setTitle(getStringOrNull(tmdbObjectJson, "title"));
                    content.setOriginalTitle(getStringOrNull(tmdbObjectJson, "original_title"));
                    String releaseDate = getStringOrNull(tmdbObjectJson, "release_date");
                    content.setReleaseDate(releaseDate);
                    if (releaseDate != null && !releaseDate.isEmpty()) {
                        try { content.setYear(LocalDate.parse(releaseDate).getYear()); } catch (DateTimeParseException e) { content.setYear(0); }
                    }
                    content.setRuntimeMinutes(tmdbObjectJson.has("runtime") && !tmdbObjectJson.get("runtime").isJsonNull() ? tmdbObjectJson.get("runtime").getAsInt() : 0);
                }

                if (tmdbObjectJson.has("production_countries") && tmdbObjectJson.get("production_countries").isJsonArray()) {
                    JsonArray countriesArray = tmdbObjectJson.getAsJsonArray("production_countries");
                    if (countriesArray.size() > 0) content.setCountry(countriesArray.get(0).getAsJsonObject().get("name").getAsString());
                }

                if (tmdbObjectJson.has("videos") && tmdbObjectJson.getAsJsonObject("videos").has("results")) {
                    JsonArray videos = tmdbObjectJson.getAsJsonObject("videos").getAsJsonArray("results");
                    for (JsonElement videoEl : videos) {
                        JsonObject videoObj = videoEl.getAsJsonObject();
                        if ("Trailer".equalsIgnoreCase(getStringOrNull(videoObj, "type")) && "YouTube".equalsIgnoreCase(getStringOrNull(videoObj, "site"))) {
                            content.setVideoUrl("https://www.youtube.com/watch?v=" + getStringOrNull(videoObj, "key"));
                            break;
                        }
                    }
                }
                if (content.getVideoUrl() == null || content.getVideoUrl().isEmpty() || !content.getVideoUrl().startsWith("https://www.youtube.com")) {
                    content.setVideoUrl("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4");
                }

                cacheManager.put(content);
                return content;
            } catch (Exception e) {
                System.err.println("TMDbApiManager: Failed to parse full details for ID: " + contentId + ". Error: " + e.getMessage());
                throw new RuntimeException("Failed to parse full details for ID: " + contentId, e);
            }
        }, executor);
    }

    public CompletableFuture<ApiSeasonDetails> fetchTvSeasonDetails(int seriesId, int seasonNumber) {
        String endpoint = "/tv/" + seriesId + "/season/" + seasonNumber;
        return makeRequestAsync(endpoint).thenApplyAsync(responseString -> {
            if (responseString == null || responseString.isEmpty()) {
                System.err.println("TMDbApiManager: Empty response for TV season: " + endpoint);
                throw new RuntimeException("Empty response for TV season " + seriesId + " S" + seasonNumber);
            }
            try {
                JsonObject seasonJson = JsonParser.parseString(responseString).getAsJsonObject();
                ApiSeasonDetails seasonDetails = new ApiSeasonDetails();
                seasonDetails.id = seasonJson.get("id").getAsInt();
                seasonDetails.seasonNumber = seasonJson.get("season_number").getAsInt();
                seasonDetails.name = getStringOrNull(seasonJson, "name");
                if (seasonDetails.name == null || seasonDetails.name.isEmpty()) {
                    seasonDetails.name = "Season " + seasonDetails.seasonNumber;
                }
                seasonDetails.overview = getStringOrNull(seasonJson, "overview");
                seasonDetails.posterPath = getStringOrNull(seasonJson, "poster_path");
                seasonDetails.airDate = getStringOrNull(seasonJson, "air_date");

                if (seasonJson.has("episodes") && seasonJson.get("episodes").isJsonArray()) {
                    for (JsonElement episodeElement : seasonJson.getAsJsonArray("episodes")) {
                        JsonObject epJson = episodeElement.getAsJsonObject();
                        ApiEpisodeDetails epDetails = new ApiEpisodeDetails();
                        epDetails.id = epJson.get("id").getAsInt();
                        epDetails.episodeNumber = epJson.get("episode_number").getAsInt();
                        epDetails.name = getStringOrNull(epJson, "name");
                        epDetails.overview = getStringOrNull(epJson, "overview");
                        epDetails.stillPath = getStringOrNull(epJson, "still_path");
                        epDetails.runtime = epJson.has("runtime") && !epJson.get("runtime").isJsonNull() ? epJson.get("runtime").getAsInt() : 0;
                        epDetails.airDate = getStringOrNull(epJson, "air_date");
                        epDetails.voteAverage = epJson.has("vote_average") ? epJson.get("vote_average").getAsDouble() : 0.0;
                        seasonDetails.episodes.add(epDetails);
                    }
                }
                return seasonDetails;
            } catch (Exception e) {
                System.err.println("TMDbApiManager: Failed to parse TV season for series " + seriesId + " S" + seasonNumber + ". Error: " + e.getMessage());
                throw new RuntimeException("Failed to parse TV season details", e);
            }
        }, executor);
    }

    private String getStringOrNull(JsonObject jsonObject, String memberName) {
        if (jsonObject.has(memberName) && !jsonObject.get(memberName).isJsonNull()) {
            return jsonObject.get(memberName).getAsString();
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
            return CompletableFuture.failedFuture(new IllegalStateException("ContentDao not initialized."));
        }

        List<CompletableFuture<List<Content>>> futures = List.of(
                getPopularMoviesAsMoviestarContent(),
                getTrendingMoviesAsMoviestarContent("day"),
                getPopularTvShowsAsMoviestarContent(),
                getTrendingTvShowsAsMoviestarContent("day")
        );

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenComposeAsync(voidResult -> {
                    List<Content> allContentFromApi = futures.stream()
                            .flatMap(future -> {
                                try { return future.join().stream(); }
                                catch (Exception e) {
                                    System.err.println("TMDbApiManager: Error joining future: " + e.getMessage());
                                    return Stream.empty();
                                }
                            })
                            .filter(Objects::nonNull).filter(c -> c.getId() != 0)
                            .filter(distinctByKey(Content::getId))
                            .collect(Collectors.toList());

                    if (!allContentFromApi.isEmpty()) {
                        try {
                            contentDao.insertContents(allContentFromApi);
                        } catch (Exception e) {
                            System.err.println("TMDbApiManager: Error during DAO insert: " + e.getMessage());
                            return CompletableFuture.failedFuture(e);
                        }
                    }
                    return CompletableFuture.completedFuture(null);
                }, executor);
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    public CompletableFuture<List<Content>> searchMultiContent(String query, int page) {
        String encodedQuery;
        try {
            encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            System.err.println("TMDbApiManager: UTF-8 encoding error: " + e.getMessage());
            return CompletableFuture.failedFuture(new RuntimeException("Failed to encode query.", e));
        }
        String endpointWithQuery = "/search/multi?query=" + encodedQuery + "&page=" + page + "&include_adult=false";
        return fetchAsMoviestarContentList(endpointWithQuery, ContentType.UNKNOWN);
    }

    @Nullable
    public String getImageUrl(@Nullable String imagePath, String size) {
        if (imagePath == null || imagePath.trim().isEmpty() || "null".equalsIgnoreCase(imagePath.trim())) {
            return null;
        }
        return IMAGE_BASE_URL + size + (imagePath.startsWith("/") ? imagePath : "/" + imagePath);
    }
}