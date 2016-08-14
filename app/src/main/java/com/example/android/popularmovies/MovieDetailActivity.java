package com.example.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

/**
 * Shows the details about a selected movie
 */
public class MovieDetailActivity extends ActionBarActivity {

    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MovieDetailFragment())
                    .commit();
        }
    }

    public static class MovieDetailFragment extends Fragment {

        public MovieDetailFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

            Intent detailIntent = getActivity().getIntent();
            if (detailIntent != null && detailIntent.getExtras() != null) {

                /**The backdrop image is the most likely item that will not be
                 * accessible without a connection, but it is possible that it will
                 * already be in cache.
                 */
                if (!(new ConnectionHelper().isConnected(getContext()))) {
                    Toast.makeText(getContext(), getString(R.string.connectivity_error_msg), Toast.LENGTH_SHORT).show();
                }

                /**
                 * Regardless of the connection state, show the details of the movie.
                 */
                Bundle extras = detailIntent.getExtras();

                ImageView backdropImageView = (ImageView) rootView.findViewById(R.id.backdrop_image_view);
                retrieveImage(extras.getString("EXTRA_BACKDROP_URL"), backdropImageView, Picasso.Priority.HIGH);

                ImageView posterImageView = (ImageView) rootView.findViewById(R.id.poster_image_view);
                retrieveImage(extras.getString("EXTRA_POSTER_URL"), posterImageView, Picasso.Priority.LOW);

                TextView titleTextView = (TextView) rootView.findViewById(R.id.movie_title_textview);
                titleTextView.setText(extras.getString("EXTRA_TITLE"));

                TextView releaseDateTextView = (TextView) rootView.findViewById(R.id.release_date_textview);
                releaseDateTextView.setText(extras.getString("EXTRA_RELEASE_DATE"));

                TextView ratingTextView = (TextView) rootView.findViewById(R.id.rating_textview);
                ratingTextView.setText(extras.getString("EXTRA_RATING") + getString(R.string.rating_appendage));

                TextView descriptionTextView = (TextView) rootView.findViewById(R.id.description_textview);
                descriptionTextView.setText(extras.getString("EXTRA_DESCRIPTION"));

            }
            return rootView;
        }

        private ImageView retrieveImage(String path, ImageView view, Picasso.Priority priority) {
            Context context = getContext();

            if (view == null) {
                view = new ImageView(context);
                view.setAdjustViewBounds(true);
                view.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }

            Picasso.with(context)
                    .load(path)
                    .priority(priority)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .tag(context)
                    .into(view);

            return view;
        }
    }

}
