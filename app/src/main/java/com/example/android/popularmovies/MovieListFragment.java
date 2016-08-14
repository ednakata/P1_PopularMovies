package com.example.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A fragment to contain the grid of movies.
 */
public class MovieListFragment extends Fragment {

    private MovieGridViewAdapter mMovieAdapter;

    public MovieListFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);


    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_movie_grid, container, false);

        Movie[] movieArray = {};
        ArrayList<Movie> movieList = new ArrayList<>(Arrays.asList(movieArray));

        mMovieAdapter =
                new MovieGridViewAdapter(
                        getActivity(),
                        R.layout.grid_item_movie,
                        R.id.grid_item_movie_image_view,
                        movieList);

        final GridView movieGridView = (GridView) rootView.findViewById(R.id.movie_gridView);
        movieGridView.setAdapter(mMovieAdapter);

        movieGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            /**
             *
             * Clicking on an item will show the user the details about the selected movie
             *
             * @param adapterView
             * @param view
             * @param i
             * @param l
             */
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent detailIntent = new Intent(getActivity(), MovieDetailActivity.class);
                Movie currentMovie = (Movie) adapterView.getItemAtPosition(i);
                Bundle extras = new Bundle();
                extras.putString(getString(R.string.ex_backdrop_url), currentMovie.getBackdropUrl(getContext()).toString());
                extras.putString(getString(R.string.ex_description), currentMovie.getDescription());
                extras.putString(getString(R.string.ex_poster_url), currentMovie.getPosterUrl(getContext()).toString());
                extras.putString(getString(R.string.ex_rating), currentMovie.getRating());
                extras.putString(getString(R.string.ex_release_date), currentMovie.getReleaseDate());
                extras.putString(getString(R.string.ex_title), currentMovie.getTitle());
                detailIntent.putExtras(extras);
                startActivity(detailIntent);
            }
        });
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.movielistfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        if (id == R.id.action_change_sort) {
            Intent intent = new Intent(getContext(), SettingsActivity.class);
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(intent);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }

    /**
     * This will call a helper class to determine the connection state.
     *
     * @return the current connection state
     */
    private boolean isConnected() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    private void updateMovies() {
        /**
         * If there is a valid connection, retrieve the list of movies based on the
         * current sort order preference.
         */
        if (new ConnectionHelper().isConnected(getContext())) {
            new GetMovieListTask().execute(getSortOrder());
        } else {
            Toast.makeText(getActivity(), getString(R.string.connectivity_error_msg), Toast.LENGTH_SHORT).show();
        }
    }

    private String getSortOrder() {
        // Get the sort order from preferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return preferences.getString(getString(R.string.pref_sort_key),
                getString(R.string.pref_sort_popular));
    }

    final class MovieGridViewAdapter extends ArrayAdapter {
        private Context context;
        private final List<Movie> movies = new ArrayList<>();

        public MovieGridViewAdapter(Context context, int resource, int textViewResourceId, List<Movie> movies) {
            super(context, resource, textViewResourceId, movies);
        }

        @Override
        public Movie getItem(int i) {
            return (Movie) super.getItem(i);
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            Context context = getContext();
            ImageView movieImage = (ImageView) view;
            if (movieImage == null) {
                movieImage = new ImageView(context);
                movieImage.setAdjustViewBounds(true);
                movieImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
            String url = getItem(i).getPosterUrl(getContext()).toString();

            Picasso.with(context)
                    .load(url)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .tag(context)
                    .into(movieImage);

            return movieImage;
        }
    }

    private class GetMovieListTask extends AsyncTask<String, Void, Movie[]> {

        private final String LOG_TAG = GetMovieListTask.class.getSimpleName();

        @Override
        protected Movie[] doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String moviesJsonStr;
            Movie[] moviesArray = null;
            String[] posterUrlArray = null;

            try {
                // Construct the URL for the movie DB query
                String sortOrder = "";
                if (params != null && params.length > 0) {
                    sortOrder = params[0];
                }

                Uri.Builder builder = new Uri.Builder();
                builder.scheme(getString(R.string.themoviedb_scheme));
                builder.authority(getString(R.string.themoviedb_api_authority));
                builder.appendPath(getString(R.string.themoviedb_path_a));
                builder.appendPath(getString(R.string.themoviedb_path_b));
                builder.appendPath(sortOrder);
                builder.appendQueryParameter(
                        getString(R.string.themoviedb_api_parameter_name),
                        getString(R.string.themoviedb_api_key));

                URL url = new URL(builder.build().toString());
                // Create the request to the Movie DB, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                moviesJsonStr = buffer.toString();
                moviesArray = getMovieListDataFromJson(moviesJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error ", e);
                return moviesArray;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            return moviesArray;
        }

        @Override
        protected void onPostExecute(Movie[] movieArray) {
            if (movieArray != null) {
                mMovieAdapter.clear();
                for (Movie movie : movieArray) {
                    mMovieAdapter.add(movie);
                }
            }
        }
    }

    private Movie[] getMovieListDataFromJson(String movieJsonStr)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String MOVIE_RESULTS = getString(R.string.themoviedb_json_results);
        final String MOVIE_ID = getString(R.string.themoviedb_json_id);
        final String MOVIE_TITLE = getString(R.string.themoviedb_json_title);
        final String MOVIE_DESCRIPTION = getString(R.string.themoviedb_json_overview);
        final String MOVIE_POSTER_PATH = getString(R.string.themoviedb_json_poster_path);
        final String MOVIE_BACKDROP_PATH = getString(R.string.themoviedb_json_backdrop_path);
        final String MOVIE_RELEASE_DATE = getString(R.string.themoviedb_json_release_date);
        final String MOVIE_RATING = getString(R.string.themoviedb_json_vote_average);

        JSONObject moviesJson = new JSONObject(movieJsonStr);
        JSONArray moviesArray = moviesJson.getJSONArray(MOVIE_RESULTS);

        //Create movie objects out of each item in the array
        Movie[] movieObjs = new Movie[moviesArray.length()];
        for (int i = 0; i < moviesArray.length(); i++) {

            JSONObject movieJson = moviesArray.getJSONObject(i);

            Movie movieObject = new Movie(
                    movieJson.getString(MOVIE_ID),
                    movieJson.getString(MOVIE_TITLE),
                    movieJson.getString(MOVIE_DESCRIPTION),
                    movieJson.getString(MOVIE_POSTER_PATH),
                    movieJson.getString(MOVIE_BACKDROP_PATH),
                    movieJson.getString(MOVIE_RATING),
                    movieJson.getString(MOVIE_RELEASE_DATE));


            movieObjs[i] = movieObject;
        }

        return movieObjs;

    }

}
