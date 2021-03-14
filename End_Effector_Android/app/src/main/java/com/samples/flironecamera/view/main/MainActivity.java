/*
 * ******************************************************************
 * @title FLIR THERMAL SDK
 * @file MainActivity.java
 * @Author FLIR Systems AB
 *
 * @brief  Main UI of test application
 *
 * Copyright 2019:    FLIR Systems
 * ******************************************************************/

package com.samples.flironecamera.view.main;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.flir.thermalsdk.ErrorCode;
import com.flir.thermalsdk.androidsdk.BuildConfig;
import com.flir.thermalsdk.androidsdk.ThermalSdkAndroid;
import com.flir.thermalsdk.androidsdk.live.connectivity.UsbPermissionHandler;
import com.flir.thermalsdk.image.Point;
import com.flir.thermalsdk.image.ThermalImage;
import com.flir.thermalsdk.live.CommunicationInterface;
import com.flir.thermalsdk.live.Identity;
import com.flir.thermalsdk.live.connectivity.ConnectionStatusListener;
import com.flir.thermalsdk.live.discovery.DiscoveryEventListener;
import com.flir.thermalsdk.log.ThermalLog;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.samples.flironecamera.R;
import com.samples.flironecamera.camera.CameraSourcePreview;
import com.samples.flironecamera.camera.FaceGraphic;
import com.samples.flironecamera.camera.GraphicOverlay;
import com.samples.flironecamera.view.chatbot.ChatBotActivity;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Sample application for scanning a FLIR ONE or a built in emulator
 * <p>
 * See the {@link CameraHandler} for how to preform discovery of a FLIR ONE camera, connecting to it and start streaming images
 * <p>
 * The MainActivity is primarily focused to "glue" different helper classes together and updating the UI components
 * <p/>
 * Please note, this is <b>NOT</b> production quality code, error handling has been kept to a minimum to keep the code as clear and concise as possible
 */
public class MainActivity extends AppCompatActivity {

    //private static final int RC_HANDLE_GMS = 9001;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    //private static final int Y_SHIFT = -150;
    //private static final int W_SHIFT = 50;
    private double threshold = 38.5;
    private double mX, mY;

    private static final String TAG = "MainActivity";
    //private static final int FRAME_RATE = 10;

    //Handles Android permission for eg Network
    private PermissionHandler permissionHandler;

    //Handles network camera operations
    private CameraHandler cameraHandler;

    private Identity connectedIdentity = null;

    private ImageView photoImage;

    private TextView resultTextView;
    private LinearLayout backgroundView;

    private TextToSpeech mTTS1;
    private TextToSpeech mTTS2;
    private TextView textView1;
    private TextView textView2;

    private MyView myView;

    private LinkedBlockingQueue<FrameDataHolder> framesBuffer = new LinkedBlockingQueue(21);
    private UsbPermissionHandler usbPermissionHandler = new UsbPermissionHandler();

    private CameraSource mCameraSource = null;

    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;

    private boolean was_red = false;

    private double mx = 240, my = 500;
    /** used to deal with sudden spikes by making sure three consecutive temperature values all exceed threshold to go red */
    private List<Double> temps = Collections.synchronizedList(new ArrayList<>());

    /** Show message on the screen */
    public interface ShowMessage {
        void show(String message);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //<- Keeps the device screen awake
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ThermalLog.LogLevel enableLoggingInDebug = BuildConfig.DEBUG ? ThermalLog.LogLevel.DEBUG : ThermalLog.LogLevel.NONE; //huh?

        //ThermalSdkAndroid has to be initiated from a Activity with the Application Context to prevent leaking Context,
        // and before ANY using any ThermalSdkAndroid functions
        //ThermalLog will show log from the Thermal SDK in standards android log framework
        ThermalSdkAndroid.init(getApplicationContext(), enableLoggingInDebug);

        permissionHandler = new PermissionHandler(showMessage, MainActivity.this);

        cameraHandler = new CameraHandler();

        setupViews();

        backgroundView.setBackgroundColor(Color.BLUE);
        textView1 = findViewById(R.id.top_text);
        textView2 = findViewById(R.id.bottom_text);
        textView1.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        textView2.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        textView1.setSelected(true);
        textView2.setSelected(true);
        textView2.setVisibility(View.GONE);

        talk_bottom();
    }

    private void talk_top() {
        mTTS1 = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTTS1.setLanguage(Locale.US); //change to English
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported");
                    } else {
                        new Timer().scheduleAtFixedRate(new TimerTask() {
                            @Override
                            public void run() {
                                speak1();
                            }
                        }, 0, 10000);
                    }
                } else {
                    Log.e("TTS", "Initialization failed");
                }
            }
        });
    }

    private void talk_bottom() {
        mTTS2 = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTTS2.setLanguage(Locale.US); //change to English
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported");
                    }
                    backgroundView.setBackgroundColor(Color.WHITE);
                } else {
                    Log.e("TTS", "Initialization failed");
                }
            }
        });
    }

    private void speak1() {
        String text1 = textView1.getText().toString();
        //mTTS1.speak(text1, TextToSpeech.QUEUE_ADD, null);

        if (mTTS2.isSpeaking() == false) {
            mTTS1.speak(text1, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void speak2() {
        String text2 = textView2.getText().toString();
        //mTTS1.speak(text2, TextToSpeech.QUEUE_ADD, null);
        if (mTTS2.isSpeaking() == false) {
            mTTS2.speak(text2, TextToSpeech.QUEUE_ADD, null, null);
        }
    }

    public void startDiscovery(View view) {
        startDiscovery();
    }

    public void stopDiscovery(View view) {
        stopDiscovery();
    }

    public void connectFlirOne(View view) {
        connect(cameraHandler.getFlirOne());
    }

    public void disconnect(View view) {
        disconnect();
    }

    /**
     * Handle Android permission request response for Bluetooth permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult() called with: requestCode = [" + requestCode + "], /permissions = [" + permissions + "], grantResults = [" + grantResults + "]");
        permissionHandler.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * Connect to a Camera
     */
    private void connect(Identity identity) {
        //We don't have to stop a discovery but it's nice to do if we have found the camera that we are looking for
        cameraHandler.stopDiscovery(discoveryStatusListener);

        if (connectedIdentity != null) {
            Log.d(TAG, "connect(), in *this* code sample we only support one camera connection at the time");
            showMessage.show("connect(), in *this* code sample we only support one camera connection at the time");
            return;
        }

        if (identity == null) {
            Log.d(TAG, "connect(), can't connect, no camera available");
            showMessage.show("connect(), can't connect, no camera available");
            return;
        }

        connectedIdentity = identity;

        updateConnectionText(identity, "CONNECTING");
        //IF your using "USB_DEVICE_ATTACHED" and "usb-device vendor-id" in the Android Manifest
        // you don't need to request permission, see documentation for more information
        if (UsbPermissionHandler.isFlirOne(identity)) {
            usbPermissionHandler.requestFlirOnePermisson(identity, this, permissionListener);
        } else {
            doConnect(identity);
        }

    }

    private UsbPermissionHandler.UsbPermissionListener permissionListener = new UsbPermissionHandler.UsbPermissionListener() {
        @Override
        public void permissionGranted(Identity identity) {
            doConnect(identity);
        }

        @Override
        public void permissionDenied(Identity identity) {
            MainActivity.this.showMessage.show("Permission was denied for identity ");
        }

        @Override
        public void error(ErrorType errorType, final Identity identity) {
            MainActivity.this.showMessage.show("Error when asking for permission for FLIR ONE, error:" + errorType + " identity:" + identity);
        }
    };

    private void doConnect(Identity identity) {
        new Thread(() -> {
            try {
                cameraHandler.connect(identity, connectionStatusListener);
                runOnUiThread(() -> {
                    updateConnectionText(identity, "CONNECTED");
                    cameraHandler.startStream(streamDataListener);
                });
            } catch (IOException e) {
                runOnUiThread(() -> {
                    Log.d(TAG, "Could not connect: " + e);
                    updateConnectionText(identity, "DISCONNECTED");
                });
            }
        }).start();
    }

    /* Disconnect to a camera */
    private void disconnect() {
        updateConnectionText(connectedIdentity, "DISCONNECTING");
        connectedIdentity = null;
        Log.d(TAG, "disconnect() called with: connectedIdentity = [" + connectedIdentity + "]");
        new Thread(() -> {
            cameraHandler.disconnect();
            runOnUiThread(() -> {
                updateConnectionText(null, "DISCONNECTED");
            });
        }).start();
    }

    /* Update the UI text for connection status */
    private void updateConnectionText(Identity identity, String status) {
        String deviceId = identity != null ? identity.deviceId : "";
        Toast.makeText(this, getString(R.string.connection_status_text, deviceId + " " + status), Toast.LENGTH_SHORT).show();
    }

    /* Start camera discovery */
    private void startDiscovery() {
        cameraHandler.startDiscovery(cameraDiscoveryListener, discoveryStatusListener);
    }

    /* Stop camera discovery */
    private void stopDiscovery() {
        cameraHandler.stopDiscovery(discoveryStatusListener);
    }

    /* Callback for discovery status, using it to update UI */
    private CameraHandler.DiscoveryStatus discoveryStatusListener = new CameraHandler.DiscoveryStatus() {
        @Override
        public void started() {
            Toast.makeText(MainActivity.this, getString(R.string.connection_status_text, "discovering"), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void stopped() {
            Toast.makeText(MainActivity.this, getString(R.string.connection_status_text, "not discovering"), Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * Camera connecting state thermalImageStreamListener, keeps track of if the camera is connected or not
     * <p>
     * Note that callbacks are received on a non-ui thread so have to eg use {@link #runOnUiThread(Runnable)} to interact view UI components
     */
    private ConnectionStatusListener connectionStatusListener = new ConnectionStatusListener() {
        @Override
        public void onDisconnected(@org.jetbrains.annotations.Nullable ErrorCode errorCode) {
            Log.d(TAG, "onDisconnected errorCode:" + errorCode);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateConnectionText(connectedIdentity, "DISCONNECTED");
                }
            });
        }
    };

    private final CameraHandler.StreamDataListener streamDataListener = new CameraHandler.StreamDataListener() {

        @Override
        public void images(Bitmap dcBitmap) {
            try {
                framesBuffer.put(new FrameDataHolder(dcBitmap));
            } catch (InterruptedException e) {
                //if interrupted while waiting for adding a new item in the queue
                Log.e(TAG, "images(), unable to add incoming images to frames buffer, exception:" + e);
            }

            runOnUiThread(() -> {
                Log.d(TAG, "framebuffer size:" + framesBuffer.size());
                FrameDataHolder poll = framesBuffer.poll();
                photoImage.setImageBitmap(poll.dcBitmap);
            });

        }

        /** Gets image, calculates temperature and changes screen according*/
        @Override
        public void images(ThermalImage thermalImage) {
            Log.d("images", String.valueOf(Looper.getMainLooper().getThread() == Thread.currentThread()));

            int[] x_vals = {-1, 0, 1, -1, 0, 1, -1, 0, 1};
            int[] y_vals = {1, 1, 1, 0, 0, 0, -1, -1, -1};

            double temp = 0.0;
            double max_x = 0, max_y = 0;
            for (int i = 0; i < 8; i++) {
                int nx = (int) mx + x_vals[i];
                int ny = (int) my + y_vals[i];
                Point point = new Point((int) nx, (int) ny);
                if (temp < thermalImage.getValueAt(point) - 273.15) {
                    temp = thermalImage.getValueAt(point) - 273.15;
                    max_x = nx; max_y = ny;
                }
            }

            final double finalMax_x = max_x;
            final double finalMax_y = max_y;
            runOnUiThread(() -> {
                myView.setCoord(finalMax_x, finalMax_y);
                Log.d(TAG, "images: " + finalMax_x + ", " + finalMax_y);
            });


            temps.add(temp);
            if (temps.size() > 2) {
                boolean isRed = true;
                for (double t: temps) {
                    if (t < threshold) {
                        isRed = false;
                    }
                }

                if (was_red == false) {
                    if (isRed) {
                        was_red = true;
                        myView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                was_red = false;
                            }
                        }, 7000);
                        runOnUiThread(() -> {
                            startActivity(new Intent(getApplicationContext(), ChatBotActivity.class));
                        });
                    } else {
                        runOnUiThread(() -> {
                            backgroundView.setBackgroundColor(Color.WHITE);
                            resultTextView.setTextColor(Color.GREEN);
                            textView2.setVisibility(View.GONE);
                        });
                    }
                }

                temps.remove(0);
            }
        }
    };

    /**
     * Camera Discovery thermalImageStreamListener, is notified if a new camera was found during a active discovery phase
     * <p>
     * Note that callbacks are received on a non-ui thread so have to eg use {@link #runOnUiThread(Runnable)} to interact view UI components
     */
    private DiscoveryEventListener cameraDiscoveryListener = new DiscoveryEventListener() {
        @Override
        public void onCameraFound(Identity identity) {
            Log.d(TAG, "onCameraFound identity:" + identity);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    cameraHandler.add(identity);
                }
            });
        }

        @Override
        public void onDiscoveryError(CommunicationInterface communicationInterface, ErrorCode errorCode) {
            Log.d(TAG, "onDiscoveryError communicationInterface:" + communicationInterface + " errorCode:" + errorCode);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    stopDiscovery();
                    MainActivity.this.showMessage.show("onDiscoveryError communicationInterface:" + communicationInterface + " errorCode:" + errorCode);
                }
            });
        }
    };

    private ShowMessage showMessage = new ShowMessage() {
        @Override
        public void show(String message) {
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    };

    private void setupViews() {
//        connectionStatus = findViewById(R.id.connection_status_text);
//        discoveryStatus = findViewById(R.id.discovery_status);
        backgroundView = findViewById(R.id.background);

        photoImage = findViewById(R.id.photo_image);
        myView = findViewById(R.id.myView);

        resultTextView = findViewById(R.id.result_textView);

        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }
    }

    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

    }

    private void createCameraSource() {
        FaceDetector detector = new FaceDetector.Builder(this)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        mCameraSource = new CameraSource.Builder(this, detector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30.0f)
                .build();

    }

    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();

        talk_top();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();

        if (mTTS1.isSpeaking()) {
            mTTS1.stop();
            mTTS1.shutdown();
        }

        if (mTTS2.isSpeaking()) {
            mTTS2.stop();
            mTTS2.shutdown();
        }
    }

    private void startCameraSource() {
        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    FaceGraphic.ITemp iTemp = new FaceGraphic.ITemp() {

        @Override
        public double getTemp(int faceId) {
            double temp = temps.size() == 0 ? 0 : temps.get(temps.size() - 1);
            return temp;
        }
    };

    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay, iTemp);
        }
    }

    private static class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;

        GraphicFaceTracker(GraphicOverlay overlay, FaceGraphic.ITemp iTemp) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay, iTemp);
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
            mOverlay.add(mFaceGraphic);
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mFaceGraphic.updateFace(face);

//            Log.d(TAG, "onUpdate: " + face.getPosition() + ", width: " + face.getWidth() + ", height: " + face.getHeight());
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Handle item selection
        switch (item.getItemId()) {
            case R.id.action_chat_bot:
                startActivity(new Intent(this, ChatBotActivity.class));
                return true;
            case R.id.action_start_discovery:
                startDiscovery();
                return true;
            case R.id.action_stop_discovery:
                stopDiscovery();
                return true;
            case R.id.action_connect_flirone:
                connectFlirOne(null);
                return true;
            case R.id.action_disconnect:
                disconnect();
                return true;
            case R.id.action_debug:
                findViewById(R.id.debug_view).setVisibility(View.VISIBLE);
                return true;
            case R.id.action_normal:
                findViewById(R.id.debug_view).setVisibility(View.GONE);
                return true;
            case R.id.action_test:
                try {
                    WebSocketClient client = new WebSocketClient(new URI("ws://192.168.0.47:8000/ws/robot/guy/")) {

                        @Override
                        public void onOpen(ServerHandshake handshakedata) {
                            //Called once connected
                            Log.d(TAG, "ws onOpen");
                        }

                        @Override
                        public void onMessage(String message) {
                            //Log.d(TAG, "ws onMessage: " +  message);
                            //receives message from server
                            try {
                                JSONObject json = new JSONObject(message);
                                JSONObject message1 = json.getJSONObject("message");
                                double x = message1.getDouble("x");
                                double y = message1.getDouble("y");

                                if (80 <= x && x <= 560) {
                                    mx = x - 80; my = y;
                                }

                                Log.d(TAG, "ws onMessage: " + x + " , " + y);
                            } catch (Exception e) {
                                Log.d(TAG, "ws onMessage: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onClose(int code, String reason, boolean remote) {
                            Log.d(TAG, "ws onClose: ");
                        }

                        @Override
                        public void onError(Exception ex) {
                            Log.e(TAG, "ws onError:", ex);
                        }
                    };
                    client.connect();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        if (mTTS1 != null || mTTS2 != null) {
            mTTS1.stop();
            mTTS2.stop();
            mTTS1.shutdown();
            mTTS2.shutdown();
        }
        if (mCameraSource != null) {
            mCameraSource.release();
        }
        super.onDestroy();
    }
}
