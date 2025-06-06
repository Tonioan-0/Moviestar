package com.esa.moviestar.model;

import java.util.ArrayList;
import java.util.List;

public class Content {
    private int id;
    private String title;
    private String originalTitle;
    private String plot;
    private String imageUrl;    // Typically backdrop URL
    private String posterUrl;   // Poster URL
    private String videoUrl;

    private int runtimeMinutes;

    private int year;
    private double rating;
    private int popularity;
    private String country;
    private String releaseDate;

    private boolean isSeries;

    private int numberOfSeasons;
    private int numberOfEpisodes; // Total episodes in the series

    private List<Integer> categories;
    private List<String> genreNames;


    public Content() {
        this.categories = new ArrayList<>();
        this.genreNames = new ArrayList<>();
    }

    // --- Standard Getters and Setters ---
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getPlot() {
        return plot;
    }

    public void setPlot(String plot) {
        this.plot = plot;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public int getRuntimeMinutes() {
        return runtimeMinutes;
    }

    public void setRuntimeMinutes(int runtimeMinutes) {
        this.runtimeMinutes = runtimeMinutes;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getPopularity() {
        return popularity;
    }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public boolean isSeries() {
        return this.isSeries;
    }

    // Renamed for convention
    public void setIsSeries(boolean isSeries) {
        this.isSeries = isSeries;
    }

    public int getNumberOfSeasons() {
        return numberOfSeasons;
    }

    public void setNumberOfSeasons(int numberOfSeasons) {
        this.numberOfSeasons = numberOfSeasons;
    }

    public int getNumberOfEpisodes() {
        return numberOfEpisodes;
    }

    public void setNumberOfEpisodes(int numberOfEpisodes) {
        this.numberOfEpisodes = numberOfEpisodes;
    }

    public List<Integer> getCategories() {
        return categories;
    }

    public void setCategories(List<Integer> categories) {
        this.categories = categories;
    }

    public void addCategory(Integer i) {
        if (this.categories == null) {
            this.categories = new ArrayList<>();
        }
        this.categories.add(i);
    }

    public List<String> getGenreNames() {
        return genreNames;
    }

    public void setGenreNames(List<String> genreNames) {
        this.genreNames = genreNames;
    }

    @Override
    public String toString() {
        return "Content{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", originalTitle='" + originalTitle + '\'' +
                ", plot='" + (plot != null ? plot.substring(0, Math.min(plot.length(), 50)) + "..." : "N/A") + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", posterUrl='" + posterUrl + '\'' +
                ", runtimeMinutes=" + runtimeMinutes +
                ", year=" + year +
                ", rating=" + rating +
                ", isSeries=" + isSeries +
                ", numberOfSeasons=" + numberOfSeasons +
                ", numberOfEpisodes=" + numberOfEpisodes +
                ", genreNames=" + genreNames +
                '}';
    }
}