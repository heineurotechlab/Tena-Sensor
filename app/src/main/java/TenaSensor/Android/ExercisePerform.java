package TenaSensor.Android;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.CountDownTimer;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * @author Amir Modan (amir5modan@gmail.com)
 * Activity in which 5 trials are recorded for a select exercise
 *
 * Functions include:
 *  Triggering the collection of data for 5 trials
 *  Before each trial, prompts the user 3 seconds in advance that data is going to be collected
 *  After each trial, allows the user to either advance to the next trial or return to the selection screen
 *  Once all 5 trials are complete, automatically directs the user to the exercise selection screen
 */
public class ExercisePerform extends AppCompatActivity {

    // Time before trial starts (milliseconds)
    private final int COUNTDOWN_TIME = 3000;

    // Declare GUI Components
    private Button stopBtn, stopExerciseBtn, contExerciseBtn, homeBtn;
    private TextView status, reps;
    private ImageView newton, check, hand;
    private AnimationDrawable newtonAnimation;
    private ProgressBar mProgressBar, mProgressBar1, repBar1, repBar2;

    // Static variables which are meant to be accessed from BluetoothService.java for recording data
    private static int recording = 0;
    private static boolean complete = false;
    private static int trial;
    private static boolean handStill = true;

    private final int NUM_TRIALS = 5;

    //Declare timers
    private CountDownTimer cTimer, finishTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_data);

        // Initialize trial
        trial = 1;

        // Assign models to views
        //mTextField = (TextView)findViewById(R.id.countdown);
        status = (TextView)findViewById(R.id.countdown_status);
        reps = (TextView)findViewById(R.id.repetitions);
        stopBtn = (Button)findViewById(R.id.stop);
        stopExerciseBtn = (Button)findViewById(R.id.stopExercise);
        contExerciseBtn = (Button)findViewById(R.id.continueExercise);
        newton = (ImageView) findViewById(R.id.newton);
        newton.setBackgroundResource(R.drawable.newton_list);
        newtonAnimation = (AnimationDrawable) newton.getBackground();
        check = (ImageView) findViewById(R.id.check);
        hand = (ImageView) findViewById(R.id.hand_position);

        // Separate image used for Finger-to-Nose exercise
        if(ExerciseSelection.getExercise().equals("FingerToNose")) {
            hand.setImageResource(R.drawable.hand_down);
        }

        // Set Action Bar
        //getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        //getSupportActionBar().setCustomView(R.layout.header);
        //homeBtn = (Button)findViewById(R.id.home_button);

        // Assign models to circular timers
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar_timerview);
        mProgressBar1 = (ProgressBar) findViewById(R.id.progressbar1_timerview);
        repBar1 = (ProgressBar) findViewById(R.id.progressbar1_repview);
        repBar2 = (ProgressBar) findViewById(R.id.progressbar_repview);
        repBar1.setMax(5);

        // Start timer for first trial
        startTimer();

        // When Home Button is clicked
        /*homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Goes to Home Page
                Intent intent = new Intent(ExercisePerform.this, MainActivity.class);
                ExercisePerform.this.startActivity(intent);
            }
        });*/

        // When Stop Recording Button is clicked during the recording of a trial
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Stop and remove Animation
                newton.setVisibility(View.INVISIBLE);
                newtonAnimation.stop();

                // Set text depending on exercise selected in previous activity, ExerciseSelection.java
                switch (ExerciseSelection.getExercise()) {
                    case "BlockPlacing":
                        status.setText(R.string.blockExercise);
                        break;
                    case "FingerToNose":
                        status.setText(R.string.fingerNoseExercise);
                        break;
                    case "CupPouring":
                        status.setText(R.string.cupExercise);
                        break;
                    case "RodPlacing":
                        status.setText(R.string.rodExercise);
                        break;
                }

                // Remove stop button
                stopBtn.setVisibility(View.GONE);

                // When there are trials remaining
                if(trial < 5) {
                    // Display buttons for stopping or continuing exercise
                    stopExerciseBtn.setVisibility(View.VISIBLE);
                    contExerciseBtn.setText(R.string.nextTrialString);
                    contExerciseBtn.setVisibility(View.VISIBLE);

                    // Display number of trials completed
                    reps.setVisibility(View.VISIBLE);
                    reps.setText(trial * 100 / NUM_TRIALS + getString(R.string.percent));
                    repBar2.setVisibility(View.VISIBLE);
                    repBar1.setVisibility(View.VISIBLE);
                    repBar1.setProgress(trial);
                }
                // When all trials are completed
                else {

                    ExerciseSelection.completeExercise(ExerciseSelection.getExercise());

                    // Notify user
                    if(ExerciseSelection.allExercisesComplete()) {
                        status.setText(R.string.allExercisesComplete);
                    } else {
                        status.setText(R.string.exerciseComplete);
                    }
                    // Display checkmark image
                    check.setImageResource(R.drawable.success);
                    check.setVisibility(View.VISIBLE);

                    // After 3 seconds, return to exercise selection screen
                    finishTimer = new CountDownTimer(COUNTDOWN_TIME, 10) {
                        public void onTick(long millisUntilFinished) {}
                        public void onFinish() {
                            if(ExerciseSelection.allExercisesComplete()) {
                                Intent intent = new Intent(ExercisePerform.this, BluetoothConnect.class);
                                ExercisePerform.this.startActivity(intent);
                            } else {
                                Intent intent = new Intent(ExercisePerform.this, ExerciseSelection.class);
                                ExercisePerform.this.startActivity(intent);
                            }
                        }
                    };
                    finishTimer.start();
                }

                //  Transitioning to next trial
                trial++;
                recording = 0;
                complete = true;
            }
        });

        // When Stop Exercise button is clicked after a trial is complete
        stopExerciseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Return to Exercise Selection screen
                Intent continueIntent = new Intent(ExercisePerform.this, MainActivity.class);
                ExercisePerform.this.startActivity(continueIntent);
            }
        });

        // When Continue Exercise button is clicked after a trail is complete
        contExerciseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Removes buttons
                stopExerciseBtn.setVisibility(View.GONE);
                contExerciseBtn.setVisibility(View.GONE);

                // Remove error image if applicable
                check.setVisibility(View.INVISIBLE);

                // Removes repetition progress view
                reps.setVisibility(View.GONE);
                repBar1.setVisibility(View.GONE);
                repBar2.setVisibility(View.GONE);

                // Starts timer for next trial
                startTimer();
            }
        });
    }

    /**
     * Destroys timer when called
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTimer();
    }

    /**
     * Starts exercise timer when called
     */
    void startTimer() {
        // Notifies user that data is about to be recorded
        if(ExerciseSelection.getExercise().equals("FingerToNose")){
            status.setText(R.string.sideExercise);
        } else {
            status.setText(R.string.flatExercise);
        }

        // Display Circular Timer counting down
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar1.setVisibility(View.VISIBLE);
        mProgressBar1.setMax(COUNTDOWN_TIME);

        // Display Hand
        hand.setVisibility(View.VISIBLE);

        // In Pre-Recording Stage
        recording = 1;
        setFailed(false);

        // Hide Animation
        newton.setVisibility(View.INVISIBLE);
        cTimer = new CountDownTimer(COUNTDOWN_TIME, 10) {
            /**
             * Called after each countdown interval
             * @param millisUntilFinished The number of milliseconds until timer is finished
             */
            public void onTick(long millisUntilFinished) {
                // On each tick, checks whether user is moving
                if(true) {
                    // Update circular timer if no movement
                    mProgressBar1.setProgress((int) (millisUntilFinished));
                } else {
                    // Movement invalidates the trial and must be restarted
                    recording = 0;

                    // Stop and remove timer immediately
                    cancelTimer();
                    mProgressBar.setVisibility(View.GONE);
                    mProgressBar1.setVisibility(View.GONE);

                    // Display buttons for stopping or continuing exercise
                    stopExerciseBtn.setVisibility(View.VISIBLE);
                    contExerciseBtn.setVisibility(View.VISIBLE);
                    contExerciseBtn.setText(R.string.retryTrialString);

                    // Display error image and message
                    check.setImageResource(R.drawable.error);
                    check.setVisibility(View.VISIBLE);
                    status.setText(R.string.exerciseFailed);
                }
            }

            /**
             * Called when timer is finished
             */
            public void onFinish() {
                //mTextField.setVisibility(View.INVISIBLE);

                // Remove circular timer
                hand.setVisibility(View.INVISIBLE);
                mProgressBar.setVisibility(View.GONE);
                mProgressBar1.setVisibility(View.GONE);

                // Start animation and recording
                newton.setVisibility(View.VISIBLE);
                newtonAnimation.start();
                stopBtn.setVisibility(View.VISIBLE);
                status.setText(R.string.exerciseInProgress);
                recording = 2;
            }
        };
        cTimer.start();
    }


    /**
     * Stops timer when called
     */
    void cancelTimer() {
        if(cTimer!=null)
            cTimer.cancel();
    }

    /**
     * Method for getting status of recording
     * @return Boolean for whether data is being recorded
     */
    public static int isRecording() {
        return recording;
    }

    /**
     * Method for getting status of trial completion
     * @return Boolean for whether trial is complete
     */
    public static boolean isComplete() {
        return complete;
    }

    /**
     * Method for getting status of trial completion
     * @param isComplete Boolean for whether trial is complete
     */
    public static void setComplete(boolean isComplete) {
        complete = isComplete;
    }

    /**
     * Method for setting exercise failure
     * @param failed Boolean for whether trial has failed
     */
    public static void setFailed(boolean failed) {
        handStill = !failed;
    }

    /**
     * Method for getting current trial
     * @return int for which trial is being performed
     */
    public static int getTrial() { return trial; }
}