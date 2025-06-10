package com.esa.moviestar.model;

import java.util.List;
import java.util.ArrayList;

public class FilmSeriesDetails{
    private int id; // TMDb ID of the movie or TV show
    private String title;
    private String plot;

    //Basic movie and season details
    private int movieRuntime;
    private int numberOfSeasons;
    private List<SeasonDetails> seasons;//Complete number of seasons for the show
    private int year; //Release year or first air year
    private String releaseDate; //Release date or first air date string
    private boolean isSeries;//Essential to check between series and movies
    private List<String> genreNames;
    private String cast; // Comma-separated string or similar
    private String productionName; // Primary production company name

    //Basic urls
    private String posterUrl;   //poster image URL for the show/movie
    private String backdropUrl; //backdrop image URL for the show/movie
    private String videoUrl;
    //Constructor
    public FilmSeriesDetails(){
        //Setting up all seasons to display them to the user
        this.seasons = new ArrayList<>();
        //Setting up all the genres to display them to the user
        this.genreNames = new ArrayList<>();

    }
    //Switching from season
    public void setSeasonAtIndex(int index, SeasonDetails season) {
        if (this.seasons == null)this.seasons = new ArrayList<>();
        while (this.seasons.size() <= index) {
            this.seasons.add(null);
        }

        this.seasons.set(index, season);
    }
    public void initializeSeasonsList(int size) {
        if (this.seasons == null)this.seasons = new ArrayList<>();
        this.seasons.clear();
        for (int i = 0; i < size; i++) {
            this.seasons.add(null);
        }
    }


    //Getters and setters
    //Content year
    public int getYear(){
        return year;
    }
    //Content cast
    public String getCast(){
        return cast;
    }
    public void setCast(String cast) { this.cast = cast; }
    //Content production name
    public String getProductionName() { return productionName; }
    public void setProductionName(String productionName) { this.productionName = productionName; }
    public void setYear(int year){
        this.year = year;}
    //Plot
    public String getPlot() {return plot;}

    public void setPlot(String plot) {this.plot = plot;}

    //Content id
    public int getContentId() { return id; }

    public void setContentId(int id) { this.id = id; }

    //Content title
    public String getContentTitle() { return title; }

    public void setContentTitle(String title) { this.title = title; }
    //Video url
    public String getVideoUrl() {return videoUrl;}

    public void setVideoUrl(String videoUrl) {this.videoUrl = videoUrl;}


    //Content poster
    public String getPosterUrl() { return posterUrl; }

    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
    //Content backdrop
    public String getBackdropUrl() { return backdropUrl; }

    public void setBackdropUrl(String backdropUrl) { this.backdropUrl = backdropUrl; }

    //Content release date
    public String getReleaseDate() { return releaseDate; }

    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }
    //Essential content type check
    public boolean isSeries() { return isSeries; }

    public void setSeries(boolean series) { isSeries = series; }

    //Series number of seasons
    public int getNumberOfSeasons() { return numberOfSeasons; }

    public void setNumberOfSeasons(int numberOfSeasons) { this.numberOfSeasons = numberOfSeasons; }
    //Movie runtime
    public void setMovieRuntime(int movieRuntime){this.movieRuntime=movieRuntime;}

    public int getMovieRuntime(){return movieRuntime;}


    //Genre names
    public List<String> getGenreNames() {return genreNames;}
    //Used for the film scene ui
    public void setGenreNames(List<String> genreNames) {this.genreNames = genreNames;}

    public List<SeasonDetails> getSeasons() { return seasons; }



    public void setSeasons(List<SeasonDetails> seasons) { this.seasons = seasons; }
    public void addGenreName(String genreName) {
        if (this.genreNames == null) this.genreNames = new ArrayList<>();
        this.genreNames.add(genreName);
    }
    public void addSeason(SeasonDetails season) {
        if (this.seasons == null) this.seasons = new ArrayList<>();
        this.seasons.add(season);
    }


    //Subclasses for seasons and episodes
    public static class SeasonDetails {
        //Season number section
        private int seasonNumber;
        public int getSeasonNumber() { return seasonNumber; }
        public void setSeasonNumber(int seasonNumber) { this.seasonNumber = seasonNumber; }
        //Season title section
        private String name; // Name of the season
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        //Season plot section
        private String overview;
        public String getOverview() { return overview; }
        public void setOverview(String overview) { this.overview = overview; }
        //Season thumbnail section
        private String posterUrl; // Season-specific poster
        public String getPosterUrl() { return posterUrl; }

        public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
        //Season air date section
        private String airDate;
        public String getAirDate() { return airDate; }
        public void setAirDate(String airDate) { this.airDate = airDate; }

        private List<EpisodeDetails> episodes;
        public List<EpisodeDetails> getEpisodes() { return episodes; }
        public void setEpisodes(List<EpisodeDetails> episodes) { this.episodes = episodes; }
        public void addEpisode(EpisodeDetails episode) {
            if (this.episodes == null) this.episodes = new ArrayList<>();
            this.episodes.add(episode);
        }
        //Constructor only used to initialize episode list
        public SeasonDetails() {
            this.episodes = new ArrayList<>();
        }

    }

    //Episodes for series setting class
    public static class EpisodeDetails {
        private int episodeNumber;
        private String name;
        private String overview;
        private String stillUrl; // Episode thumbnail
        private int runtimeMinutes;
        private String videoUrl;

        public EpisodeDetails() {}
        //Subclass episode details section getter and setters

        //Episode index
        public int getEpisodeNumber() { return episodeNumber; }

        public void setEpisodeNumber(int episodeNumber) { this.episodeNumber = episodeNumber; }
        //Episode title
        public String getName() { return name; }

        public void setName(String name) { this.name = name; }
        //Episode plot
        public String getOverview() { return overview; }

        public void setOverview(String overview) { this.overview = overview; }
        //Episode thumbnail
        public String getStillUrl() { return stillUrl; }

        public void setStillUrl(String stillUrl) { this.stillUrl = stillUrl; }
        //Episode runtime duration
        public int getRuntimeMinutes() { return runtimeMinutes; }

        public void setRuntimeMinutes(int runtimeMinutes) { this.runtimeMinutes = runtimeMinutes; }

        public String getVideoUrl() {
            return videoUrl;
        }

        public void setVideoUrl(String videoUrl) {
            this.videoUrl = videoUrl;
        }
    }
}