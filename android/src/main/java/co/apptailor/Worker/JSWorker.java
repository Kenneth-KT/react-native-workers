package co.apptailor.Worker;

import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.Random;

import co.apptailor.Worker.core.ReactContextBuilder;
import co.apptailor.Worker.core.WorkerSelfModule;

public class JSWorker {
    private int id;

    private String jsSlugname;
    private ReactApplicationContext reactContext;

    public JSWorker(String jsSlugname) {
        this.id = Math.abs(new Random().nextInt());
        this.jsSlugname = jsSlugname;
    }

    public int getWorkerId() {
        return this.id;
    }

    public String getName() {
        return jsSlugname;
    }

    public void runFromContext(ReactApplicationContext context, ReactContextBuilder reactContextBuilder) throws Exception {
        if (reactContext != null) {
            Log.d("JSWorker File", "line 32");
            return;
        }
        Log.d("JSWorker File", "line 35");
        reactContext = reactContextBuilder.build();
        Log.d("JSWorker File", "line 37");
        WorkerSelfModule workerSelfModule = reactContext.getNativeModule(WorkerSelfModule.class);
        Log.d("JSWorker File", "line 39");
        workerSelfModule.initialize(id, context);
        Log.d("JSWorker File", "line 41");
    }

    public void postMessage(String message) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("WorkerMessage", message);
    }

    public void onHostResume() {
        reactContext.onHostResume(null);
    }

    public void onHostPause() {
        reactContext.onHostPause();
    }

    public void terminate() {
        if (reactContext == null) {
            return;
        }

        reactContext.onHostPause();
        reactContext.destroy();
        reactContext = null;
    }
}
