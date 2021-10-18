package io.github.wzhijiang.android.surface;

import android.os.SystemClock;
import android.view.MotionEvent;

public class MotionEventWrapper {

    private long mLastTouchDownTime;
    private final MotionEvent.PointerCoords[] mPointerCoords = { new MotionEvent.PointerCoords() };
    private final MotionEvent.PointerProperties[] mPointerProperties = { new MotionEvent.PointerProperties() };

    public MotionEventWrapper() {
        MotionEvent.PointerProperties props = mPointerProperties[0];
        props.id = 0;
        props.toolType = 1;

        MotionEvent.PointerCoords coords = mPointerCoords[0];
        coords.orientation = 0.0f;
        coords.pressure = 1.0f;
        coords.size = 1.0f;
    }

    private void setPointerCoords(float x, float y) {
        MotionEvent.PointerCoords coords = mPointerCoords[0];
        coords.x = x;
        coords.y = y;
    }

    public MotionEvent genTouchEvent(int x, int y, int action) {
        long eventTime = SystemClock.uptimeMillis();

        if (action == MotionEvent.ACTION_DOWN) {
            this.mLastTouchDownTime = eventTime;
        }

        setPointerCoords((float) x, (float) y);
        return MotionEvent.obtain(mLastTouchDownTime, eventTime, action, 1,
                mPointerProperties, mPointerCoords, 0, 0,
                1.0f, 1.0f, 0, 0, 4098, 0);
    }
}
