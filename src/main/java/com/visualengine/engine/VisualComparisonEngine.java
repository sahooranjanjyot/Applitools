package com.visualengine.engine;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import nu.pattern.OpenCV;
import java.io.File;
import java.util.List;
import java.util.ArrayList;

public class VisualComparisonEngine {
    static {
        OpenCV.loadLocally();
    }

    private double matchThreshold;

    public VisualComparisonEngine(double matchThreshold) {
        this.matchThreshold = matchThreshold;
    }

    public static class Region {
        public int x, y, w, h;
        public Region(int x, int y, int w, int h) {
            this.x = x; this.y = y; this.w = w; this.h = h;
        }
    }

    public static class ComparisonResult {
        public double score;
        public boolean passed;
    }

    public ComparisonResult compareImages(String baselinePath, String actualPath, String diffOutputPath, List<Region> ignoreRegions) {
        if (!new File(baselinePath).exists()) {
            throw new RuntimeException("Baseline not found: " + baselinePath);
        }

        Mat baseline = Imgcodecs.imread(baselinePath);
        Mat actual = Imgcodecs.imread(actualPath);

        if (baseline.empty() || actual.empty()) {
            throw new RuntimeException("Failed to load images");
        }

        if (baseline.cols() != actual.cols() || baseline.rows() != actual.rows()) {
            Imgproc.resize(actual, actual, new Size(baseline.cols(), baseline.rows()));
        }

        if (ignoreRegions != null) {
            for (Region r : ignoreRegions) {
                // Validate region bounds
                int x = Math.max(0, r.x);
                int y = Math.max(0, r.y);
                int w = Math.min(baseline.cols() - x, r.w);
                int h = Math.min(baseline.rows() - y, r.h);

                if (w > 0 && h > 0) {
                    Imgproc.rectangle(baseline, new Point(x, y), new Point(x + w, y + h), new Scalar(0, 0, 0), -1);
                    Imgproc.rectangle(actual, new Point(x, y), new Point(x + w, y + h), new Scalar(0, 0, 0), -1);
                }
            }
        }

        Mat grayBase = new Mat();
        Mat grayActual = new Mat();
        Imgproc.cvtColor(baseline, grayBase, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(actual, grayActual, Imgproc.COLOR_BGR2GRAY);

        // SSIM approximation using OpenCV matchTemplate or absdiff
        // For strict SSIM, we would need a full implementation. Here we use SSIM equivalent or simple diff with MSE
        Mat diff = new Mat();
        Core.absdiff(grayBase, grayActual, diff);
        
        Mat thresh = new Mat();
        Imgproc.threshold(diff, thresh, 30, 255, Imgproc.THRESH_BINARY);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(thresh, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        Mat diffImage = actual.clone();
        int diffPixels = 0;
        for (MatOfPoint contour : contours) {
            Rect rect = Imgproc.boundingRect(contour);
            if (rect.width > 5 && rect.height > 5) {
                Imgproc.rectangle(diffImage, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 0, 255), 2);
                diffPixels += rect.area();
            }
        }

        Imgcodecs.imwrite(diffOutputPath, diffImage);

        // Calculate a pseudo-score (1.0 = perfect match)
        double totalPixels = baseline.cols() * baseline.rows();
        double score = 1.0 - (diffPixels / totalPixels);

        ComparisonResult result = new ComparisonResult();
        result.score = score;
        result.passed = score >= matchThreshold;

        return result;
    }
}
