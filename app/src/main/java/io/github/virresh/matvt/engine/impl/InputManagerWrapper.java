// Based on https://github.com/Genymobile/scrcpy/blob/c5ed2cfc28ee7c7b59b11eb4db1258ac1c633bff/server/src/main/java/com/genymobile/scrcpy/wrappers/InputManager.java
package io.github.virresh.matvt.engine.impl;

import android.annotation.SuppressLint;
import android.os.IBinder;
import android.view.InputEvent;
import android.view.MotionEvent;
import android.util.Log;

import java.lang.reflect.Method;

import rikka.shizuku.ShizukuBinderWrapper;
import rikka.shizuku.SystemServiceHelper;

@SuppressLint("PrivateApi,DiscouragedPrivateApi")
public final class InputManagerWrapper {

    private static final String LOG_TAG = "InputManagerWrapper";

    public static final int INJECT_INPUT_EVENT_MODE_ASYNC = 0;
    public static final int INJECT_INPUT_EVENT_MODE_WAIT_FOR_RESULT = 1;
    public static final int INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH = 2;

    private final Class<?> iManagerClass;
    private final Object iManagerInstance;
    private Method injectInputEventMethod;

    private static Method setDisplayIdMethod;
    private static Method setActionButtonMethod;



    static InputManagerWrapper create() {
        try {
            Class<?> iManagerStubClass = getIInputManagerStubClass();
            Method asInterfaceMethod = iManagerStubClass.getMethod("asInterface", IBinder.class);
            Object iManagerInstance = asInterfaceMethod.invoke(null, new ShizukuBinderWrapper(SystemServiceHelper.getSystemService("input")));

            Class<?> iManagerClass = getIInputManagerClass();
            return new InputManagerWrapper(iManagerClass, iManagerInstance);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private static Class<?> getIInputManagerClass() throws ClassNotFoundException {
        return Class.forName("android.hardware.input.IInputManager");
    }

    private static Class<?> getIInputManagerStubClass() throws ClassNotFoundException {
        return Class.forName("android.hardware.input.IInputManager$Stub");
    }

    private InputManagerWrapper(Class<?> iManagerClass, Object iManagerInstance) {
        this.iManagerClass = iManagerClass;
        this.iManagerInstance = iManagerInstance;
    }



    private Method getInjectInputEventMethod() throws NoSuchMethodException {
        if (injectInputEventMethod == null) {
            injectInputEventMethod = iManagerClass.getMethod("injectInputEvent", InputEvent.class, int.class);
        }
        return injectInputEventMethod;
    }

    public boolean injectInputEvent(InputEvent inputEvent, int mode) {
        try {
            Method method = getInjectInputEventMethod();
            return (boolean) method.invoke(iManagerInstance, inputEvent, mode);
        } catch (ReflectiveOperationException e) {
            Log.e(LOG_TAG, "Could not invoke method", e);
            return false;
        }
    }



    private static Method getSetDisplayIdMethod() throws NoSuchMethodException {
        if (setDisplayIdMethod == null) {
            setDisplayIdMethod = InputEvent.class.getMethod("setDisplayId", int.class);
        }
        return setDisplayIdMethod;
    }

    public static boolean setDisplayId(InputEvent inputEvent, int displayId) {
        try {
            Method method = getSetDisplayIdMethod();
            method.invoke(inputEvent, displayId);
            return true;
        } catch (ReflectiveOperationException e) {
            Log.e(LOG_TAG, "Cannot associate a display id to the input event", e);
            return false;
        }
    }



    private static Method getSetActionButtonMethod() throws NoSuchMethodException {
        if (setActionButtonMethod == null) {
            setActionButtonMethod = MotionEvent.class.getMethod("setActionButton", int.class);
        }
        return setActionButtonMethod;
    }

    public static boolean setActionButton(MotionEvent motionEvent, int actionButton) {
        try {
            Method method = getSetActionButtonMethod();
            method.invoke(motionEvent, actionButton);
            return true;
        } catch (ReflectiveOperationException e) {
            Log.e(LOG_TAG, "Cannot set action button on MotionEvent", e);
            return false;
        }
    }
}
