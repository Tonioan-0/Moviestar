package com.esa.moviestar.model;
import java.util.ArrayList;
import java.util.List;

/**
 * Content class representing a film or TV series based on the SQLite schema.
 */
public class Content {
    private int id;
    private String title;
    private String plot;
    private String imageUrl;
    private String videoUrl;
    private double duration;
    private int year;
    private double rating;
    private int clicks;
    private String country;
    private String releaseDate;
    private boolean isSeries;
    private boolean isSeasonDivided;
    private int seasonCount;
    private int episodeCount;
    private List<Integer> categories ;

    public Content(){
        categories= new ArrayList<>();
    }


    public boolean isSeries() {
        return this.isSeries;
    }
    public void Series(boolean isSeries) {
        this.isSeries = isSeries;
    }

    public boolean isSeasonDivided() {
        return this.isSeasonDivided;
    }
    public void seasonDivided(boolean isSeries) {
        this.isSeasonDivided = isSeries;
    }

    // Getters and setters
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

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
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

    public int getClicks() {
        return clicks;
    }

    public void setClicks(int clicks) {
        this.clicks = clicks;
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


    public int getEpisodeCount() {
        return episodeCount;
    }

    public void setEpisodeCount(int episodeCount) {
        this.episodeCount = episodeCount;
    }

    public int getSeasonCount() {
        return seasonCount;
    }

    public void setSeasonCount(int seasonCount) {
        this.seasonCount = seasonCount;
    }


    public List<Integer> getCategories() {
        return categories;
    }

    public void setCategories(List<Integer> categories) {
        this.categories = categories;
    }

    public  void  addCategory(Integer i){
        if(categories==null)
            categories= new ArrayList<>();
        this.categories.add(i);
    }

    @Override
    public String toString() {
        return "Content{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", plot='" + plot + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                ", duration=" + duration +
                ", year=" + year +
                ", rating=" + rating +
                ", clicks=" + clicks +
                ", country='" + country + '\'' +
                ", releaseDate='" + releaseDate + '\'' +
                ", isSeries=" + isSeries +
                ", isSeasonDivided=" + isSeasonDivided +
                ", seasonCount=" + seasonCount +
                ", episodeCount=" + episodeCount +
                ", categories=" + categories +
                '}';
    }
}