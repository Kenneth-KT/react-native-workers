package co.apptailor.Worker.core;

import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.Context;
import android.util.Log;

import com.facebook.react.NativeModuleRegistryBuilder;
import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.CatalystInstance;
import com.facebook.react.cxxbridge.CatalystInstanceImpl;
import com.facebook.react.cxxbridge.JSBundleLoader;
import com.facebook.react.cxxbridge.JSCJavaScriptExecutor;
import com.facebook.react.cxxbridge.JavaScriptExecutor;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.JavaScriptModuleRegistry;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.NativeModuleCallExceptionHandler;
import com.facebook.react.cxxbridge.NativeModuleRegistry;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.bridge.queue.ReactQueueConfigurationSpec;
import com.facebook.react.devsupport.DevSupportManager;
import com.facebook.soloader.SoLoader;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class ReactContextBuilder {

    private Context parentContext;
    private JSBundleLoader jsBundleLoader;
    private DevSupportManager devSupportManager;
    private ArrayList<ReactPackage> reactPackages;
    private ApplicationInfo applicationInfo;

    public ReactContextBuilder(Context context) {
        this.parentContext = context;
        SoLoader.init(context, /* native exopackage */ false);
    }

    public ReactContextBuilder setJSBundleLoader(JSBundleLoader jsBundleLoader) {
        this.jsBundleLoader = jsBundleLoader;
        return this;
    }

    public ReactContextBuilder setDevSupportManager(DevSupportManager devSupportManager) {
        this.devSupportManager = devSupportManager;
        return this;
    }


    public ReactContextBuilder setReactPackages(ArrayList<ReactPackage> reactPackages) {
        this.reactPackages = reactPackages;
        return this;
    }
 
    public ReactContextBuilder setApplicationInfo(ApplicationInfo applicationInfo) {
        this.applicationInfo = applicationInfo;
        return this;
    } 
    public ReactApplicationContext build() throws Exception {
        JavaScriptExecutor jsExecutor = new JSCJavaScriptExecutor.Factory(new WritableNativeMap()).create();
        ApplicationInfo ai = null;
        // fresh new react context
        final ReactApplicationContext reactContext = new ReactApplicationContext(parentContext);
        Log.d("RCB", "line 79");
        try {
            PackageManager pm = reactContext.getPackageManager();
            ai = pm.getApplicationInfo(reactContext.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
  
        if (devSupportManager != null) {
            reactContext.setNativeModuleCallExceptionHandler(devSupportManager);
        }
        Log.d("RCB", "line 64");
        // load native modules
        NativeModuleRegistryBuilder nativeRegistryBuilder = new NativeModuleRegistryBuilder(reactContext, false);
        addNativeModules(reactContext, nativeRegistryBuilder);
        Log.d("RCB", "line 68");
        // load js modules
        JavaScriptModuleRegistry.Builder jsModulesBuilder = new JavaScriptModuleRegistry.Builder();
        Log.d("RCB", "line 71");
        addJSModules(jsModulesBuilder);
        Log.d("RCB", "line 73");
        CatalystInstanceImpl.Builder catalystInstanceBuilder = new CatalystInstanceImpl.Builder()
                .setReactQueueConfigurationSpec(ReactQueueConfigurationSpec.createDefault())
                .setJSExecutor(jsExecutor)
                .setRegistry(nativeRegistryBuilder.build())
                .setJSModuleRegistry(jsModulesBuilder.build())
                .setJSBundleLoader(jsBundleLoader)
                .setNativeModuleCallExceptionHandler(devSupportManager != null
                        ? devSupportManager
                        : createNativeModuleExceptionHandler()
                )
                .setApplicationInfo(ai);

        Log.d("RCB", "line 86");
        final CatalystInstance catalystInstance;
        Log.d("RCB", "line 89");
        if (jsBundleLoader != null) {
            Log.d("RCB", "jsBundleLoader not null");
        }
        if (jsExecutor != null) {
            Log.d("RCB", "jsExecutor not null");
        }
        catalystInstance = catalystInstanceBuilder.build();
        Log.d("RCB", "line 90");
        catalystInstance.getReactQueueConfiguration().getJSQueueThread().callOnQueue(
                new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        try {
                            Log.d("RCB", "line 96");
                            reactContext.initializeWithInstance(catalystInstance);
                            catalystInstance.runJSBundle();
                        } catch (Exception e) {
                            Log.d("RCB", "line 100");
                            e.printStackTrace();
                            devSupportManager.handleException(e);
                        }
                        return null;
                    }
                }
        ).get();
        Log.d("RCB", "line 107");
        catalystInstance.getReactQueueConfiguration().getUIQueueThread().callOnQueue(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                try {
                    Log.d("RCB", "line 113");
                    catalystInstance.initialize();
                    reactContext.onHostResume(null);
                } catch (Exception e) {
                    Log.d("RCB", "line 117");
                    e.printStackTrace();
                    devSupportManager.handleException(e);
                }

                return null;
            }
        }).get();

        return reactContext;
    }

    private NativeModuleCallExceptionHandler createNativeModuleExceptionHandler() {
        return new NativeModuleCallExceptionHandler() {
            @Override
            public void handleException(Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    private void addJSModules(JavaScriptModuleRegistry.Builder jsModulesBuilder) {
        for (int i = 0; i < reactPackages.size(); i++) {
            ReactPackage reactPackage = reactPackages.get(i);
            for (Class<? extends JavaScriptModule> jsModuleClass : reactPackage.createJSModules()) {
                jsModulesBuilder.add(jsModuleClass);
            }
        }
    }

    private void addNativeModules(ReactApplicationContext reactContext, NativeModuleRegistryBuilder nativeRegistryBuilder) {
        for (int i = 0; i < reactPackages.size(); i++) {
            ReactPackage reactPackage = reactPackages.get(i);
            for (NativeModule nativeModule : reactPackage.createNativeModules(reactContext)) {
                nativeRegistryBuilder.addNativeModule(nativeModule);
            }
        }
    }
}
