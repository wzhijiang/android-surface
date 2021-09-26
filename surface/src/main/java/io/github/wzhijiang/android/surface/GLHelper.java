package io.github.wzhijiang.android.surface;

import android.opengl.GLES20;
import android.util.Log;

public class GLHelper {

    public static void checkGlError(String tag, String op) {
        int error;
        if ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(tag, op + ": glError " + error);
            throw new RuntimeException(op + ": glError 0x" + Integer.toHexString(error));
        }
    }
}
