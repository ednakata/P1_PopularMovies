package com.example.android.popularmovies;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;


public class Movie {
    private final String LOG_TAG = "Movie.class";
    public String movieID;
    public String title;
    public String description;
    public String posterPath;
    public String backdropPath;
    public String rating;
    public String releaseDate;

    public Movie(String movieID, String title, String description, String posterPath, String backdropPath, String rating, String releaseDate) {
        this.movieID = movieID;
        this.title = title;
        this.description = description;
        this.posterPath = posterPath;
        this.backdropPath = backdropPath;
        this.rating = rating;
        this.releaseDate = releaseDate;
    }

    public String getMovieID() {
        return movieID;
    }

    public void setMovieID(String movieID) {
        this.movieID = movieID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPosterPath() {
        return posterPath.replace("/", "");
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getBackdropPath() {
        return backdropPath.replace("/", "");
    }

    public void setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public URL getPosterUrl(Context c) {
        try {
            Uri.Builder builder = new Uri.Builder();

            builder.scheme(c.getString(R.string.themoviedb_scheme));
            builder.authority(c.getString(R.string.themoviedb_image_authority));
            builder.appendPath(c.getString(R.string.themoviedb_image_path_a));
            builder.appendPath(c.getString(R.string.themoviedb_image_path_b));
            builder.appendPath(c.getString(R.string.themoviedb_image_path_poster_size));
            builder.appendPath(getPosterPath());
            return new URL(builder.build().toString());

        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error ", e);
            return null;
        }
    }

    public URL getBackdropUrl(Context c) {
        try {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme(c.getString(R.string.themoviedb_scheme));
            builder.authority(c.getString(R.string.themoviedb_image_authority));
            builder.appendPath(c.getString(R.string.themoviedb_image_path_a));
            builder.appendPath(c.getString(R.string.themoviedb_image_path_b));
            builder.appendPath(c.getString(R.string.themoviedb_image_path_backdrop_size));
            builder.appendPath(getBackdropPath());
            return new URL(builder.build().toString());
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error ", e);
            return null;
        }
    }
}
