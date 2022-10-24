package com.example.laguna3hud;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.pwittchen.weathericonview.WeatherIconView;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import me.aflak.arduino.Arduino;
import me.aflak.arduino.ArduinoListener;

public class MainActivity extends AppCompatActivity implements ArduinoListener {
    private static final String TAG = "MainActivity";
    /**
     * Initialize Arduino
      */
    private Arduino arduino;
    String messageReceived = "";
    public String volumeLast = "-";
/**
* Initialize log file
*/
    //private static final String LOG_FILE = "dataReceiveLog.txt";
    private static final String LOG_FILE = new SimpleDateFormat("yyyyMMddHHmm'.txt'").format(new Date());
/**
* Coordinates with timer handler
*/
    private final Handler mHandler = new Handler(Looper.getMainLooper());
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
    public int selectedPopUpApp = 1;
    /**
     * Brightness level pop up
     */
    LinearLayout brightnessBarLayout;
    ProgressBar brightnessBar;
    int brightnessIncrement = 5;
    int brightnessVal;
    int curBrightnessValue = 1;
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
     // ---> Start a background service in order to keep the app run in the background
        Intent backgroundServiceIntent = new Intent(this, BackgroundService.class);
        startService(backgroundServiceIntent);
     // ---> Ask permission for Write acces to controll brightness
        askPermission(this);
     // ---> Change to manual brightness
        stopAutoBrightness(this);
/**
 * Set the date bellow the digital clock
 * TODO: BEWARE!!! Check for regular update of the date, or at midnight it will not change. Maybe implement it with the weather refresh?
 */
        Calendar calendar = Calendar.getInstance();
        String currentDate = DateFormat.getDateInstance().format(calendar.getTime());
        TextView textDate = findViewById(R.id.textDate);
        textDate.setText(currentDate);
    /*
       Set the display always on. Set the Raspberry Pi Pico connection with the vendor id 11914
       Optional you can use a Arduino with the vendor id 9025
    */
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        arduino = new Arduino(this, 250000);
        arduino.addVendorId(11914); // Use 11914 for the Raspberry Pi Pico board or 9025 for the Arduino boards
        arduino.addVendorId(9025);

     // ---> Request permission for location (in order to acquire the coordinates)
        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
     // ---> Weather items
        weather_cityName = findViewById(R.id.weather_cityName);
        weather_mainTemp = findViewById(R.id.weather_mainTemp);
        weather_icon = findViewById(R.id.weather_icon);
        weather_description = findViewById(R.id.weather_description);
        weather_tempMax = findViewById(R.id.weather_tempMax);
        weather_tempMin = findViewById(R.id.weather_tempMin);
     // ---> Start timed weather update
        //updateWeather();
        timedRefreshWeather.run();

     // ---> Debug box
        debugTextbox = findViewById(R.id.debugText);
        debugTextbox.setMovementMethod(new ScrollingMovementMethod());
     // ---> App selection pop-up
        //appSelectPopUp = new Dialog(this);
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
     // ---> Brightness level pop-up
        brightnessBarLayout = findViewById(R.id.brightnessBarLayout);
        brightnessBar = findViewById(R.id.brightnessBar);
        brightnessBar.setMax(255);
     // ---> Declaring volume text view
        volume = findViewById(R.id.volume_text);
     // ---> Declaring the source cards
        radioCard = findViewById(R.id.sourceRadioCard);
        cdplayerCard = findViewById(R.id.sourceCdplayerCard);
        auxCard = findViewById(R.id.sourceAuxCard);
     // ---> Declaring the grid 4 layout
        grid4 = findViewById(R.id.grid4);
        grid4text1x1 = findViewById(R.id.grid4text1x1);
        grid4text2x1 = findViewById(R.id.grid4text2x1);
        grid4text3x1 = findViewById(R.id.grid4text3x1);
        grid4text1x2 = findViewById(R.id.grid4text1x2);
     // ---> Declaring radio 3x4 grid and it's cards and text views
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
     // ---> Declaring 3x3 radio grid
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
     // ---> Declaring 3x2 grid with the first column for icons and second column for text
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
     // ---> Declaring the menu Volume progress bar
        settigsMenuProgressBar = findViewById(R.id.settingMenuProgressBarr);
        menuVolumeProgressBar = findViewById(R.id.menuVolumeProgressBar);
        functionName = findViewById(R.id.functionName);
        currentValue = findViewById(R.id.currentValue);
     // ---> Declaring the muscial atmosphere grid with two progress bars
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
        bassProgressBar = findViewById(R.id.bassProgressBar);
        trebleProgressBar = findViewById(R.id.trebleProgressBar);
     // ---> Declaring the Information one box
        infoGrid = findViewById(R.id.infoGrid);
        infoGridText = findViewById(R.id.infoGridText);

        Log.i("MainActivity", "onCreate");
        logFile("ANDROID: onCreate");
    }

    @Override
    protected void onStart() {
        super.onStart();
        arduino.setArduinoListener(this);
        Log.i("MainActivity", "onStart");
        logFile("ANDROID: onStart");
     // ---> Send a message request in case the app has been restarted
        String msg = "reqMsg";
        arduino.send(msg.getBytes());
        Log.i(TAG, msg);
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
        String str = new String(bytes);
        messageReceived = messageReceived + str;

        // -- Print the message received on the debug window on the right
        if (messageReceived.contains("end_string")) debugTextbox(messageReceived);

        if (messageReceived.contains("end_string")){
            logFile(messageReceived);
            Log.i(TAG, messageReceived);
            /**
             * SOURCE
              */
            if (messageReceived.toLowerCase().contains("source")){
                source(messageReceived);
                messageReceived = "";
            }
            /**
             * VOLUME
             */
            else if (messageReceived.toLowerCase().contains("device_volume")){
                volume(messageReceived);
                messageReceived = "";
            }
            /**
             * HIGHLIGHTED BOX
             */
            else if (messageReceived.toLowerCase().contains("highlightedbox")){
                highBox(messageReceived);
                messageReceived = "";
            }
            /**
             *  RADIO
             */
            else if (messageReceived.toLowerCase().contains("view_41") || messageReceived.toLowerCase().contains("view_43")){
                // STRING EX ---> view_41 : FM : 3 (highlighted box) : 92.90 : KISS FM : 1 : end_string
                // STRING EX ---> view_43 : FM : 3 (highlighted box) : 92.90 : 100.00 : 120.20 : KISS FM : PRO FM : DIGI FM : 1 : 2 : 3 : end_string
                // STRING EX ---> view_41 : LW : 3 (highlighted box) : PTY : 162 : 2 : end_string
                // STRING EX ---> view_43 : LW : 3 (highlighted box) : PTY : 162 : 162 : 162 : 1 : 2 : - : end_string
                radioDisplay(messageReceived);
                messageReceived = "";
            }
            /**
             * DISPLAY MENU
             */
            else if (messageReceived.toLowerCase().contains("view_c1") || messageReceived.toLowerCase().contains("view_c3")){
                // STRING EX ---> view_c1 : icon1 : track_name1 : end_string
                // STRING EX ---> view_c3 : icon1 : icon2 : icon3 : track_name1 : track_name2 : track_name3 : end_string
                menuDisplay(messageReceived);
                messageReceived = "";
            }
            /**
             * VOLUME PROGRESS BAR
             */
            else if (messageReceived.toLowerCase().contains("view_70")){
                // STRING EX ---> view_70 : function_name : value : end_string
                menuVolumeProgressBar(messageReceived);
                messageReceived = "";
            }
            /**
             * MUSICAL ATMOSPHERE
             */
            else if (messageReceived.toLowerCase().contains("view_73")){
                // STRING EX ---> view_73 : 2 (high box) : icon : icon : icon : title_1 : title_2 : title_3 : + : 5 : - : 2 : end_string
                menuMusicalAtmosphere(messageReceived);
                messageReceived = "";
            }
            /**
             * SELECTION MENU
             */
            else if (messageReceived.toLowerCase().contains("view_63")){
                // The confirm_cancel_function
                // STRING EX ---> view_63 : Cancel : Confirm : _ : Do you want to reset : these parameters? : end_string
                confirm_cancel(messageReceived);
                messageReceived = "";
            }
            /**
             * INFO BOX
             */
            else if(messageReceived.toLowerCase().contains("view_52")){
                // STRING EX ---> view_52 : Message is received here bla bla : end_string
                informationBox(messageReceived);
                messageReceived = "";
            }
            /**
             * KEYPAD
             */
            else if(messageReceived.toLowerCase().contains("keypad")){
                String[] messageIds = messageReceived.split(":");
                if (messageReceived.toLowerCase().contains("menu")){
                    if (brightnessBarLayout.isShown()) brightnessBarLayout.setVisibility(View.INVISIBLE);
                    // string ex: keypad : Menu : end_string
                    appSelectionMenu(messageIds[1]);
                }else if (messageReceived.toLowerCase().contains("brightness")){
                    if (appSelectionLayout.isShown()) appSelectionLayout.setVisibility(View.INVISIBLE);
                    // string ex: keypad : brightness : end_string
                    changeBrightness(messageIds[1]);
                }else if (messageReceived.toLowerCase().contains("right") || messageReceived.toLowerCase().contains("left")){
                    if (appSelectionLayout.isShown()) appSelectionMenu(messageIds[1]);
                    else if (brightnessBarLayout.isShown()) changeBrightness(messageIds[1]);
                }
                messageReceived = "";
            }
            /**
             * DATA FOR LOG
             */
            else if(messageReceived.toLowerCase().contains("candata")){
                logFile(messageReceived);
                messageReceived = "";
            }
            // CLEAR STRING IF NOT RECOGNIZED
            else {
                //logFile(messageReceived);
                messageReceived = "";
            }
        }
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
    private final Runnable timedRefreshWeather = new Runnable() {
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
        runOnUiThread(() -> {
            debugTextbox.append("\r\n" + ">" + message);
            //debugTextbox.setText(message);
        });
    }

    public void source(final String message) {
        runOnUiThread(() -> {
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
        });
    }

    public void volume(final String message){
        runOnUiThread(() -> {
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
        });
    }

    public void highBox(final String message){
        runOnUiThread(() -> {
            Log.i(TAG, "highBox");
            logFile("ANDROID: highBox");
            //Set all card colours to main colour
            radio2x2card.setCardBackgroundColor(getResources().getColor(R.color.sourceMainBackground));
            radio2x3card.setCardBackgroundColor(getResources().getColor(R.color.sourceMainBackground));
            radio2x4card.setCardBackgroundColor(getResources().getColor(R.color.sourceMainBackground));

            radio3x3grid2x2card.setCardBackgroundColor(getResources().getColor(R.color.sourceMainBackground));
            radio3x3grid2x3card.setCardBackgroundColor(getResources().getColor(R.color.sourceMainBackground));
            //Check for highlighted box nr
            String[] messageIds = message.split(":");
            if (messageIds[1].contains("2")){
                radio2x2card.setCardBackgroundColor(getResources().getColor(R.color.sourceSelected));
                radio3x3grid2x2card.setCardBackgroundColor(getResources().getColor(R.color.sourceSelected));
            }else if (messageIds[1].contains("3")){
                radio2x3card.setCardBackgroundColor(getResources().getColor(R.color.sourceSelected));
                radio3x3grid2x3card.setCardBackgroundColor(getResources().getColor(R.color.sourceSelected));
            }else if (messageIds[1].contains("4")){
                radio2x4card.setCardBackgroundColor(getResources().getColor(R.color.sourceSelected));
            }
        });
    }

    public void radioDisplay(final String message){
        runOnUiThread(() -> {
            Log.i(TAG, "radioDisplayShort");
            logFile("ANDROID: radioDisplayShort");
            closeAllDisplays();
            clearTextBoxes();
            //Set all card colours to main colour
            radio2x2card.setCardBackgroundColor(getResources().getColor(R.color.sourceMainBackground));
            radio2x3card.setCardBackgroundColor(getResources().getColor(R.color.sourceMainBackground));
            radio2x4card.setCardBackgroundColor(getResources().getColor(R.color.sourceMainBackground));
            /**
             * One row STRING EX    --> view_41 : FM : 3 (highlighted box) : 92.90 : KISS FM : 1 : end_string
             * Three rows STRING EX --> view_43 : FM : 3 (highlighted box) : 92.90 : 100.00 : 120.20 : KISS FM : PRO FM : DIGI FM : 1 : 2 : 3 : end_string
             * ALSO!!!
             * One row STRING EX    --> view_41 : LW : 3 (highlighted box) : 102.00 : 1 : end_string
             * Three rows STRING EX --> view_43 : LW : 3 (highlighted box) : 102.00 : 102.00 : 102.00 : 1 : 2 : 3 : end_string
             */
            String[] messageIds = message.split(":");
            radio2x1text.setText(messageIds[1]);
            if (messageIds[0].toLowerCase().contains("view_41")){
                if (messageIds[5].toLowerCase().contains("end_string")){
                    radio2x3text.setText(messageIds[3]);
                    radio2x4text.setText(messageIds[4]);
                }else {
                    radio2x2text.setText(messageIds[3]);
                    radio2x3text.setText(messageIds[4]);
                    radio2x4text.setText(messageIds[5]);
                }
            }else if (messageIds[0].toLowerCase().contains("view_43")){
                if (messageIds[9].toLowerCase().contains("end_string")){
                    radio1x3text.setText(messageIds[3]);
                    radio2x3text.setText(messageIds[4]);
                    radio3x3text.setText(messageIds[5]);
                    //---------------------------------
                    radio1x4text.setText(messageIds[6]);
                    radio2x4text.setText(messageIds[7]);
                    radio3x4text.setText(messageIds[8]);
                }else {
                    radio1x2text.setText(messageIds[3]);
                    radio2x2text.setText(messageIds[4]);
                    radio3x2text.setText(messageIds[5]);
                    //---------------------------------
                    radio1x3text.setText(messageIds[6]);
                    radio2x3text.setText(messageIds[7]);
                    radio3x3text.setText(messageIds[8]);
                    //---------------------------------
                    radio1x4text.setText(messageIds[9]);
                    radio2x4text.setText(messageIds[10]);
                    radio3x4text.setText(messageIds[11]);
                }
            }else {
                Log.i("DEBUG", "radioDisplay method received a package without a correct identifier");
                logFile("ANDROID: radioDisplay method received a package without a correct identifier");
                radio2x2text.setText("incorect");
                radio2x3text.setText("identifier");
            }

            if (messageIds[2].contains("2")) radio2x2card.setCardBackgroundColor(getResources().getColor(R.color.sourceSelected));
            else if (messageIds[2].contains("3")) radio2x3card.setCardBackgroundColor(getResources().getColor(R.color.sourceSelected));
            else if (messageIds[2].contains("4")) radio2x4card.setCardBackgroundColor(getResources().getColor(R.color.sourceSelected));
            //Bring front the Radio layout.
            radio3x4grid.setVisibility(View.VISIBLE);
        });
    }

    /**
     * TODO: DELETE - Obsolete - not necessary anymore!!!
     */
    public void frequency3x3Display(final String message){
        runOnUiThread(() -> {
            Log.i(TAG, "frequency3x3Display");
            logFile("ANDROID: Method -> frequency3x3Display");
            closeAllDisplays();
            clearTextBoxes();
            //set all card colours to main colour
            radio3x3grid2x1card.setCardBackgroundColor(getResources().getColor(R.color.sourceMainBackground));
            radio3x3grid2x2card.setCardBackgroundColor(getResources().getColor(R.color.sourceMainBackground));
            radio3x3grid2x3card.setCardBackgroundColor(getResources().getColor(R.color.sourceMainBackground));
            /**
             * string ex: freq_grid3x3 : 3 (highlighted box) : PTY : 162 : - : end_string
             * string ex: freq_grid3x3 : 3 (highlighted box) : PTY : 162 : 162 : 162 : 1 : 2 : - : end_string
             */
            String[] messageIds = message.split(":");
            radio3x3grid2x1text.setText(messageIds[2]);
            if (messageIds[0].toLowerCase().contains("????????")){
                radio3x3grid2x2text.setText(messageIds[3]);
                radio3x3grid2x3text.setText(messageIds[4]);
            }else if (messageIds[0].toLowerCase().contains("????????")){
                radio3x3grid1x2text.setText(messageIds[3]);
                radio3x3grid2x2text.setText(messageIds[4]);
                radio3x3grid3x2text.setText(messageIds[5]);
                //----------------------------------------
                radio3x3grid1x3text.setText(messageIds[6]);
                radio3x3grid2x3text.setText(messageIds[7]);
                radio3x3grid3x3text.setText(messageIds[8]);
            }else {
                Log.i("DEBUG", "freq_grid method received a package without a correct identifier");
                logFile("ANDROID: freq_grid method received a package without a correct identifier");
                radio3x3grid2x2text.setText("incorect");
                radio3x3grid2x3text.setText("identifier");
            }
            if (messageIds[1].contains("2")) radio3x3grid2x2card.setCardBackgroundColor(getResources().getColor(R.color.sourceSelected));
            if (messageIds[1].contains("3")) radio3x3grid2x3card.setCardBackgroundColor(getResources().getColor(R.color.sourceSelected));
            //Bring front the layout
            radio3x3grid.setVisibility(View.VISIBLE);
        });
    }

/**
*<li> —————————————————————————————————————————————————————————————————————————————————————————————————————————————————</li>
*<li>  A three row / three columns view, usually with icons in the first column, and text on the second</li>
*<li>  There may be instances where in the second column we have additional icons (mainly in the settings - bluetooth).</li>
*      ||icon||TEXT||
*      ||icon||TEXT||
*      ||icon||TEXT||
*—————————————————————————————————————————————————————————————————————————————————————————————————————————————————
*/
    public void menuDisplay(final String message){
        runOnUiThread(() -> {
            Log.i(TAG, "menuDisplay");
            logFile("ANDROID: menuDisplay");
            closeAllDisplays();
            clearTextBoxes();
            setIconsToInvisible();
            /**
             * STRING EX ---> view_c1 : icon1 : track_name1 : end_string
             * STRING EX ---> view_c3 : icon1 : icon2 : icon3 : track_name1 : track_name2 : track_name3 : end_string
             */
            String[] messageIds = message.split(":");
            if (messageIds[0].toLowerCase().contains("view_c1")){
                // Set first icon visible
                setIconsVisible("", messageIds[1], "");
                complex2x2text.setText(messageIds[2]);
            }else if (messageIds[0].toLowerCase().contains("view_c3")){
                // Set first icon visible
                setIconsVisible(messageIds[1], messageIds[2], messageIds[3]);
                complex1x2text.setText(messageIds[4]);
                complex2x2text.setText(messageIds[5]);
                complex3x2text.setText(messageIds[6]);
            }  else {
                Log.i("DEBUG", "menuDisplay method received a package without a correct identifier");
                logFile("ANDROID: menuDisplay method received a package without a correct identifier");
                complex1x2text.setText("incorect");
                complex2x2text.setText("identifier");
            }
            complex3x2grid.setVisibility(View.VISIBLE);
        });
    }

/**—————————————————————————————————————————————————————————————————————————————————————————————————————————————————
*  A progress bar that displays the level of volume for different setting of the audio.
*        || Bluetooth volume      || +20 ||
*        ||   ---------------            ||
*—————————————————————————————————————————————————————————————————————————————————————————————————————————————————
*/
    public void menuVolumeProgressBar(final String message){
        runOnUiThread(() -> {
            Log.i(TAG, "menuVolumeProgressBar");
            logFile("ANDROID: Method -> menuVolumeProgressBar");
            closeAllDisplays();
            /**
             * string ex ---> view_70 : function_name : value : end_string
             */
            String[] messageIds = message.split(":");
            functionName.setText(messageIds[1]);
            currentValue.setText(messageIds[2]);
            menuVolumeProgressBar.setProgress(Integer.parseInt(messageIds[2]));
            settigsMenuProgressBar.setVisibility(View.VISIBLE);
        });
    }

/**—————————————————————————————————————————————————————————————————————————————————————————————————————————————————
*  The musical atmosphere menu, which has a selection circle (empty or full), and on the Bass/treble option there
*  are two level indicators for those settings.
*        || ICON ||  "Rock"         ||  --   --
*        || ICON ||  Bass/treble    ||  --   --
*        || ICON ||  "Voice"        ||  --   --
*                                        B   T
*—————————————————————————————————————————————————————————————————————————————————————————————————————————————————
 */
    private void menuMusicalAtmosphere(String message) {
        runOnUiThread(() -> {
            Log.i(TAG, "menuMusicalAtmosphere");
            logFile("ANDROID: menuMusicalAtmosphere");
            int bassVolume;
            int trebleVolume;
            closeAllDisplays();
            bassTrebleLayout.setVisibility(View.INVISIBLE);
            bassTextCard.setCardBackgroundColor(getResources().getColor(R.color.sourceMainBackground));
            trebleTextCard.setCardBackgroundColor(getResources().getColor(R.color.sourceMainBackground));
            /**
             * String ex: view_73 : 2 (high box) : icon : icon : icon : title_1 : title_2 : title_3 : + : 5 : - : 2 : end_string
             */
            String[] messageIds = message.split(":");
            if (messageIds[1].toLowerCase().contains("2")){
                //set other CardView to main colors
                bassTextCard.setCardBackgroundColor(getResources().getColor(R.color.sourceMainBackground));
                trebleTextCard.setCardBackgroundColor(getResources().getColor(R.color.sourceMainBackground));
                //set main CardView to select color
                cardRow2.setCardBackgroundColor(getResources().getColor(R.color.sourceSelected));
                cardText2.setCardBackgroundColor(getResources().getColor(R.color.sourceSelected));

            }
            if (messageIds[1].toLowerCase().contains("3")){
                //set other CardView to main colors
                cardRow2.setCardBackgroundColor(getResources().getColor(R.color.sourceMainBackground));
                cardText2.setCardBackgroundColor(getResources().getColor(R.color.sourceMainBackground));
                trebleTextCard.setCardBackgroundColor(getResources().getColor(R.color.sourceMainBackground));
                //set main CardView to select color
                bassTextCard.setCardBackgroundColor(getResources().getColor(R.color.sourceSelected));
            }
            if (messageIds[1].toLowerCase().contains("4")){
                //set other CardView to main colors
                cardRow2.setCardBackgroundColor(getResources().getColor(R.color.sourceMainBackground));
                cardText2.setCardBackgroundColor(getResources().getColor(R.color.sourceMainBackground));
                bassTextCard.setCardBackgroundColor(getResources().getColor(R.color.sourceMainBackground));
                //set main CardView to select color
                trebleTextCard.setCardBackgroundColor(getResources().getColor(R.color.sourceSelected));
            }

            if (messageIds[2].length() == 0){
                musicalAtmosphereRow1Image.setImageResource(0);
            }else if (messageIds[2].toLowerCase().contains("icon")){
                String drawable1 = messageIds[2].replace("icon_", "");
                drawable1 = drawable1.toLowerCase();
                int id = getResources().getIdentifier(drawable1, "drawable", getPackageName());
                musicalAtmosphereRow1Image.setImageResource(id);
            }
            if (messageIds[3].length() == 0){
                musicalAtmosphereRow2Image.setImageResource(0);
            }else if (messageIds[3].toLowerCase().contains("icon")){
                String drawable2 = messageIds[3].replace("icon_", "");
                drawable2 = drawable2.toLowerCase();
                int id = getResources().getIdentifier(drawable2, "drawable", getPackageName());
                musicalAtmosphereRow2Image.setImageResource(id);
            }
            if (messageIds[4].length() == 0){
                musicalAtmosphereRow3Image.setImageResource(0);
            }else if (messageIds[4].toLowerCase().contains("icon")){
                String drawable3 = messageIds[4].replace("icon_", "");
                drawable3 = drawable3.toLowerCase();
                int id = getResources().getIdentifier(drawable3, "drawable", getPackageName());
                musicalAtmosphereRow3Image.setImageResource(id);
            }

            musicalAtmosphereMainText1.setText(messageIds[5]);
            musicalAtmosphereMainText2.setText(messageIds[6]);
            musicalAtmosphereMainText3.setText(messageIds[7]);

            if (messageIds[6].toLowerCase().contains("bass")){
                bassTrebleLayout.setVisibility(View.VISIBLE);
            }

            if (messageIds[8].contains("+")){
                bassVolume = Integer.parseInt(messageIds[9]) + 10;
                bassProgressBar.setProgress(bassVolume);
            }else if (messageIds[8].contains("-")){
                bassVolume = 10 - Integer.parseInt(messageIds[9]);
                bassProgressBar.setProgress(bassVolume);
            }

            if (messageIds[10].contains("+")){
                trebleVolume = Integer.parseInt(messageIds[11]) + 10;
                trebleProgressBar.setProgress(trebleVolume);
            }else if (messageIds[10].contains("-")){
                trebleVolume = 10 - Integer.parseInt(messageIds[11]);
                trebleProgressBar.setProgress(trebleVolume);
            }

            musicalAtmosphere.setVisibility(View.VISIBLE);
        });
    }

/**—————————————————————————————————————————————————————————————————————————————————————————————————————————————————
*  A three rows on the first column with a only one box on the second column view.
*        || TEXT ||                 ||
*        ||------||                 ||
*        || TEXT ||                 ||
*        ||------||                 ||
*        || TEXT ||                 ||
*—————————————————————————————————————————————————————————————————————————————————————————————————————————————————
*/
    public void confirm_cancel(String message){
        runOnUiThread(() -> {
            Log.i(TAG, "confirm_cancel");
            logFile("ANDROID: confirm_cancel");
            closeAllDisplays();
            clearTextBoxes();
            // string ex: view_63 : Cancel : Confirm : _ : Do you want to reset : these parameters? : end_string
            String[] messageIds = message.split(":");
            //Set text
            grid4text1x1.setText(messageIds[1]);
            grid4text2x1.setText(messageIds[2]);
            grid4text3x1.setText(messageIds[3]);
            grid4text1x2.append(messageIds[4] + "\r\n" + messageIds[5] + "\r\n" + messageIds[6]);
            //----------------------------------
            grid4.setVisibility(View.VISIBLE);
        });
    }

/**—————————————————————————————————————————————————————————————————————————————————————————————————————————————————
*  A INFO view. Only one box that receives characters. It might receive some icons, have to investigate.
*        || TEXT          ||
*        || TEXT          ||
*        || TEXT          ||
*—————————————————————————————————————————————————————————————————————————————————————————————————————————————————
*/
    public void informationBox(String message){
        runOnUiThread(() -> {
            Log.i(TAG, "informationBox");
            logFile("ANDROID: informationBox");
            closeAllDisplays();
            infoGridText.setText("");
            // string ex: view_52 : Message is received here bla bla : end_string
            String[] messageIds = message.split(":");
            infoGridText.setText(messageIds[1]);
            infoGrid.setVisibility(View.VISIBLE);
        });
    }

    public void closeAllDisplays(){
        runOnUiThread(() -> {
            Log.i(TAG, "closeAllDisplays");
            logFile("ANDROID: closeAllDisplays");
            radio3x4grid.setVisibility(View.INVISIBLE);
            radio3x3grid.setVisibility(View.INVISIBLE);
            complex3x2grid.setVisibility(View.INVISIBLE);
            settigsMenuProgressBar.setVisibility(View.INVISIBLE);
            musicalAtmosphere.setVisibility(View.INVISIBLE);
            grid4.setVisibility(View.INVISIBLE);
            infoGrid.setVisibility(View.INVISIBLE);
        });
    }

    public void clearTextBoxes(){
        runOnUiThread(() -> {
            Log.i(TAG, "clearTextBoxes");
            logFile("ANDROID: Method -> clearTextBoxes");
            radio2x1text.setText("");
            radio1x2text.setText("");
            radio1x3text.setText("");
            radio1x4text.setText("");
            radio2x2text.setText("");
            radio2x3text.setText("");
            radio2x4text.setText("");
            radio3x2text.setText("");
            radio3x3text.setText("");
            radio3x4text.setText("");

            complex1x2text.setText("");
            complex2x2text.setText("");
            complex3x2text.setText("");

            grid4text1x1.setText("");
            grid4text2x1.setText("");
            grid4text3x1.setText("");
            grid4text1x2.setText("");

            radio3x3grid1x1text.setText("");
            radio3x3grid1x2text.setText("");
            radio3x3grid1x3text.setText("");
            radio3x3grid2x1text.setText("");
            radio3x3grid2x2text.setText("");
            radio3x3grid2x3text.setText("");
            radio3x3grid3x1text.setText("");
            radio3x3grid3x2text.setText("");
            radio3x3grid3x3text.setText("");
        });
    }

    public void setIconsToInvisible(){
        runOnUiThread(() -> {
            Log.i(TAG, "setIconsToInvisible");
            logFile("ANDROID: Method -> setIconsToInvisible");
            complex1x1text.setVisibility(View.INVISIBLE);
            complex2x1text.setVisibility(View.INVISIBLE);
            complex3x1text.setVisibility(View.INVISIBLE);

            //complex1x1image.setImageResource(android.R.color.transparent);
            complex1x1image.setImageResource(0);
            complex2x1image.setImageResource(0);
            complex3x1image.setImageResource(0);

            musicalAtmosphereRow1Image.setImageResource(0);
            musicalAtmosphereRow2Image.setImageResource(0);
            musicalAtmosphereRow3Image.setImageResource(0);

        });
    }

    public void setIconsVisible(final String icon1, String icon2, String icon3){
        runOnUiThread(() -> {
            Log.i(TAG, "setIconsVisible");
            logFile("ANDROID: Method -> setIconsVisible");
            if (!icon1.contains("icon")){
                complex1x1text.setText(icon1);
                complex1x1text.setVisibility(View.VISIBLE);
            }
            if (icon1.toLowerCase().contains("icon")){
                String drawable1 = icon1.replace("icon_", "");
                drawable1 = drawable1.toLowerCase();
                int id = getResources().getIdentifier(drawable1, "drawable", getPackageName());
                complex1x1image.setImageResource(id);
            }
            //-------------------------------------------------------------------------------------
            if (!icon2.contains("icon")) {
                complex2x1text.setText(icon2);
                complex2x1text.setVisibility(View.VISIBLE);
            }
            if (icon2.toLowerCase().contains("icon")){
                String drawable2 = icon2.replace("icon_", "");
                drawable2 = drawable2.toLowerCase();
                int id = getResources().getIdentifier(drawable2, "drawable", getPackageName());
                complex2x1image.setImageResource(id);
            }
            //-------------------------------------------------------------------------------------
            if (!icon3.contains("icon")) {
                complex3x1text.setText(icon3);
                complex3x1text.setVisibility(View.VISIBLE);
            }
            if (icon3.toLowerCase().contains("icon")){
                String drawable3 = icon3.replace("icon_", "");
                drawable3 = drawable3.toLowerCase();
                int id = getResources().getIdentifier(drawable3, "drawable", getPackageName());
                complex3x1image.setImageResource(id);
            }
        });

    }

    public void appSelectionMenu(String keyReceived){
        runOnUiThread(() -> {
            Log.i(TAG, "appSelectionMenu");
            logFile("ANDROID: appSelectionMenu");
//                appSelectPopUp.setContentView(R.layout.app_selection_menu);
//                appSelectPopUp.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            Intent googleMaps = getPackageManager().getLaunchIntentForPackage("com.google.android.apps.maps");
            Intent waze = getPackageManager().getLaunchIntentForPackage("com.waze");
            Intent spotify = getPackageManager().getLaunchIntentForPackage("com.spotify.music");

            //set all the app background to not selected
//                googleMapsCard.setCardBackgroundColor(getResources().getColor(R.color.popUpMenuSelected));
//                wazeCard.setCardBackgroundColor(getResources().getColor(R.color.popUpMenuSelected));
//                spotifyCard.setCardBackgroundColor(getResources().getColor(R.color.popUpMenuBackground));
//                app_select_tv.setText("0");
            gMapsCard.setCardBackgroundColor(getResources().getColor(R.color.popUpMenuBackground));
            layout_wazeCard.setCardBackgroundColor(getResources().getColor(R.color.popUpMenuBackground));
            layout_spotifyCard.setCardBackgroundColor(getResources().getColor(R.color.popUpMenuBackground));

            if (keyReceived.toLowerCase().contains("menu")){
                if (appSelectionLayout.isShown()){
                    appSelectionLayout.setVisibility(View.INVISIBLE);
                } else {
                    appSelectionLayout.setVisibility(View.VISIBLE);
                    selectedPopUpApp = 1;
                    gMapsCard.setCardBackgroundColor(getResources().getColor(R.color.popUpMenuSelected));
                }
//                    if (appSelectPopUp.isShowing()){
//                        appSelectPopUp.dismiss();
//                        selectedPopUpApp = 1;
//                    }
//                    else {
//                        selectedPopUpApp = 1;
//                        appSelectPopUp.show();
//                        googleMapsCard.setCardBackgroundColor(getResources().getColor(R.color.popUpMenuSelected));
//
//                    }
            }
            else if (keyReceived.toLowerCase().contains("right")){
                if (selectedPopUpApp == 3) selectedPopUpApp = 1;
                else selectedPopUpApp++;
            }
            else if (keyReceived.toLowerCase().contains("left")){
                if (selectedPopUpApp == 1) selectedPopUpApp = 3;
                else selectedPopUpApp--;
            }

            //check which is the current app selected and highlight it
            if (selectedPopUpApp == 1) {
                gMapsCard.setCardBackgroundColor(getResources().getColor(R.color.popUpMenuSelected));
//                    googleMapsCard.setCardBackgroundColor(getResources().getColor(R.color.popUpMenuSelected));
                Log.i(TAG, "selectedPopUp=" + selectedPopUpApp +"; googleMapsCard");
            }
            else if (selectedPopUpApp == 2) {
                layout_wazeCard.setCardBackgroundColor(getResources().getColor(R.color.popUpMenuSelected));
//                    wazeCard.setCardBackgroundColor(getResources().getColor(R.color.popUpMenuSelected));
                Log.i(TAG, "selectedPopUp=" + selectedPopUpApp +"; wazeCard");
            }
            else if (selectedPopUpApp == 3) {
                layout_spotifyCard.setCardBackgroundColor(getResources().getColor(R.color.popUpMenuSelected));
//                    spotifyCard.setCardBackgroundColor(getResources().getColor(R.color.popUpMenuSelected));
                Log.i(TAG, "selectedPopUp=" + selectedPopUpApp +"; spotifyCard");
            }

            if (keyReceived.toLowerCase().contains("enter") && appSelectionLayout.getVisibility() == View.VISIBLE){
                Log.i(TAG, "enter pressed");
                if (selectedPopUpApp == 1){
                    Log.i(TAG, "selectedPopUp=1");
                    //appSelectPopUp.dismiss();
                    popUpMenuAppSelected = "googleMaps";
                    appSelectionLayout.setVisibility(View.INVISIBLE);

                    Intent speachIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    speachIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    speachIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Please specify your destination:");
                    startActivityForResult(speachIntent, RECOGNIZER_RESULT);
                }
                else if (selectedPopUpApp == 2){
                    //appSelectPopUp.dismiss();
                    popUpMenuAppSelected = "waze";
                    appSelectionLayout.setVisibility(View.INVISIBLE);

                    Intent speachIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    speachIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    speachIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Please specify your destination:");
                    startActivityForResult(speachIntent, RECOGNIZER_RESULT);
                }
                else if (selectedPopUpApp == 3){
                    if (spotify != null){
                        //appSelectPopUp.dismiss();
                        appSelectionLayout.setVisibility(View.INVISIBLE);
                        startActivity(spotify);
                    }
                }
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        Log.i(TAG, "onActivityResult");
        logFile("ANDROID: onActivityResult -> get voice to text and open nav app with destination set");
        if (requestCode == RECOGNIZER_RESULT && resultCode == RESULT_OK){
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            destination = matches.get(0).toString();
        }
        super.onActivityResult(requestCode, resultCode, data);

        if (popUpMenuAppSelected == "googleMaps"){
            //Start GoogleMaps Intent using the data received from GoogleVoiceToText
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + destination + "&mode=d");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            popUpMenuAppSelected = "";
            startActivity(mapIntent);
        }else if (popUpMenuAppSelected == "waze"){
            /**
             * Start Waze Intent using the data received from GoogleVoiceToText
             */
            popUpMenuAppSelected = "";
        }

    }

    /**
     * TODO DELETE - OBSOLETE - just for testing
     */
    /*
    public void openApp(View v){
        Intent googleMaps = getPackageManager().getLaunchIntentForPackage("com.google.android.apps.maps");
        Intent waze = getPackageManager().getLaunchIntentForPackage("com.waze");
        Intent spotify = getPackageManager().getLaunchIntentForPackage("com.spotify.music");

        if (v.getId() == R.id.googleMaps){
            if (googleMaps != null){
                startActivity(googleMaps);
            }
        }
        if (v.getId() == R.id.waze){
            if (waze != null){
                startActivity(waze);
            }
        }
        if (v.getId() == R.id.spotify){
            if (spotify != null){
                startActivity(spotify);
            }
        }
        Log.i(TAG, "openApp");
        logFile("ANDROID: openApp - Gmaps, Waze, Spotify");
    }

     */


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

    public void askPermission(Context c){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (Settings.System.canWrite(c)){
                //you have permission to write settings
            }else {
                Intent i = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                c.startActivity(i);
            }
        }
    }

    public static void stopAutoBrightness(Activity activity) {
        android.provider.Settings.System.putInt(activity.getContentResolver(),
                android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE,
                android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
    }

    public void changeBrightness(String message){
        runOnUiThread(() -> {
            Log.i(TAG, "changeBrightness method");
            if (message.toLowerCase().contains("brightness")){
                if (brightnessBarLayout.isShown()) brightnessBarLayout.setVisibility(View.GONE);
                else {
                    try {
                        curBrightnessValue = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
                    } catch (Settings.SettingNotFoundException e) {
                        e.printStackTrace();
                    }
                    brightnessVal = curBrightnessValue;
                    brightnessBar.setProgress(curBrightnessValue);
                    brightnessBarLayout.setVisibility(View.VISIBLE);
                }
            } else if (message.toLowerCase().contains("right") && brightnessBarLayout.isShown()){
                brightnessVal = brightnessVal + brightnessIncrement;
                Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightnessVal);
                brightnessBar.setProgress(brightnessVal);
            } else if (message.toLowerCase().contains("left") && brightnessBarLayout.isShown()){
                brightnessVal = brightnessVal - brightnessIncrement;
                Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightnessVal);
                brightnessBar.setProgress(brightnessVal);
            }
        });
    }

}