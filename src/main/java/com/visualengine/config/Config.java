package com.visualengine.config;

import java.io.File;

public class Config {
    public enum ExecutionMode {
        BASELINE, COMPARE, APPROVAL, HEAL
    }

    public static ExecutionMode MODE;
    public static final String LOCATOR_MAP_FILE = System.getProperty("user.dir") + "/locator_map.json";
    
    public static final double PIXEL_MATCH_THRESHOLD;

    static {
        String modeStr = System.getenv("VISUAL_EXECUTION_MODE");
        MODE = (modeStr != null) ? ExecutionMode.valueOf(modeStr.toUpperCase()) : ExecutionMode.COMPARE;

        String thresholdStr = System.getenv("PIXEL_MATCH_THRESHOLD");
        PIXEL_MATCH_THRESHOLD = (thresholdStr != null) ? Double.parseDouble(thresholdStr) : 0.98;
    }

    public static String getBaselineDir(String viewport) {
        String branch = System.getenv("GIT_BRANCH") != null ? System.getenv("GIT_BRANCH") : "local";
        String dir = System.getProperty("user.dir") + "/visual_baselines/" + branch + "/" + viewport;
        new File(dir).mkdirs();
        return dir;
    }

    public static String getPendingBaselineDir(String viewport) {
        String branch = System.getenv("GIT_BRANCH") != null ? System.getenv("GIT_BRANCH") : "local";
        String dir = System.getProperty("user.dir") + "/pending_baselines/" + branch + "/" + viewport;
        new File(dir).mkdirs();
        return dir;
    }

    public static String getReportsDir() {
        String dir = System.getProperty("user.dir") + "/visual_reports";
        new File(dir).mkdirs();
        return dir;
    }
}
