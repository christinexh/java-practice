package edu.csula.dream.exercise.singleplayer.boxing;

import android.graphics.PorterDuff;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andreabaccega.widget.FormEditText;

import edu.csula.dream.app.R;
import edu.csula.dream.exercise.BleDataGuiUpdater;
import edu.csula.dream.exercise.BleGameFragment;
import edu.csula.dream.exercise.data.MiscSessionMetrics;
import edu.csula.dream.exercise.data.collectors.EmgCollector;
import edu.csula.dream.exercise.data.collectors.HeartRateCollector;
import edu.csula.dream.exercise.multiplayer.live.boxing.models.BoxingProgress;
import edu.csula.dream.exercise.multiplayer.live.models.GameType;
import edu.csula.dream.exercise.multiplayer.replay.boxing.BoxingProgressTracker;
import edu.csula.dream.models.restApi.Acceleration;
import edu.csula.dream.models.restApi.EMG;
import edu.csula.dream.models.restApi.HeartRate;

import android.media.MediaPlayer;

import java.util.HashMap;
import java.util.Map;

public class SPBoxingPlayFragment extends BleGameFragment implements BleDataGuiUpdater {
    private static final String LOG_TAG = SPBoxingPlayFragment.class.getSimpleName();

    //private static final String ARG_OTHER_PLAYER_SESSION = "otherPlayerSession";
    private static final String ARG_UP_TARGET_HEART_RATE = "uppertargetHeartRate";
    private static final String ARG_LOW_TARGET_HEART_RATE = "lowertargetHeartRate";
    private static final String ARG_UP_TARGET_EMG_ENVELOPE_LEVEL = "uppertargetEmgEnvelopeLevel";
    private static final String ARG_LOW_TARGET_EMG_ENVELOPE_LEVEL = "lowertargetEmgEnvelopeLevel";
    private static final String ARG_LEFT_EMG_MAC_ADDRESS = "leftemgmacaddress";
    private static final String ARG_RIGHT_EMG_MAC_ADDRESS = "rightemgmacaddress";

    private static final String ARG_UP_TARGET_ACCEL = "uppertargetAccel";
    private static final String ARG_LOW_TARGET_ACCEL = "lowertargetAccel";

    // just some flags to determine if we should show some stats or not. not really useful yet
    private boolean showHr = true;
    private boolean showAccel1 = true;
    private boolean showAccel2 = true;
    private boolean showEmg = true;

    private LinearLayout mGameProgressLayout; // displays the entire progress of the game, i.e. the progress bars, the players, ect.
    private RelativeLayout mAnimGraphics;
    private Button mQuit; // quits the game;
    private Button mStart; // start game
    private LinearLayout mSession;

    // Live player UI widgets
    private TextView mLiveUser; // displays the username of the live, current user
    private TextView mLiveHr; // displays the current heart rate of the live, current user
    private TextView mLiveAccel1; // displays the current accel 1 of the live, current user
    private TextView mLiveAccel2; // displays the current accel 2 of the live, current user
    private TextView mLiveEmg1; // displays the current emg of the live, current user
    private TextView mLiveEmg2;
    private TextView mPunchLeft;
    private TextView mPunchRight;
    private ProgressBar mLiveProgressBar; // holds the progress bar of the live, current user

    // edit box for the name of session
    private FormEditText mSessionNameEditText;

    private TextView mGameResult; // displays the game result, i.e. who won or lost once game is finished

    private TextView mEmgZone; // displays the emg target zone, e.g. 1000-2500
    private TextView mHrZone; // displays the heart rate target zone
    private TextView mAccelZone; // displays the accel target zone

    private BoxingProgressTracker livePlayerProgress; // keeps track of the current, live player progress
    private BoxingProgress boxingProgress; // boxing progress bar
//    private BoxingProgressTracker replayPlayerProgress; // keeps track of the replayed player progress

    ImageView bplayer;
    AnimationDrawable bplayerAnimation;
    ImageView star;
    private int punchCountLeft = 0;
    private int punchCountRight = 0;
    private boolean _EnteredEMGRange = false;   //track whether EMG signal entered range when not previously

    private int upperEmgTarget;
    private int lowerEmgTarget;
    private int upperHrTarget;
    private int lowerHrTarget;
    private int upperAccelTarget;
    private int lowerAccelTarget;

    private Map<String, TextView> emgToPunchTextViewMap = new HashMap<>();

    public static SPBoxingPlayFragment newInstance(int uppertargetHeartRate,
                                                   int lowertargetHeartRate,
                                                   int uppertargetEMGEnvelope,
                                                   int lowertargetEMGEnvelope,
                                                   String leftEmgMacAddress,
                                                   String rightEmgMacAddress) {
        // 'Bundle' used to share variables with other activities
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_UP_TARGET_HEART_RATE, uppertargetHeartRate);
        bundle.putInt(ARG_LOW_TARGET_HEART_RATE, lowertargetHeartRate);
        bundle.putInt(ARG_UP_TARGET_EMG_ENVELOPE_LEVEL, uppertargetEMGEnvelope);
        bundle.putInt(ARG_LOW_TARGET_EMG_ENVELOPE_LEVEL, lowertargetEMGEnvelope);
        bundle.putString(ARG_LEFT_EMG_MAC_ADDRESS, leftEmgMacAddress);
        bundle.putString(ARG_RIGHT_EMG_MAC_ADDRESS, rightEmgMacAddress);

        SPBoxingPlayFragment spBoxingPlayFragment = new SPBoxingPlayFragment();
        spBoxingPlayFragment.setArguments(bundle);
        return spBoxingPlayFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_spboxing_play, null);

        // blue player animation
        bplayer = (ImageView) v.findViewById(R.id.BPlayer);
        bplayerAnimation = (AnimationDrawable) bplayer.getDrawable();

        mGameProgressLayout = (LinearLayout) v.findViewById(R.id.game_progress_layout);
        mAnimGraphics = (RelativeLayout) v.findViewById(R.id.animgraphics);
        mSession = (LinearLayout) v.findViewById(R.id.session);

        mEmgZone = (TextView) v.findViewById(R.id.emg_target_zone);
        mHrZone = (TextView) v.findViewById(R.id.hr_target_zone);
        mAccelZone = (TextView) v.findViewById(R.id.accel_target_zone);

        mQuit = (Button) v.findViewById(R.id.quit_replay_game);
        mQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            }
        });

        mStart = (Button) v.findViewById(R.id.start_replay_game);
        mStart.setVisibility(View.VISIBLE);
        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createSession(mSessionNameEditText, GameType.BOXING);
            }
        });

        mGameResult = (TextView) v.findViewById(R.id.game_result_msg);

        mSessionNameEditText = (FormEditText) v.findViewById(R.id.session_name_value);

        // initialize live user ui widgets
        mLiveUser = (TextView) v.findViewById(R.id.live_name_value);
        mLiveHr = (TextView) v.findViewById(R.id.live_hr_value);
        mLiveEmg1 = (TextView) v.findViewById(R.id.live_emg1_value);
        mLiveEmg2 = (TextView) v.findViewById(R.id.live_emg2_value);

        mPunchLeft = (TextView) v.findViewById(R.id.punch_count_value_left);
        mPunchRight = (TextView) v.findViewById(R.id.punch_count_value_right);

        mLiveProgressBar = (ProgressBar) v.findViewById(R.id.live_progress_bar);

        Animation verticalAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.progress_bar_anim);

//        mReplayProgressBar.startAnimation(verticalAnim);
        mLiveProgressBar.startAnimation(verticalAnim);

        // show or hide metrics depending on what should be measured
        toggleHrVis();
        toggleAccelVis();
        toggleEmgVis();

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle args = getArguments();
        if (args != null && userIsOnline()) {
            String leftEmgMacAddress = args.getString(ARG_LEFT_EMG_MAC_ADDRESS);
            String rightEmgMacAddress = args.getString(ARG_RIGHT_EMG_MAC_ADDRESS);

            emgToPunchTextViewMap.put(leftEmgMacAddress, mPunchLeft);
            emgToPunchTextViewMap.put(rightEmgMacAddress, mPunchRight);

            upperEmgTarget = args.getInt(ARG_UP_TARGET_EMG_ENVELOPE_LEVEL);
            lowerEmgTarget = args.getInt(ARG_LOW_TARGET_EMG_ENVELOPE_LEVEL);
            lowerHrTarget = args.getInt(ARG_LOW_TARGET_HEART_RATE);
            upperHrTarget = args.getInt(ARG_UP_TARGET_HEART_RATE);

            mEmgZone.setText("EMG: " + lowerEmgTarget + " - " + upperEmgTarget);
            mHrZone.setText(" HR: " + lowerHrTarget + " - " + upperHrTarget);

            livePlayerProgress = new BoxingProgressTracker(mIdentityManager.getUserName(),
                    lowerEmgTarget, upperEmgTarget,
                    lowerHrTarget, upperHrTarget,
                    lowerAccelTarget, upperAccelTarget);

            // set up the progress bar for the live player based on the prior set up progress
            updateProgressBar(livePlayerProgress, mLiveProgressBar);
        }
    }

    /**
     * Session is created, so start the game now
     */
    @Override
    public void onSessionCreated() {
        miscSessionMetrics = new MiscSessionMetrics.MiscSessionMetricsBuilder()
                .setLowerTargetHr(livePlayerProgress.getLowerHrTarget())
                .setUpperTargetHr(livePlayerProgress.getUpperHrTarget())
                .setLowerTargetEmg(livePlayerProgress.getLowerEmgTarget())
                .setUpperTargetEmg(livePlayerProgress.getUpperEmgTarget())
                .setLowerTargetAccel(livePlayerProgress.getLowerAccelTarget())
                .setUpperTargetAccel(livePlayerProgress.getUpperAccelTarget())
                .createMiscSessionMetrics();
        startGame();
    }

    /**
     * Starts the game. Creates data consumers for each of the replayed players data producers, and
     * pairs those consumers with those producers. Starts each of these consumers and producers.
     */
    private void startGame() {
        final long startTime = System.currentTimeMillis();

        startCollecting();

        MediaPlayer mp1 = MediaPlayer.create(getActivity(), R.raw.boxing_bell2);
        mp1.start();
        MediaPlayer mp2 = MediaPlayer.create(getActivity(), R.raw.crowd);
        mp2.start();

        // comment or uncomment to disable or enable simulation of sensor data being received, respectively.
//        mockTest();
        startGuiUpdates();
    }

    /**
     * When there's a winner, show a message saying who won / lost
     *
     * @param trackedWinner the tracker of the player who won
     */
    public void endGame(BoxingProgressTracker trackedWinner) {
        stop();
        final StringBuilder result = new StringBuilder("Game over. ");
        if (userIsOnline()) {
            bplayerAnimation.stop();
            if (trackedWinner == livePlayerProgress) {
                result.append("You win!");
            } else {
                result.append("You lose!");
            }
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mGameResult.setVisibility(View.VISIBLE);
                        mGameResult.setText(result);
                    }
                });
            }
        }
    }

    /**
     * Stops collecting, consuming, and producing data.
     */
    private void stop() {
        stopCollecting();
    }

    @Override
    public void onStop() {
        super.onStop();
        stop();
    }

    /**
     * Updates the progress bar according to the tracker. For example, if the tracker says the progress
     * bar should be red, then this method will make the progress bar red.
     *
     * @param boxingProgressTracker the tracker of the progress of a user
     * @param progressBar           the progress bar that the tracker updates
     */
    protected static void updateProgressBar(BoxingProgressTracker boxingProgressTracker, ProgressBar progressBar) {
        progressBar.getProgressDrawable().setColorFilter(boxingProgressTracker.getColor(), PorterDuff.Mode.SRC_IN);
        progressBar.setMax(boxingProgressTracker.getMaxProgress());
        progressBar.setProgress(boxingProgressTracker.getCurrentProgress());
    }

    /**
     * This method gets called whenever a new accel value arrives for the current, live user.
     * Puts the value into the correct accel collector, updates GUI, and determines if there's a winner.
     *
     * @param macAddress the mac address of the accelerometer
     * @param strValue   the string value of acceleration
     */
    @Override
    protected void handleLiveAccel(final String macAddress,
                                   final String strValue) {
        /*AccelerationCollector accelerationCollector = (AccelerationCollector) dataCollectorMap.get(macAddress);
        if (accelerationCollector != null) {
            Acceleration accel = accelerationCollector.collect(strValue, null);
            if (accel != null) {
                livePlayerProgress.addAccel(accel.getMagnitude());
                updateAccelGui(accel);
                if (livePlayerProgress.hasWon()) {
                    endGame(livePlayerProgress);
                }
            }
        }*/
    }

    /**
     * This method gets called whenever new EMG values arrive for the current, live user.
     * Puts the value into the correct emg collector, updates GUI, and determines if there's a winner.
     *
     * @param macAddress the mac address of the Dynofit Flexdot
     * @param intValues  the int values of the emg
     */
    @Override
    protected void handleLiveEmg(final String macAddress,
                                 final int[] intValues) {
        EmgCollector emgCollector = (EmgCollector) dataCollectorMap.get(macAddress);
        if (emgCollector != null) {
            for (int emgValue : intValues) {
                EMG emg = emgCollector.collect(null, emgValue);
                if (emg != null) {
                    livePlayerProgress.addEmg(emg.getEnvelopeValue());
                    updateEmgGui(emg, macAddress);
                    //updateEmg2Gui(emg2);
                    if (livePlayerProgress.hasWon()) {
                        endGame(livePlayerProgress);
                    }
                }
            }
        }
    }

    /**
     * This method gets called whenever a new heart rate value arrives for the current, live user.
     * Puts the value into the correct heart rate collector, updates GUI, and determines if there's a winner.
     *
     * @param macAddress the mac address of the heart rate monitor
     * @param intValue   the int value of the heart rate
     */
    @Override
    protected void handleLiveHr(final String macAddress,
                                final int intValue) {
        HeartRateCollector heartRateCollector = (HeartRateCollector) dataCollectorMap.get(macAddress);
        if (heartRateCollector != null) {
            HeartRate heartRate = heartRateCollector.collect(null, intValue);
            if (heartRate != null) {
                livePlayerProgress.addHr(heartRate.getValue());
                updateHrGui(heartRate);
                if (livePlayerProgress.hasWon()) {
                    endGame(livePlayerProgress);
                }
            }
        }
    }

    /**
     * This method gets called in order for this fragment to initialize collectors for
     * data it's interested in.
     */
    @Override
    protected void initCollectors() {

    }

    @Override
    public void updateAccelGui(final Acceleration accel) {
        /*if (accel != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLiveAccel1.setText(accel.getMagnitude() + " G");
//                    updateProgressBar(livePlayerProgress, mLiveProgressBar);
                }
            });
        }*/
    }

    int count = 1;
    //@Override
    public void updateEmgGui(final EMG emg, final String macAddress) {
        Bundle args = getArguments();
        if ((args != null && userIsOnline()) && emg != null) {
            final int currProg = livePlayerProgress.getCurrentProgress();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if(count <= 60){
                        count++;
                        emg.getEnvelopeValue();
                        mLiveEmg1.setText(emg.getEnvelopeValue() + "mV");

                        if(count == 60){
                            mLiveEmg2.setText(emg.getEnvelopeValue() + "mV");
                            if(emg.getEnvelopeValue() >= lowerEmgTarget){
                                //if(emg.getEnvelopeValue() >= lowerEmgTarget && emg.getEnvelopeValue() <= upperEmgTarget){

                                // starts animation when within EMG range
                                // animation does not loop, only plays once
                                bplayerAnimation.start();

                                //register entry of range if not previously done so, increment punch count too
                                if(!_EnteredEMGRange){

                                    // animation restarts when entering EMG range since animation only plays once
                                    bplayerAnimation.setVisible(true,true);
                                    _EnteredEMGRange = true;

                                    MediaPlayer mp3= MediaPlayer.create(getActivity(),R.raw.punches);
                                    mp3.setVolume(0.5f, 0.5f);
                                    mp3.start();

                                    // if the emg value came from the left emg
                                    if (emgToPunchTextViewMap.get(macAddress) == mPunchLeft) {
                                        punchCountLeft++;
                                        mPunchLeft.setText("" + punchCountLeft);
                                    }

                                    // if the emg value came from the right emg
                                    if (emgToPunchTextViewMap.get(macAddress) == mPunchRight) {
                                        punchCountRight++;
                                        mPunchRight.setText("" + punchCountRight);
                                    }
                                }

                                // resets punch count to zero when going to next level
                                // currProg is current progress on the progress bar
                                if(currProg == 0 && punchCountLeft == 25){
                                    punchCountLeft = 0;
                                } else if(currProg == 0 && punchCountLeft == 40){
                                    punchCountLeft = 0;
                                }

                            } else {
                                _EnteredEMGRange = false;

                            }
                            count = 0;
                        }
                    }
                    updateProgressBar(livePlayerProgress, mLiveProgressBar);

                }
            });
        }
    }

    @Override
    public void updateHrGui(final HeartRate heartRate) {
        if (heartRate != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (heartRate.getValue() == 0) {
                        mLiveHr.setText("");
                    } else {
                        mLiveHr.setText(heartRate.getValue() + " bpm");
                    }
//                    updateProgressBar(livePlayerProgress, mLiveProgressBar);
                }
            });
        }
    }

    @Override
    public void updatePerfGui(final double performance) {
        // no-op
    }

    @Override
    public void startGuiUpdates() {
//        mLiveUser.setText(livePlayerProgress.getUsername());
//        mReplayUser.setText(replayPlayerProgress.getUsername());
        mStart.setVisibility(View.GONE);
        mGameProgressLayout.setVisibility(View.VISIBLE);
        mAnimGraphics.setVisibility(View.VISIBLE);
        mSession.setVisibility(View.GONE);
    }

    private void toggleHrVis() {
        if (!showHr) {
//            mReplayHr.setVisibility(View.GONE);
            mLiveHr.setVisibility(View.GONE);
        }
    }

    private void toggleAccelVis() {
        if (!showAccel1) {
//            mReplayAccel1.setVisibility(View.GONE);
            mLiveAccel1.setVisibility(View.GONE);
        }
        if (!showAccel2) {
//            mReplayAccel2.setVisibility(View.GONE);
            mLiveAccel2.setVisibility(View.GONE);
        }
    }

    private void toggleEmgVis() {
        if (!showEmg) {
//            mReplayEmg.setVisibility(View.GONE);
            mLiveEmg1.setVisibility(View.GONE);
//            mLiveEmg2.setVisibility(View.GONE);
        }
    }
}