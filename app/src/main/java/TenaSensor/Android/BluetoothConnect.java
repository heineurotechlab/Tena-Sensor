package TenaSensor.Android;

import static android.content.Context.MODE_PRIVATE;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.Manifest;

import com.amazonaws.mobileconnectors.lambdainvoker.*;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;

/**
 * @author Amir Modan (amir5modan@gmail.com)
 * Fragment which acts as the home page for the T'ena Sensor App
 *
 * Functions include:
 *  Connecting to existing T'ena Sensor by clicking on red logo (No setup required)
 *  Connecting to new T'ena Sensor by clicking on settings icon (Setup required)
 *  Disconnecting to an already connected T'ena Sensor by clicking on green logo
 *  Initiating T'ena Sensor calibration process
 *  Navigating to exercise selection screen
 *  Displaying trial statistics after exercises have been completed
 */
public class BluetoothConnect extends Fragment {

    // GUI Components
    private ImageView connectedImage;
    private ImageView disconnectedImage;
    private BluetoothAdapter mBTAdapter;
    private Set<BluetoothDevice> mPairedDevices;
    private ArrayAdapter<String> mBTArrayAdapter;

    private Handler mHandler; // Our main handler that will receive callback notifications

    private TextView status;
    private Button bluetoothButton;
    private Button exerciseButton;
    private Button calibrateButton;
    private Button connectButton;
    private Button settingsButton;
    private static int connected = 2;

    private String name, address;
    private String filename;
    private String message = "";

    private File file, path;
    private FileOutputStream stream;

    private int mInterval = 500;

    private static String nameKey = "Name Key";
    private static String addressKey = "Address Key";
    private static String exercise = "Exercise";
    public static String ip = "192.168.1.133";
    public static int port = 8080;

    private static String speed = "-";
    private static String smoothness = "-";
    private static String time = "-";

    private String NAME, MAC_ADDRESS;

    public static SharedPreferences sharedPreferences;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_PRIVILEGED
    };
    private static String[] PERMISSIONS_LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_PRIVILEGED
    };

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            switch (connected) {
                case 0:
                    connectedImage.setImageResource(R.drawable.error);
                    exerciseButton.setVisibility(View.INVISIBLE);
                    calibrateButton.setVisibility(View.INVISIBLE);
                    status.setText(R.string.btFailureMessage);
                    break;
                case 1:
                    connectedImage.setImageResource(R.drawable.success);
                    exerciseButton.setVisibility(View.VISIBLE);
                    calibrateButton.setVisibility(View.VISIBLE);
                    status.setText(R.string.btSuccessMessage);
                    break;
                case 2:
                    connectedImage.setImageResource(R.drawable.calibration1);
                    exerciseButton.setVisibility(View.INVISIBLE);
                    calibrateButton.setVisibility(View.INVISIBLE);
                    status.setText(R.string.btConnectMessage);
            }
            mHandler.postDelayed(mStatusChecker, mInterval);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final View v = inflater.inflate(R.layout.activity_bluetooth, container, false);
        assert v != null;

        bluetoothButton = v.findViewById(R.id.ToggleBluetooth);
        exerciseButton = v.findViewById(R.id.ExerciseButton);
        calibrateButton = v.findViewById(R.id.CalibrateButton);
        connectButton = v.findViewById(R.id.ConnectButton);
        settingsButton = v.findViewById(R.id.settings);
        connectedImage = v.findViewById(R.id.connected);
        status = v.findViewById(R.id.bt_status);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Gets extras from previous class
        Intent intent = getActivity().getIntent();
        Bundle bundle = intent.getExtras();

        sharedPreferences = getContext().getSharedPreferences("MAC", MODE_PRIVATE);
        Map<String, ?> savedDevices = sharedPreferences.getAll();
        MAC_ADDRESS = (String) savedDevices.get("1");
        NAME = (String) savedDevices.get("2");

        // Instantiate GUI components

        // Fields for entering IP and Port Number (not for final app)
        /*EditText text = view.findViewById(R.id.ip);
        text.setText(ip);
        EditText textPort = view.findViewById(R.id.port);
        textPort.setText(Integer.toString(port));
         */

        super.onCreate(savedInstanceState);

        mBTArrayAdapter = new ArrayAdapter<String>(this.getActivity(),android.R.layout.simple_list_item_1);
        mBTAdapter = BluetoothAdapter.getDefaultAdapter(); // get a handle on the bluetooth radio

        mHandler = new Handler();
        mStatusChecker.run();

        /*
        // Create an instance of CognitoCachingCredentialsProvider
        CognitoCachingCredentialsProvider cognitoProvider = new CognitoCachingCredentialsProvider(this.getContext(), "us-east-1:68a62898-4649-4512-ba39-cdeae41bca18", Regions.US_EAST_1);

        // Create LambdaInvokerFactory, to be used to instantiate the Lambda proxy.
        LambdaInvokerFactory factory = new LambdaInvokerFactory(this.getContext(),
                Regions.US_EAST_1, cognitoProvider);

        // Create the Lambda proxy object with a default Json data binder.
        // You can provide your own data binder by implementing
        // LambdaDataBinder.
        final AWS_Interface myInterface = factory.build(AWS_Interface.class);
        List<Double> recorded_data = new ArrayList<>();
        recorded_data.add(5.8);
        recorded_data.add(7.3);
        recorded_data.add(2.6);
        recorded_data.add(1.1);
        recorded_data.add(15.2);
        recorded_data.add(21.5);
        AWS_Request request = new AWS_Request(recorded_data);
        // The Lambda function invocation results in a network call.
        // Make sure it is not called from the main thread.
        new AsyncTask<AWS_Request, Void, AWS_Response>() {
            @Override
            protected AWS_Response doInBackground(AWS_Request... params) {
                // invoke "echo" method. In case it fails, it will throw a
                // LambdaFunctionException.
                try {
                    return myInterface.TenaFunction(params[0]);
                } catch (LambdaFunctionException lfe) {
                    Log.e("Tag", "Failed to invoke echo", lfe);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(AWS_Response result) {
                if (result == null) {
                    return;
                }

                //Toast.makeText(getContext(), result.getGreetings(), Toast.LENGTH_LONG).show();
                speed = Float.toString(result.getSpeed());
                smoothness = Float.toString(result.getSmoothness());
                time = Float.toString(result.getTime());
            }
        }.execute(request);*/

        //
        try {
            filename = bundle.getString(exercise) + ".txt";
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(filename != null) {
            path = getContext().getExternalFilesDir(null);
            file = new File(path, filename);
            try {
                stream = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        // Toggle connection to T'ena Sensor
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent bluetoothIntent = new Intent(getContext(), BluetoothSelection.class);
                startActivity(bluetoothIntent);
            }
        });

        // Toggle connection to T'ena Sensor
        bluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissions();
                getContext().startService(new Intent(getContext(), BluetoothService.class));
                if(NAME == null) {
                    Intent bluetoothIntent = new Intent(getContext(), BluetoothSelection.class);
                    startActivity(bluetoothIntent);
                }
            }
        });

        // Toggle connection to T'ena Sensor
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getContext().startService(new Intent(getContext(), BluetoothService.class));
                if(NAME == null) {
                    Intent bluetoothIntent = new Intent(getContext(), BluetoothSelection.class);
                    startActivity(bluetoothIntent);
                }
            }
        });

        // Navigate to ExerciseSelection.java, the exercise selection activity
        exerciseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if(!text.getText().toString().isEmpty()) {
                    ip = text.getText().toString();
                }
                if(!textPort.getText().toString().isEmpty()) {
                    port = Integer.parseInt(textPort.getText().toString());
                }*/
                Intent exerciseIntent = new Intent(getContext(), ExerciseSelection.class);
                startActivity(exerciseIntent);
            }
        });

        // Navigate to SensorCalibration.java, the sensor calibration activity
        calibrateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent calibrateIntent = new Intent(getContext(), SensorCalibration.class);
                startActivity(calibrateIntent);
            }
        });

        // When directed from T'ena Sensor setup activity (BluetoothSelection.java), retains Bluetooth connection
        if(bundle != null && connected != 0){
            name = bundle.getString(nameKey);
            address = bundle.getString(addressKey);
            getContext().startService(new Intent(getContext(), BluetoothService.class));
        }
    }

    private void checkPermissions(){
        int permission1 = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission2 = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_SCAN);
        if (permission1 != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    getActivity(),
                    PERMISSIONS_STORAGE,
                    1
            );
        } else if (permission2 != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                    getActivity(),
                    PERMISSIONS_LOCATION,
                    1
            );
        }
    }

    /**
     * Updates the connection status of the T'ena Sensor
     * @param deviceConnected Boolean describing whether or not the sensor is connected
     */
    public static void updateView(int deviceConnected) {
        connected = deviceConnected;
    }

    public static void setStats(String spd, String smth, String tm) {
        speed = spd;
        smoothness = smth;
        time = tm;
    }

    /**
     * Gets the current exercise
     * @return A string representing the current exercise
     */
    public static String getExercise() {
        return exercise;
    }
}
