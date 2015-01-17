package com.botornot.demo.botornot;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


public class BotOrNotActivity extends ActionBarActivity {

    public static final String MODE_KEY = "MODE";
    public static final int MODE_REGULAR = 10;
    public static final int MODE_FACEBOOK = 20;
    private static final String LOG_TAG = "QUYEN";
    private static final int DEFAULT_RATING = 25;
    private static int NUM_FRIENDS = 0;
    private static int FRIEND_COUNT = 0;
    private static String FRIEND_NAME;

    private ImageView mImageViewer;
    private SeekBar mRatingBar;
    private Button mNotButton;
    private Button mBotButton;
    private Button mBackButton;
    private Button mNextButton;
    private Button mSkipButton;
    private TextView mBotnessLabel;
    private int mCurrentPosition = 0;
    private int mMode = MODE_REGULAR;
    private boolean continueDownload = true;
    private Toast toast;

    private RatingsManager mRatingsManager;
    private ArrayList<ImageViewRatingHelper> mImageList;
    private HashMap<Long, String> nameLookupMap = new HashMap<Long, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bot_or_not);
        initRatingsManager();
        initViews();
        initButtons();
        layoutDefaultScreen();
        handleModeSetting(savedInstanceState);

        if (getMode() == MODE_FACEBOOK) {
            initImageViewer();
            nameLookupMap = LauncherActivity.nameLookupMap;
            new Thread(new Runnable() {

                @Override
                public void run() {
                    downloadImageList(LauncherActivity.friendList);
                }
            }).start();
        } else {
            continueDownload = false;
            setImage(getCurrentPosition());
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bot_or_not, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initRatingsManager() {
        mRatingsManager = new RatingsManager(this);
        mImageList = new ArrayList<ImageViewRatingHelper>();
        generateImageList();
    }

    private void initImageViewer() {
        ImageViewRatingHelper ivrh = getImageList().get(getCurrentPosition());
        getImageViewer().setImageDrawable(ivrh.getImageView().getDrawable());
    }

    private void initViews() {
        mRatingBar = (SeekBar) findViewById(R.id.ratingBar);
        getRatingBar().setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                doSetBotnessText(progress);
            }
        });

        mBotnessLabel = (TextView) findViewById(R.id.botness_label);
    }

    private void handleModeSetting(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mMode = savedInstanceState.getInt(MODE_KEY); // Saving state not implemented yet
        } else {
            if (getIntent().getExtras() != null) {
                mMode = getIntent().getExtras().getInt(MODE_KEY);
            }
        }
    }

    private void doSetBotnessText(int progress) {
        String ratingText = generateRatingText(generateRatingFromProgress(progress));
        String botnessText = "";
        if (progress < 10) {
            botnessText = getString(botnessStringIds[0]);
        } else if (progress < 40) {
            botnessText = getString(botnessStringIds[1]);
        } else if (progress < 60) {
            botnessText = getString(botnessStringIds[2]);
        } else if (progress < 80) {
            botnessText = getString(botnessStringIds[3]);
        } else {
            botnessText = getString(botnessStringIds[4]);
        }
        setBotnessText(botnessText + "\n" + ratingText);
    }

    private double generateRatingFromProgress(int progress) {
        return (new Double(progress))/20.0 + 5.0;
    }

    private String generateRatingText(double rating) {
        return new DecimalFormat("0.0").format(rating);
    }

    private void initButtons() {
        mNotButton = (Button) findViewById(R.id.notButton);
        getNotButton().setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveNotRating();
                nextImage();
            }
        });

        mBotButton = (Button) findViewById(R.id.botButton);
        getBotButton().setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                layoutSetBotnessScreen();
            }
        });

        mBackButton = (Button) findViewById(R.id.backButton);
        getBackButton().setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                layoutDefaultScreen();
            }
        });

        mNextButton = (Button) findViewById(R.id.nextButton);
        getNextButton().setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveRating();
                nextImage();
            }
        });

        mSkipButton = (Button) findViewById(R.id.skipButton);
        getSkipButton().setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (continueDownload == true) { // Do once to stop download
                    continueDownload = false;
                    getSkipButton().setVisibility(View.INVISIBLE);
                    return;
                }
                nextImage();
                getRatingsManager().getAllRatings();
            }
        });
    }

    private void layoutDefaultScreen() {
        getNotButton().setVisibility(View.VISIBLE);
        getBotButton().setVisibility(View.VISIBLE);
        getSkipButton().setVisibility(View.VISIBLE);
        getBotnessLabel().setVisibility(View.VISIBLE);

        getBackButton().setVisibility(View.INVISIBLE);
        getNextButton().setVisibility(View.INVISIBLE);
        getRatingBar().setVisibility(View.INVISIBLE);

        populatePreviousRatings();
    }

    private void layoutSetBotnessScreen() {
        getRatingBar().setProgress(DEFAULT_RATING);
        doSetBotnessText(DEFAULT_RATING);

        getBackButton().setVisibility(View.VISIBLE);
        getNextButton().setVisibility(View.VISIBLE);
        getBotnessLabel().setVisibility(View.VISIBLE);
        getRatingBar().setVisibility(View.VISIBLE);

        getNotButton().setVisibility(View.INVISIBLE);
        getBotButton().setVisibility(View.INVISIBLE);
        getSkipButton().setVisibility(View.INVISIBLE);
    }

    private void populatePreviousRatings() {
        String text = "";
        double rating = getRatingsManager().getRating(
                getImageList().get(getCurrentPosition()).getRating().getImageId());
        if (rating > 0.0) {
            text = "Last Rating: "+generateRatingText(rating);
        }
        setBotnessText(text);
    }

    private void saveRating() {
        Rating ratingReference = getCurrentIvrHelper().getRating();
        Rating rating = new Rating(ratingReference.getImageId(), ratingReference.getUserId(),
                generateRatingFromProgress(getRatingBar().getProgress()));
        getRatingsManager().saveRating(rating);
    }

    private void saveNotRating() {
        Rating ratingReference = getCurrentIvrHelper().getRating();
        Rating rating = new Rating(ratingReference.getImageId(), ratingReference.getUserId(), 0.0);
        getRatingsManager().saveRating(rating);
    }

    private RatingsManager getRatingsManager() {
        return mRatingsManager;
    }

    private ImageView getImageViewer() {
        if (mImageViewer==null) {
            mImageViewer = (ImageView) findViewById(R.id.imageViewer);
        }
        return mImageViewer;
    }

    private SeekBar getRatingBar() {
        return mRatingBar;
    }

    private Button getNotButton() {
        return mNotButton;
    }

    private Button getBotButton() {
        return mBotButton;
    }

    private Button getBackButton() {
        return mBackButton;
    }

    private Button getNextButton() {
        return mNextButton;
    }

    private Button getSkipButton() {
        return  mSkipButton;
    }

    private TextView getBotnessLabel() {
        return mBotnessLabel;
    }

    private void setBotnessText(String text) {
        getBotnessLabel().setText(text);
    }

    private void nextImage() {
        //Log.d(LOG_TAG, "mCurrentPosition at nextImage() START: "+mCurrentPosition);
        if (getCurrentPosition() == (getNumImages()-1)) {
            setCurrentPosition(0);
        } else {
            setCurrentPosition(getCurrentPosition() + 1);
        }
        setImage(getCurrentPosition());
        layoutDefaultScreen();
        //Log.d(LOG_TAG, "mCurrentPosition at nextImage() END: "+mCurrentPosition);
    }

    @SuppressWarnings("unused")
    private void previousImage() {
        //Log.d(LOG_TAG, "mCurrentPosition at previousImage() START: "+mCurrentPosition);
        if (getCurrentPosition() == 0) {
            setCurrentPosition(getCurrentPosition() - 1 + getNumImages());
        } else {
            setCurrentPosition(getCurrentPosition() - 1);
        }
        setImage(getCurrentPosition());
        //Log.d(LOG_TAG, "mCurrentPosition at previousImage() END: "+mCurrentPosition);
    }

    private void setImage(int index) {
        ImageViewRatingHelper ivrh = getImageList().get(index);
        getImageViewer().setImageDrawable(ivrh.getImageView().getDrawable());
        displayText(ivrh.getUserName());
    }

    private void displayText(String text) {
        if (toast != null) {
            toast.cancel();
            //Log.d(LOG_TAG, "Cancelled toast with duration: "+toast.getDuration());
        }
        toast = Toast.makeText(BotOrNotActivity.this, text, Toast.LENGTH_SHORT);
        toast.show();
    }

    private int getCurrentPosition() {
        return mCurrentPosition;
    }

    private void setCurrentPosition(int currentPosition) {
        mCurrentPosition = currentPosition;
    }

    private int getNumImages() {
        return getImageList().size();
    }

    private int getMode() {
        return mMode;
    }

    private ImageViewRatingHelper getCurrentIvrHelper() {
        return getImageList().get(getCurrentPosition());
    }

    private void generateImageList() {
        ImageView imageView;
        ImageViewRatingHelper ivrHelper;
        for(int i=0; i<imageNumIds.length; i++) {
            imageView = new ImageView(getImageViewer().getContext());
            imageView.setImageResource(imageIds[i]);
            ivrHelper = new ImageViewRatingHelper(imageView, imageNumIds[i], userIds[i], userNames[i]);
            getImageList().add(ivrHelper);
        }
    }

    private void downloadImageList(ArrayList<Long> ids) {

        getImageList().clear();
        ImageView imageView;
        ImageViewRatingHelper ivrHelper;
        int i=0;
        NUM_FRIENDS = ids.size();
        continueDownload = true;
        Collections.shuffle(ids);
        for(Long id:ids) {
            if (continueDownload == false) {
                break;
            }
            FRIEND_COUNT = i+1;
            FRIEND_NAME = nameLookupMap.get(id);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getBotnessLabel().setText("Downloading " + FRIEND_COUNT + " of " +
                            NUM_FRIENDS + " images.\nPress Next to stop.\n" + FRIEND_NAME);
                }
            });
            imageView = new ImageView(getImageViewer().getContext());

            imageView.setImageBitmap(doDownload(id));
            ivrHelper = new ImageViewRatingHelper(imageView, id, id, nameLookupMap.get(id));
            getImageList().add(ivrHelper);
            i++;
        }
        continueDownload = false;
        setCurrentPosition(0); // TODO NEED CHANGE
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getBotnessLabel().setVisibility(View.INVISIBLE);
                Toast.makeText(BotOrNotActivity.this, "Download of "+FRIEND_COUNT+
                        " images done! Press Next to start.", Toast.LENGTH_SHORT).show();
                getSkipButton().setVisibility(View.VISIBLE);
            }
        });
    }

    private Bitmap doDownload(Long id) {
        URL url = null;
        Bitmap bitmap = null;
        try {
            url = new URL("https://graph.facebook.com/"+id+"/picture?type=large");
            bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    /**
     * All images retrieved from Wikimedia Commons (http://commons.wikimedia.org/wiki/Main_Page)
     * All copyrights and trademarks are property of their respective holders
     */
    private Integer[] imageIds = {
            R.drawable.bot1, R.drawable.bot2, R.drawable.bot3,
            R.drawable.bot4, R.drawable.bot5, R.drawable.bot6,
    };

    private Long[] imageNumIds = {
            1L, 2L, 3L,
            4L, 5L, 6L,
    };

    private Long[] userIds = {
            1L, 2L, 3L,
            4L, 5L, 6L,
    };

    private String[] userNames = {
            "Albert", "Duck", "Android",
            "Domo", "Duke", "Hot",
    };

    private ArrayList<ImageViewRatingHelper> getImageList() {
        return mImageList;
    }

    private Integer[] botnessStringIds = {
            R.string.botness_label_1,
            R.string.botness_label_2,
            R.string.botness_label_3,
            R.string.botness_label_4,
            R.string.botness_label_5,
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                Log.d(LOG_TAG, "RIGHT KEY pressed");
                //nextImage();
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                Log.d(LOG_TAG, "LEFT KEY pressed");
                //previousImage();
                return true;
        };
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected  void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mMode == MODE_FACEBOOK) {
            getBotnessLabel().setVisibility(View.VISIBLE);
            getNotButton().setVisibility(View.INVISIBLE);
            getBotButton().setVisibility(View.INVISIBLE);
        }
    }
}












