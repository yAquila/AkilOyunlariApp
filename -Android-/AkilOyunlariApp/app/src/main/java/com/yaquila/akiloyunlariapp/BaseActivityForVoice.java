package com.yaquila.akiloyunlariapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.ViewConfiguration;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewConfigurationCompat;

import com.yaquila.akiloyunlariapp.model.ConstantApp;
import com.yaquila.akiloyunlariapp.model.CurrentUserSettings;
import com.yaquila.akiloyunlariapp.model.EngineConfig;
import com.yaquila.akiloyunlariapp.model.MyEngineEventHandler;
import com.yaquila.akiloyunlariapp.model.WorkerThread;
import com.yaquila.akiloyunlariapp.propeller.Constant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import io.agora.rtc.RtcEngine;

public abstract class BaseActivityForVoice extends AppCompatActivity {
    private final static Logger log = LoggerFactory.getLogger(BaseActivityForVoice.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        final View layout = findViewById(Window.ID_ANDROID_CONTENT);
//        ViewTreeObserver vto = layout.getViewTreeObserver();
//        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                    layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                } else {
//                    layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
//                }
//                initUIandEvent();
//            }
//        });
    }

    protected abstract void initUIandEvent();

    protected abstract void deInitUIandEvent();

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    return;
                }

                boolean checkPermissionResult = checkSelfPermissions();

                if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.M)) {
                    // so far we do not use OnRequestPermissionsResultCallback
                }
            }
        }, 500);
    }

    private boolean checkSelfPermissions() {
        return checkSelfPermission(Manifest.permission.RECORD_AUDIO, ConstantApp.PERMISSION_REQ_ID_RECORD_AUDIO) &&
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, ConstantApp.PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE);
    }

    @Override
    protected void onDestroy() {
        deInitUIandEvent();
        super.onDestroy();
    }

    public boolean checkSelfPermission(String permission, int requestCode) {
        log.debug("checkSelfPermission " + permission + " " + requestCode);
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{permission},
                    requestCode);
            return false;
        }

        if (Manifest.permission.RECORD_AUDIO.equals(permission)) {
            ((AGApplication) getApplication()).initWorkerThread();
        }
        return true;
    }

    protected RtcEngine rtcEngine() {
        return ((AGApplication) getApplication()).getWorkerThread().getRtcEngine();
    }

    protected final WorkerThread worker() {
        return ((AGApplication) getApplication()).getWorkerThread();
    }

    protected final EngineConfig config() {
        return ((AGApplication) getApplication()).getWorkerThread().getEngineConfig();
    }

    protected final MyEngineEventHandler event() {
        return ((AGApplication) getApplication()).getWorkerThread().eventHandler();
    }

    public final void showLongToast(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        log.debug("onRequestPermissionsResult " + requestCode + " " + Arrays.toString(permissions) + " " + Arrays.toString(grantResults));
        switch (requestCode) {
            case ConstantApp.PERMISSION_REQ_ID_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, ConstantApp.PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE);
                    ((AGApplication) getApplication()).initWorkerThread();
                } else {
                    finish();
                }
                break;
            }
            case ConstantApp.PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    finish();
                }
                break;
            }
        }
    }

    protected CurrentUserSettings vSettings() {
        return AGApplication.mAudioSettings;
    }

    protected int virtualKeyHeight() {
        boolean hasPermanentMenuKey = ViewConfigurationCompat.hasPermanentMenuKey(ViewConfiguration.get(getApplication()));

        DisplayMetrics metrics = new DisplayMetrics();
        Display display = getWindowManager().getDefaultDisplay();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealMetrics(metrics);
        } else {
            display.getMetrics(metrics);
        }

        int fullHeight = metrics.heightPixels;

        display.getMetrics(metrics);

        return fullHeight - metrics.heightPixels;
    }

    protected void initVersionInfo() {
        String version = "V " + BuildConfig.VERSION_NAME + "(Build: " + BuildConfig.VERSION_CODE
                + ", " + ConstantApp.APP_BUILD_DATE + ", SDK: " + Constant.MEDIA_SDK_VERSION + ")";
//        TextView textVersion = (TextView) findViewById(R.id.app_version);
//        textVersion.setText(version);
    }
}
