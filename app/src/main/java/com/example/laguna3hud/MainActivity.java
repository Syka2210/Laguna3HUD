package com.example.laguna3hud;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import me.aflak.arduino.Arduino;
import me.aflak.arduino.ArduinoListener;
import com.github.pwittchen.weathericonview.WeatherIconView;

public class MainActivity extends AppCompatActivity implements ArduinoListener {
    private Arduino arduino;

/**
* Initialize log file
*/
    //private static final String LOG_FILE = "dataReceiveLog.txt";
    private static final String LOG_FILE = new SimpleDateFormat("yyyyMMddHHmm'.txt'").format(new Date());
/**
* Coordinates with timer handler
*/
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Coordinates coordinates;
/**
* Initialize weather
*/
    final WeatherDataService weatherDataService = new WeatherDataService(MainActivity.this);
    int REQUEST_CODE_LOCATION_PERMISSION = 1;
    TextView weather_cityName;
    TextView weather_mainTemp;
    WeatherIconView weather_icon;
    TextView weather_description;
    TextView weather_tempMin;
    TextView weather_tempMax;
    double latitude = 0;
    double longitude = 0;

/**
* Debug box
*/
    private TextView debugTextbox;

/**
* App selection pop up
*/
    Dialog appSelectPopUp;
    private CardView googleMapsCard;
    private CardView wazeCard;
    private CardView spotifyCard;
    TextView app_select_tv;
    String destination;
    String popUpMenuAppSelected;
    private static final int RECOGNIZER_RESULT = 1;
    LinearLayout appSelectionLayout;
    CardView gMapsCard;
    CardView layout_wazeCard;
    CardView layout_spotifyCard;

/**
*Declaring the volume layout and the volume text box
*/
    private TextView volume;
/**
*Declaring the Radio Layout and the textviews
*/
    private LinearLayout radio3x4grid;
    private TextView radio1x2text, radio1x3text, radio1x4text;
    //----------------------------
    CardView radio2x1card;
    private TextView radio2x1text;
    CardView radio2x2card;
    private TextView radio2x2text;
    CardView radio2x3card;
    private TextView radio2x3text;
    CardView radio2x4card;
    private TextView radio2x4text;
    //----------------------------
    private TextView radio3x2text, radio3x3text, radio3x4text;

    /**
     *Declaring the 3x3 grid Layout
     */
    private LinearLayout radio3x3grid;
    private TextView radio3x3grid1x1text;
    private TextView radio3x3grid1x2text;
    private TextView radio3x3grid1x3text;
    CardView radio3x3grid2x1card;
    CardView radio3x3grid2x2card;
    CardView radio3x3grid2x3card;
    private TextView radio3x3grid2x1text;
    private TextView radio3x3grid2x2text;
    private TextView radio3x3grid2x3text;
    private TextView radio3x3grid3x1text;
    private TextView radio3x3grid3x2text;
    private TextView radio3x3grid3x3text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
/**
* Start a background service in order to keep the app run in the background
*/
        Intent backgroundServiceIntent = new Intent(this, BackgroundService.class);
        startService(backgroundServiceIntent);
/**
 * Set the date bellow the digital clock
 * TODO: BEWARE!!! Check for regular update of the date, or at midnight it will not change. Maybe implement it with the weather refresh?
 */
        Calendar calendar = Calendar.getInstance();
        String currentDate = DateFormat.getDateInstance().format(calendar.getTime());
        TextView textDate = findViewById(R.id.textDate);
        textDate.setText(currentDate);
/**
 * Set the display always on
 * Set the Raspberry Pi Pico connection with the vendor id 11914
 * Optional you can use a Arduino with the vendor id 9025
 */
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        arduino = new Arduino(this, 250000);
        arduino.addVendorId(11914); // Use 11914 for the Raspberry Pi Pico board or 9025 for the Arduino boards
        arduino.addVendorId(9025);
/**
 *Request permission for location (in order to acquire the coordinates)
 */
        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
/**
 *Weather items
 */
        weather_cityName = findViewById(R.id.weather_cityName);
        weather_mainTemp = findViewById(R.id.weather_mainTemp);
        weather_icon = findViewById(R.id.weather_icon);
        weather_description = findViewById(R.id.weather_description);
        weather_tempMax = findViewById(R.id.weather_tempMax);
        weather_tempMin = findViewById(R.id.weather_tempMin);
// --- Start timed weather update
        //updateWeather();
        timedRefreshWeather.run();
/**
 * Debug box
 */
        debugTextbox = findViewById(R.id.debugText);
        debugTextbox.setMovementMethod(new ScrollingMovementMethod());
    }

    @Override
    public void onArduinoAttached(UsbDevice device) {

    }

    @Override
    public void onArduinoDetached() {

    }

    @Override
    public void onArduinoMessage(byte[] bytes) {

    }

    @Override
    public void onArduinoOpened() {

    }

    @Override
    public void onUsbPermissionDenied() {

    }
}