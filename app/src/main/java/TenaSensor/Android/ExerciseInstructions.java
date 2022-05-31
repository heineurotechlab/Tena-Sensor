package TenaSensor.Android;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

public class ExerciseInstructions extends AppCompatActivity {

    private Button startBtn, homeBtn;
    public static String exercise = "Exercise";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);

        VideoView videoView = (VideoView) findViewById(R.id.videoView);  //casting to VideoView is not Strictly required above API level 26
        TextView exerciseName = findViewById(R.id.ExerciseText);
        TextView exerciseInstructions = findViewById(R.id.ExerciseInstructions);
        Bundle bundle = getIntent().getExtras();
        switch (bundle.getString(exercise)) {
            case "BlockPlacing":
                exerciseName.setText("Block Placing");
                exerciseInstructions.setText(R.string.block_instructions);
                videoView.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.block_video); //set the path of the video that we need to use in our VideoView
                break;
            case "FingerToNose":
                exerciseName.setText("Finger To Nose");
                exerciseInstructions.setText(R.string.water_instructions);
                videoView.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.block_video); //set the path of the video that we need to use in our VideoView
                break;
            case "CupDrinking":
                exerciseName.setText("Cup Drinking");
                exerciseInstructions.setText(R.string.cup_instructions);
                videoView.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.block_video); //set the path of the video that we need to use in our VideoView
                break;
            case "RodPlacing":
                exerciseName.setText("Rod Placing");
                exerciseInstructions.setText(R.string.rod_instructions);
                videoView.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.block_video); //set the path of the video that we need to use in our VideoView
                break;
        }

        videoView.start();

        MediaController mediaController = new MediaController(this);
        //link mediaController to videoView
        mediaController.setAnchorView(videoView);
        //allow mediaController to control our videoView
        videoView.setMediaController(mediaController);

        startBtn = (Button)findViewById(R.id.StartExerciseButton);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.header);

        homeBtn = (Button)findViewById(R.id.home_button);

        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExerciseInstructions.this, MainActivity.class);
                ExerciseInstructions.this.startActivity(intent);
            }
        });

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExerciseInstructions.this, ExercisePerform.class);
                ExerciseInstructions.this.startActivity(intent);
            }
        });
    }
}