package com.example.laguna3hud;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import me.aflak.arduino.Arduino;
import me.aflak.arduino.ArduinoListener;
import com.github.pwittchen.weathericonview.WeatherIconView;

public class MainActivity extends AppCompatActivity implements ArduinoListener {
    private static final String TAG = "MainActivity";
    private Arduino arduino;
    String messageReceived = "";
    public String volumeLast = "-";
    public int selectedPopUpApp = 1;

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
     * Declaring the Source Tab Layout
     */
    CardView radioCard;
    CardView cdplayerCard;
    CardView auxCard;
/**
*Declaring the volume text box
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
    /**
     * Declaring the 3x2 menu Layout
     */
    private LinearLayout complex3x2grid;
    private TextView complex1x2text, complex2x2text, complex3x2text;
    private TextView complex1x1text, complex2x1text, complex3x1text;
    //-----------------------------
    private ImageView complex1x1image, complex2x1image, complex3x1image;
    /**
     *Declaring the volume settings Layout
     */
    private LinearLayout settigsMenuProgressBar;
    private ProgressBar menuVolumeProgressBar;
    private TextView functionName;
    private TextView currentValue;
    /**
     * Declaring the musical atmosphere menu Layout
     */
    private LinearLayout musicalAtmosphere;
    private ImageView musicalAtmosphereRow1Image, musicalAtmosphereRow2Image, musicalAtmosphereRow3Image;
    private TextView musicalAtmosphereMainText1, musicalAtmosphereMainText2, musicalAtmosphereMainText3;
    CardView cardRow2;
    CardView cardText2;
    private LinearLayout bassTrebleLayout;
    CardView bassTextCard;
    CardView trebleTextCard;
    private ProgressBar bassProgressBar;
    private ProgressBar trebleProgressBar;
    /**
     * Declaring the grid 4 Layout
     */
    private LinearLayout grid4;
    private TextView grid4text1x1;
    private TextView grid4text2x1;
    private TextView grid4text3x1;
    private TextView grid4text1x2;
    /**
     *Declaring the Information one box Layout
     */
    private LinearLayout infoGrid;
    private TextView infoGridText;

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
 * Set the display always on. Set the Raspberry Pi Pico connection with the vendor id 11914
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
/**
 * Start timed weather update
 */
        //updateWeather();
        timedRefreshWeather.run();
/**
 * Debug box
 */
        debugTextbox = findViewById(R.id.debugText);
        debugTextbox.setMovementMethod(new ScrollingMovementMethod());
/**
* App selection pop-up
*/
//        appSelectPopUp = new Dialog(this);
//        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
//        @SuppressLint("InflateParams") View vi = inflater.inflate(R.layout.app_selection_menu, null);
//        googleMapsCard = vi.findViewById(R.id.googleMapsCard);
//        wazeCard = vi.findViewById(R.id.wazeCard);
//        spotifyCard = vi.findViewById(R.id.spotifyCard);
//        app_select_tv = vi.findViewById(R.id.app_select_tv);
        appSelectionLayout = findViewById(R.id.appSelectionLayout);
        gMapsCard = findViewById(R.id.gMapsCard);
        layout_wazeCard = findViewById(R.id.wazeCard);
        layout_spotifyCard = findViewById(R.id.spotifyCard);
/**
* Declaring volume text view
*/
        volume = findViewById(R.id.volume_text);
/**
* Declaring the grid 4 layout
*/
        grid4 = findViewById(R.id.grid4);
        grid4text1x1 = findViewById(R.id.grid4text1x1);
        grid4text2x1 = findViewById(R.id.grid4text2x1);
        grid4text3x1 = findViewById(R.id.grid4text3x1);
        grid4text1x2 = findViewById(R.id.grid4text1x2);
/**
 * Declaring radio 3x4 grid and it's cards and text views
 */
        radio3x4grid = findViewById(R.id.radio3x4grid);
        //--------------------------------------------
        radio1x2text = findViewById(R.id.radio1x2text);
        radio1x3text = findViewById(R.id.radio1x3text);
        radio1x4text = findViewById(R.id.radio1x4text);
        //--------------------------------------------
        radio2x1card = findViewById(R.id.radio2x1card);
        radio2x1text = findViewById(R.id.radio2x1text);
        radio2x2card = findViewById(R.id.radio2x2card);
        radio2x2text = findViewById(R.id.radio2x2text);
        radio2x3card = findViewById(R.id.radio2x3card);
        radio2x3text = findViewById(R.id.radio2x3text);
        radio2x4card = findViewById(R.id.radio2x4card);
        radio2x4text = findViewById(R.id.radio2x4text);
        //--------------------------------------------
        radio3x2text = findViewById(R.id.radio3x2text);
        radio3x3text = findViewById(R.id.radio3x3text);
        radio3x4text = findViewById(R.id.radio3x4text);
/**
* Declaring 3x3 radio grid
*/
        radio3x3grid = findViewById(R.id.radio3x3grid);
        radio3x3grid2x1card = findViewById(R.id.radio3x3grid2x1card);
        radio3x3grid2x2card = findViewById(R.id.radio3x3grid2x2card);
        radio3x3grid2x3card = findViewById(R.id.radio3x3grid2x3card);
        //--------------------------------------------
        radio3x3grid1x1text = findViewById(R.id.radio3x3grid1x1text);
        radio3x3grid1x2text = findViewById(R.id.radio3x3grid1x2text);
        radio3x3grid1x3text = findViewById(R.id.radio3x3grid1x3text);
        radio3x3grid2x1text = findViewById(R.id.radio3x3grid2x1text);
        radio3x3grid2x2text = findViewById(R.id.radio3x3grid2x2text);
        radio3x3grid2x3text = findViewById(R.id.radio3x3grid2x3text);
        radio3x3grid3x1text = findViewById(R.id.radio3x3grid3x1text);
        radio3x3grid3x2text = findViewById(R.id.radio3x3grid3x2text);
        radio3x3grid3x3text = findViewById(R.id.radio3x3grid3x3text);
/**
* Declaring 3x2 grid with the first column for icons and second column for text
*/
        //
        complex3x2grid = findViewById(R.id.complex3x2grid);
        complex1x2text = findViewById(R.id.complex1x2text);
        complex2x2text = findViewById(R.id.complex2x2text);
        complex3x2text = findViewById(R.id.complex3x2text);
        complex1x1text = findViewById(R.id.complex1x1text);
        complex2x1text = findViewById(R.id.complex2x1text);
        complex3x1text = findViewById(R.id.complex3x1text);
        //------------------------------------------------
        complex1x1image = findViewById(R.id.complex1x1image);
        complex2x1image = findViewById(R.id.complex2x1image);
        complex3x1image = findViewById(R.id.complex3x1image);
/**
* Declaring the menu Volume progress bar
*/
        settigsMenuProgressBar = findViewById(R.id.settingMenuProgressBarr);
        menuVolumeProgressBar = (ProgressBar) findViewById(R.id.menuVolumeProgressBar);
        functionName = findViewById(R.id.functionName);
        currentValue = findViewById(R.id.currentValue);
/**
* Declaring the muscial atmosphere grid with two progress bars
*/
        musicalAtmosphere = findViewById(R.id.musicalAtmosphere);
        musicalAtmosphereRow1Image = findViewById(R.id.musicalAtmosphereRow1Image);
        musicalAtmosphereRow2Image = findViewById(R.id.musicalAtmosphereRow2Image);
        musicalAtmosphereRow3Image = findViewById(R.id.musicalAtmosphereRow3Image);
        //--------------------------------------------------------
        musicalAtmosphereMainText1 = findViewById(R.id.musicalAtmosphereMainText1);
        musicalAtmosphereMainText2 = findViewById(R.id.musicalAtmosphereMainText2);
        musicalAtmosphereMainText3 = findViewById(R.id.musicalAtmosphereMainText3);
        cardRow2 = findViewById(R.id.cardRow2);
        cardText2 = findViewById(R.id.cardText2);
        bassTrebleLayout = findViewById(R.id.bassTrebleLayout);
        bassTextCard = findViewById(R.id.bassTextCard);
        trebleTextCard = findViewById(R.id.trebleTextCard);
        bassProgressBar = (ProgressBar) findViewById(R.id.bassProgressBar);
        trebleProgressBar = (ProgressBar) findViewById(R.id.trebleProgressBar);
/**
 * Declaring the Information one box
 */
        infoGrid = findViewById(R.id.infoGrid);
        infoGridText = findViewById(R.id.infoGridText);

        Log.i("MainActivity", "onCreate");
        logFile("ANDROID: onCreate");
    }

    @Override
    protected void onStart() {
        super.onStart();
        arduino.setArduinoListener((ArduinoListener) this);
        Log.i("MainActivity", "onStart");
        logFile("ANDROID: onStart");
        /*
        String msg = "reqMsg";
        arduino.send(msg.getBytes());
        Log.i(TAG, msg);
         */
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("MainActivity", "onResume");
        logFile("ANDROID: onResume");
        String msg = "reqMsg";
        arduino.send(msg.getBytes());
        Log.i("DEBUG", "The folowing message was sent to Arduino:" + msg);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("MainActivity", "onPause");
        logFile("ANDROID: onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("MainActivity", "onStop");
        logFile("ANDROID: onStop");
    }

    protected void onRestart(){
        super.onRestart();
        Log.i("MainActivity", "onRestart");
        logFile("ANDROID: onRestart");
        String msg = "reqMsg";
        arduino.send(msg.getBytes());
        Log.i("DEBUG", "The folowing message was sent to Arduino:" + msg);
    }

    @Override
    public void onArduinoAttached(UsbDevice device) {
        showToast("Arduino attached!");
        arduino.open(device);
        Log.i("MainActivity", "Arduino attached");
        logFile("ANDROID: onArduinoAttached");
    }

    @Override
    public void onArduinoDetached() {
        showToast("Arduino detached!");
        arduino.close();
        Log.i("MainActivity", "Arduino detached");
        logFile("ANDROID: onArduinoDetached");
    }

    @Override
    public void onArduinoMessage(byte[] bytes) {

    }

    @Override
    public void onArduinoOpened() {

    }

    @Override
    public void onUsbPermissionDenied() {
        Log.i("MainActivity", "USB permission denied");
        logFile("ANDROID: onUsbPermissionDenied");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        arduino.unsetArduinoListener();
        arduino.close();
        Log.i("MainActivity", "onDestroy");
        logFile("ANDROID: onDestroy");
    }

    /**
     * Timed refresh of the weather widget
     */
    private Runnable timedRefreshWeather = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void run() {
            updateWeather();
            /**
             * repeat the update every 10 minutes
             */
            mHandler.postDelayed(this, 600000);
            logFile("ANDROID: timedRefreshWeather");
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void refreshWeather(View view){
        updateWeather();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void updateWeather(){
        Log.i(TAG, "Weather updated");
        logFile("ANDROID: updateWeather");
        /**
         * Get coordinates
         */
        coordinates = new Coordinates(MainActivity.this);
        if (coordinates.canGetLocation()){
            latitude = coordinates.getLatitude();
            longitude = coordinates.getLongitude();
        } else {
            coordinates.showSettingsAlert();
        }
        if (latitude != 0 && longitude != 0){
            Log.i(TAG, "got latitude and longitude");
            Log.i(TAG, "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&appid=63b3859e68a3572bd20b683f5dde6411" + "&units=metric");
            weatherDataService.getWeatherByCoordinates(latitude, longitude, new WeatherDataService.VolleyResponseListener() {
                @Override
                public void onError(String message) {
                    Toast.makeText(MainActivity.this, "something wrong", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onResponse(String id, String main, String description, String icon, String temp, String temp_min, String temp_max, String feels_like, String pressure, String humidity, String cityName) {
                    //Toast.makeText(MainActivity.this, "Returned main= " + main + " description=" + description, Toast.LENGTH_SHORT).show();
                    weather_cityName.setText(cityName);

                    if (icon.substring(icon.length() - 1).contains("d")){
                        id = "wi_owm_day_" + id;
                    } else {
                        id = "wi_owm_night_" + id;
                    }
                    String pack = getPackageName();
                    int resId = getResources().getIdentifier(id, "string", pack);
                    if (resId != 0 ){
                        weather_icon.setIconResource(getString(resId));
                        Toast.makeText(MainActivity.this, "Weather updated", Toast.LENGTH_SHORT).show();
                    } else {
                        weather_icon.setVisibility(View.INVISIBLE);
                    }

                    int indexCharacter = temp.indexOf(".");
                    if (indexCharacter != -1){
                        temp = temp.substring(0, indexCharacter);
                        weather_mainTemp.setText(temp + "\u00B0");
                    } else Log.i(TAG, "Temp index = -1!");

                    description = description.substring(0,1).toUpperCase() + description.substring(1).toLowerCase();
                    weather_description.setText(description);

                    indexCharacter = temp_max.indexOf(".");
                    if (indexCharacter != -1){
                        temp_max = temp_max.substring(0, indexCharacter);
                        weather_tempMax.setText("H:" + temp_max + "\u00B0");
                    }
                    indexCharacter = temp_min.indexOf(".");
                    if (indexCharacter != -1){
                        temp_min = temp_min.substring(0, indexCharacter);
                        weather_tempMin.setText("L:" + temp_min + "\u00B0");
                    }

                    latitude = 0;
                    longitude = 0;
                    Log.i(TAG, "Coordinates reset are:" + latitude + " " + longitude);
                }
            });
        }
    }

    public void debugTextbox(final String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                debugTextbox.append("\r\n" + ">" + message);
                //debugTextbox.setText(message);
            }
        });
    }

    public void source(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "source");
                logFile("ANDROID: source");
                // Set all sources back to initial color
                radioCard.setCardBackgroundColor(getResources().getColor(R.color.sourceMainBackground));
                cdplayerCard.setCardBackgroundColor(getResources().getColor(R.color.sourceMainBackground));
                auxCard.setCardBackgroundColor(getResources().getColor(R.color.sourceMainBackground));
                closeAllDisplays();

                // string ex: source : radio : end_string
                String[] source = message.split(":");
                // RADIO
                if (source[1].toLowerCase().contains("radio")){
                    radioCard.setCardBackgroundColor(getResources().getColor(R.color.sourceSelected));
                }
                // CD PLAYER
                if (source[1].toLowerCase().contains("cd_player")){
                    cdplayerCard.setCardBackgroundColor(getResources().getColor(R.color.sourceSelected));
                }
                // AUX
                if (source[1].toLowerCase().contains("auxiliary audio sources")){
                    auxCard.setCardBackgroundColor(getResources().getColor(R.color.sourceSelected));
                }
            }
        });
    }

    public void volume(final String message){
        runOnUiThread(new Runnable() {
            @SuppressLint("StringFormatInvalid")
            @Override
            public void run() {
                Log.i(TAG, "METHOD - volume");
                logFile("ANDROID: volume");
                // string ex: volume : 22 : end_string
                String[] messageIds = message.split(":");
                if (messageIds[1].toLowerCase().contains("return")){
                    volume.setText(volumeLast);
                    //Log.i(TAG, "Last volume is :" + volumeLast);
                }
                else if (messageIds[1].toLowerCase().contains("pause")){
                    volume.setText(messageIds[1]);
                }
                else if (!messageIds[1].toLowerCase().contains("pause")){
                    volume.setText(messageIds[1]);
                    volumeLast = messageIds[1];
                    //Log.i(TAG, "current value is :" + volumeLast);
                }
            }
        });
    }




    public void closeAllDisplays(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "closeAllDisplays");
                logFile("ANDROID: closeAllDisplays");
                radio3x4grid.setVisibility(View.INVISIBLE);
                radio3x3grid.setVisibility(View.INVISIBLE);
                complex3x2grid.setVisibility(View.INVISIBLE);
                settigsMenuProgressBar.setVisibility(View.INVISIBLE);
                musicalAtmosphere.setVisibility(View.INVISIBLE);
                grid4.setVisibility(View.INVISIBLE);
                infoGrid.setVisibility(View.INVISIBLE);
            }
        });
    }



    public void onReqButton(View v){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String msg = "reqMsg";
                arduino.send(msg.getBytes());
                Log.i(TAG, "onReqButton");
                logFile("ANDROID: onReqButton");
            }
        });

    }

    public void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public void logFile(String message){

        FileOutputStream fos = null;
        try {
            fos = openFileOutput(LOG_FILE, MODE_APPEND);
            fos.write(message.getBytes());
            fos.write("\r\n".getBytes());

            //Toast.makeText(this, "Saved to " + getFilesDir(), Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null){
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}