package iot.com.smartmirror;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.widget.ImageView;
import android.content.pm.PackageManager;
import android.Manifest;
import android.util.Log;
import android.os.Handler;
import android.os.HandlerThread;
import android.graphics.Bitmap;
import android.view.KeyEvent;

import com.google.android.things.contrib.driver.button.Button;

import java.nio.ByteBuffer;
import java.io.IOException;

import iot.com.smartmirror.camera.SmartMirrorCamera;

public class MainActivity extends Activity implements ImageReader.OnImageAvailableListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    // FIXME to collect the GPIO PIN definition.
    private final static String BUTTON_GPIO_PIN = "BCM21";
    private SmartMirrorCamera camera = SmartMirrorCamera.getInstance();
    private Bitmap photo;
    private ImageView showPictView;

    private Button.OnButtonEventListener mButtonCallback = (button, pressed) -> {
        if (pressed) {
            // Note the take photo when enter pressed or GPIO PIN signal receive.
            Log.d(TAG, "button pressed");
            camera.takePicture();
        }
    };

    @Override
    /**
     * @Author Ryo Watanabe
     * This method based on Android Things sample library Code.
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // A problem occurred auto-granting the permission
            Log.d(TAG, "No permission");
            return;
        }

        HandlerThread cameraHandlerThread = new HandlerThread("CameraBackground");
        cameraHandlerThread.start();
        Handler cameraHandler = new Handler(cameraHandlerThread.getLooper());

        camera.initCamera(this, cameraHandler, (reader) -> {
            Image image = reader.acquireLatestImage();
                // get image bytes
                ByteBuffer imageBuf = image.getPlanes()[0].getBuffer();
                final byte[] imageBytes = new byte[imageBuf.remaining()];
                imageBuf.get(imageBytes);
                image.close();
                photo = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, null);
                showPictView.setImageBitmap(photo);
                setContentView(showPictView);
        });
        try {
            Button button = new Button(BUTTON_GPIO_PIN, Button.LogicState.PRESSED_WHEN_LOW);
            button.setOnButtonEventListener(mButtonCallback);
            showPictView = new ImageView(this);
        } catch (IOException e) {
            Log.e(TAG, "button driver error", e);
        }
    }

    /**
     * @Author Ryo Watanabe
     * Summarize back ground thread initialize.
     */
    private void init() {

    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        Log.d(TAG,"this log is enabled on implementing image reader.");
        Image image = reader.acquireLatestImage();
        // get image bytes
        ByteBuffer imageBuf = image.getPlanes()[0].getBuffer();
        final byte[] imageBytes = new byte[imageBuf.remaining()];
        imageBuf.get(imageBytes);
        image.close();
        photo = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, null);
        showPictView.setImageBitmap(photo);
        setContentView(showPictView);

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        // Note the take photo when enter pressed or GPIO PIN signal receive.
        int pressed = e.getKeyCode();
        Log.d(TAG, "this key is pressed -> " + pressed);
        if(KeyEvent.KEYCODE_ENTER == pressed) {
            camera.takePicture();
        }
        return super.dispatchKeyEvent(e);
    }

}
