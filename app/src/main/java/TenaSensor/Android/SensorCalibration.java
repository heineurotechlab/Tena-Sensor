package TenaSensor.Android;

import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Map;

public class SensorCalibration extends AppCompatActivity {

    private Button calibrateBtn, homeBtn;
    private TextView instruction;
    private ImageView calibrationImg;
    private boolean timeout = false;
    private static short calibrating = 0;
    private static boolean calibration_failed = false;

    //Declare timer
    private static CountDownTimer cTimer;
    private CountDownTimer finishTimer;
    private Handler mHandler; // Our main handler that will receive callback notifications
    private int mInterval = 500;

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            if(calibration_failed) {
                Toast.makeText(getApplicationContext(),"Calibration failed, please try again",Toast.LENGTH_SHORT).show();
                calibration_failed = false;
            } else if(calibrating == 0) {
                instruction.setText(R.string.calibrationStepOneString);
                calibrateBtn.setVisibility(View.VISIBLE);
                calibrationImg.setImageResource(R.drawable.calibration1);
            } else if(calibrating == 2) {
                instruction.setText(R.string.calibrationStepTwoString);
                calibrateBtn.setVisibility(View.VISIBLE);
                calibrationImg.setImageResource(R.drawable.calibration2);
            } else if(calibrating == 4) {
                calibrating++;
                Toast.makeText(getApplicationContext(),"Calibration Successful",Toast.LENGTH_SHORT).show();
                calibrationImg.setImageResource(R.drawable.success);
                instruction.setText(R.string.calibrationSuccessString);
                calibrateBtn.setVisibility(View.INVISIBLE);
                finishTimer = new CountDownTimer(3000, 10) {
                    public void onTick(long millisUntilFinished) {

                    }
                    public void onFinish() {
                        calibrating++;
                        Intent continueIntent = new Intent(SensorCalibration.this, MainActivity.class);
                        SensorCalibration.this.startActivity(continueIntent);
                    }
                };
                finishTimer.start();
            } else if(calibrating == 6) {
                instruction.setText(R.string.calibrationSuccessString);
                calibrateBtn.setVisibility(View.VISIBLE);
            } else if(timeout) {
                Toast.makeText(getApplicationContext(), "Calibration timed out, please try again", Toast.LENGTH_SHORT).show();
                calibrateBtn.setVisibility(View.VISIBLE);
                timeout = false;
                calibrating--;
            }
            mHandler.postDelayed(mStatusChecker, mInterval);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);
        calibrateBtn = (Button)findViewById(R.id.calibrate);
        instruction = (TextView)findViewById(R.id.calibrate_instructions);
        calibrationImg = (ImageView)findViewById(R.id.calibrate_image);

        //getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        //getSupportActionBar().setCustomView(R.layout.header);

        //homeBtn = (Button)findViewById(R.id.home_button);

        mHandler = new Handler();
        mStatusChecker.run();

        /*homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SensorCalibration.this, MainActivity.class);
                SensorCalibration.this.startActivity(intent);
            }
        });*/

        calibrateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calibrateBtn.setVisibility(View.INVISIBLE);
                instruction.setText(R.string.calibrationInProgressString);
                timeout = false;
                startTimer();
                if(calibrating == 6)
                    calibrating = 1;
                else
                    calibrating++;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTimer();
    }

    //start timer function
    void startTimer() {
        cTimer = new CountDownTimer(10000, 10) {
            public void onTick(long millisUntilFinished) {

            }
            public void onFinish() {
                timeout = true;
            }
        };
        cTimer.start();
    }


    //cancel timer
    static void cancelTimer() {
        if(cTimer!=null)
            cTimer.cancel();
    }

    public static short isCalibrating() {
        return calibrating;
    }
    public static void stopCalibration(boolean failed) {
        calibration_failed = failed;
        cancelTimer();
        if(failed) {
            calibrating--;
        } else {
            calibrating++;
        }
    }
}