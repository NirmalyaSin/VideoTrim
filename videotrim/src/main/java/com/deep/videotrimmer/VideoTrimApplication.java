package com.deep.videotrimmer;

import android.app.Application;

public class VideoTrimApplication extends Application {
    private static VideoTrimApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        setInstance(this);

    }

    public static void setInstance(VideoTrimApplication instance) {
        VideoTrimApplication.instance = instance;
    }
    public static VideoTrimApplication getInstance() {
        return instance;
    }
}
