package com.esa.moviestar.model;

import java.util.List;
import java.util.ArrayList;

public class FilmSeriesDetails {
    private int id; // TMDb ID of the movie or TV show
    private String title;
    private String plot; // Overview
    private String posterUrl;   // Main poster image URL for the show/movie
    private String backdropUrl; // Main backdrop image URL for the show/movie
    private String videoUrl;
    private int movieRuntime;

    private int year; // Release year or first air year
    private String releaseDate; // Full release date or first air date string

    private boolean isSeries;

    // Fields specific to TV series
    private int numberOfSeasons; // Total number of seasons for the show

    private List<String> genreNames;
    private String cast; // Comma-separated string or similar
    private String productionName; // Primary production company name


    // Detailed season and episode information for TV series
    private List<SeasonDetails> seasons;

    public FilmSeriesDetails() {
        this.genreNames = new ArrayList<>();
        this.seasons = new ArrayList<>();
    }
    public void initializeSeasonsList(int size) {
        if (this.seasons == null) {
            this.seasons = new ArrayList<>();
        }

        // Clear existing seasons and initialize with null values
        this.seasons.clear();
        for (int i = 0; i < size; i++) {
            this.seasons.add(null);
        }
    }

    /**
     * Set a season at a specific index in the seasons list
     * @param index The index to set the season at
     * @param season The season details to set
     */
    public void setSeasonAtIndex(int index, SeasonDetails season) {
        if (this.seasons == null) {
            this.seasons = new ArrayList<>();
        }

        // Ensure the list is large enough
        while (this.seasons.size() <= index) {
            this.seasons.add(null);
        }

        // Set the season at the specified index
        this.seasons.set(index, season);
    }

    // --- Getters and Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPlot() { return plot; }
    public void setPlot(String plot) { this.plot = plot; }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    public String getBackdropUrl() { return backdropUrl; }
    public void setBackdropUrl(String backdropUrl) { this.backdropUrl = backdropUrl; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }

    public boolean isSeries() { return isSeries; }
    public void setSeries(boolean series) { isSeries = series; }

    public int getNumberOfSeasons() { return numberOfSeasons; }
    public void setNumberOfSeasons(int numberOfSeasons) { this.numberOfSeasons = numberOfSeasons; }

    public void setMovieRuntime(int movieRuntime){this.movieRuntime=movieRuntime;}
    public int getMovieRuntime(){return movieRuntime;}


    public List<String> getGenreNames() { return genreNames; }
    public void setGenreNames(List<String> genreNames) { this.genreNames = genreNames; }
    public void addGenreName(String genreName) {
        if (this.genreNames == null) this.genreNames = new ArrayList<>();
        this.genreNames.add(genreName);
    }

    public String getCast() { return cast; }
    public void setCast(String cast) { this.cast = cast; }

    public String getProductionName() { return productionName; }
    public void setProductionName(String productionName) { this.productionName = productionName; }


    public List<SeasonDetails> getSeasons() { return seasons; }
    public void setSeasons(List<SeasonDetails> seasons) { this.seasons = seasons; }
    public void addSeason(SeasonDetails season) {
        if (this.seasons == null) this.seasons = new ArrayList<>();
        this.seasons.add(season);
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    // --- Inner Classes for Season and Episode Details ---

    public static class SeasonDetails {
        private int seasonNumber;
        private String name; // Name of the season
        private String overview;
        private String posterUrl; // Season-specific poster
        private String airDate;
        private List<EpisodeDetails> episodes;

        public SeasonDetails() {
            this.episodes = new ArrayList<>();
        }

        // --- Getters and Setters for SeasonDetails ---
        public int getSeasonNumber() { return seasonNumber; }
        public void setSeasonNumber(int seasonNumber) { this.seasonNumber = seasonNumber; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getOverview() { return overview; }
        public void setOverview(String overview) { this.overview = overview; }

        public String getPosterUrl() { return posterUrl; }
        public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

        public String getAirDate() { return airDate; }
        public void setAirDate(String airDate) { this.airDate = airDate; }

        public List<EpisodeDetails> getEpisodes() { return episodes; }
        public void setEpisodes(List<EpisodeDetails> episodes) { this.episodes = episodes; }
        public void addEpisode(EpisodeDetails episode) {
            if (this.episodes == null) this.episodes = new ArrayList<>();
            this.episodes.add(episode);
        }
    }

    public static class EpisodeDetails {
        private int episodeNumber;
        private String name;
        private String overview;
        private String stillUrl; // Episode thumbnail
        private int runtimeMinutes;
        // Add other fields if needed, like airDate, voteAverage for episodes

        public EpisodeDetails() {}

        // --- Getters and Setters for EpisodeDetails ---
        public int getEpisodeNumber() { return episodeNumber; }
        public void setEpisodeNumber(int episodeNumber) { this.episodeNumber = episodeNumber; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getOverview() { return overview; }
        public void setOverview(String overview) { this.overview = overview; }

        public String getStillUrl() { return stillUrl; }
        public void setStillUrl(String stillUrl) { this.stillUrl = stillUrl; }

        public int getRuntimeMinutes() { return runtimeMinutes; }
        public void setRuntimeMinutes(int runtimeMinutes) { this.runtimeMinutes = runtimeMinutes; }
    }
}