package nl.michielproce.cropscannedphotos;

import org.apache.commons.io.FilenameUtils;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.*;

public class Extractor {
    public static final int LINE_THICKNESS = 10;
    public static final Scalar WHITE = new Scalar(255, 255, 255, 0);
    public static final Scalar GREEN = new Scalar(0, 255, 0, 0);

    private boolean writeDebug;
    private boolean writeRects;
    private int threshold;
    private int ignoreEdge;
    private int postcrop;
    private double minAreaRatio;
    private double maxAreaRatio;

    private int outputCounter;
    private int debugOutputCounter;


    private String inFile;
    private String outFile;

    public Extractor(Properties properties, String inFile, String outFile) {
        writeDebug = Boolean.parseBoolean(properties.getProperty("write-debug", "false"));
        writeRects = Boolean.parseBoolean(properties.getProperty("write-rects", "false"));
        threshold = Integer.parseInt(properties.getProperty("threshold", "200"));
        ignoreEdge = Integer.parseInt(properties.getProperty("ignore-edge", "20"));
        postcrop = Integer.parseInt(properties.getProperty("postcrop", "5"));
        minAreaRatio = Double.parseDouble(properties.getProperty("min-area-ratio", "0.1"));
        maxAreaRatio = Double.parseDouble(properties.getProperty("max-area-ratio", "0.9"));

        this.inFile = inFile;
        this.outFile = outFile;
    }

    public int start() {
        Mat src = Imgcodecs.imread(inFile);
        if(src.empty()) {
            return 0;
        }

        Mat cropped = crop(src, ignoreEdge, true);
        Mat withBorder = border(cropped, ignoreEdge);
        Mat greyscale = grayscale(withBorder);
        Mat threshold = threshold(greyscale);
        List<MatOfPoint> contours = contours(threshold, src);
        List<Rect> rects = rects(contours, src);

        for (Rect rect : rects) {
            Mat mat = new Mat(src, rect);

//            mat = crop(mat, postcrop, false);
            writeOutput(mat);
        }

        return rects.size();
    }


    private Mat crop(Mat src, int cropSize, boolean writeDebugOutput) {
        Rect crop = new Rect(cropSize, cropSize, src.width() - (2 * cropSize), src.height() - (2 * cropSize));
        Mat dst = new Mat(src, crop);
        if(writeDebugOutput) {
            writeDebugOutput(dst, "crop");
        }
        return dst;
    }

    private Mat border(Mat src, int borderSize) {
        Mat dst = new Mat();
        Core.copyMakeBorder(src, dst, borderSize, borderSize, borderSize, borderSize, Core.BORDER_ISOLATED, WHITE);
        writeDebugOutput(dst, "border");
        return dst;
    }

    private Mat grayscale(Mat src) {
        Mat dst = new Mat();
        Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGR2GRAY);
        writeDebugOutput(dst, "greyscale");
        return dst;
    }

    private Mat threshold(Mat img) {
        Mat dst = new Mat();

        Imgproc.threshold(img, dst, threshold, 255, Imgproc.THRESH_BINARY);
        writeDebugOutput(dst, "threshold");
        return dst;
    }

    private List<MatOfPoint> contours(Mat threshold, Mat original) {
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        Mat hierarchy = new Mat();
        Imgproc.findContours(threshold, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        Mat clone = original.clone();
        Imgproc.drawContours(clone, contours, -1, GREEN, LINE_THICKNESS);
        writeDebugOutput(clone, "contours");

        return contours;
    }

    private List<Rect> rects(List<MatOfPoint> contours, Mat original) {
        List<Rect> allRects = new ArrayList<Rect>();


        for (MatOfPoint contour : contours) {
            Rect rect = Imgproc.boundingRect(contour);
            allRects.add(rect);
        }

        Collections.sort(allRects, new Comparator<Rect>() {
            @Override
            public int compare(Rect o1, Rect o2) {
                return (int) (o2.area() - o1.area());
            }
        });

        List<Rect> rects = new ArrayList<Rect>();

        Mat clone = original.clone();

        double minArea =  minAreaRatio * original.size().area();
        double maxArea =  maxAreaRatio * original.size().area();

        for (Rect rect : allRects) {
            if (rect.area() < minArea) {
                continue;
            }

            if (rect.area() > maxArea) {
                continue;
            }

            if(rect.x <= ignoreEdge) {
                double diff =  rect.x;
                rect.x -= diff;
                rect.width += diff;
            }

            if(rect.y <= ignoreEdge) {
                double diff = rect.y;
                rect.y -=  diff;
                rect.height += diff;
            }

            if(rect.br().x >= original.width() - ignoreEdge) {
                double diff = original.width() - rect.br().x;
                rect.width += diff;
            }

            if(rect.br().y >= original.height() - ignoreEdge) {
                double diff = original.height() - rect.br().y;
                rect.height += diff;
            }

            rect.x += postcrop;
            rect.y += postcrop;
            rect.width -= 2 * postcrop;
            rect.height -= 2 * postcrop;

            Imgproc.rectangle(clone, rect.tl(), rect.br(), GREEN, LINE_THICKNESS);

            rects.add(rect);
        }

        writeDebugOutput(clone, "rects");

        if(writeRects) {
            writeRects(clone);
        }

        return rects;
    }


    private void writeOutput(Mat mat) {
        String file = FilenameUtils.getFullPath(outFile) + FilenameUtils.getBaseName(outFile) + "-" + outputCounter++ + "." + FilenameUtils.getExtension(outFile);
        Imgcodecs.imwrite(file, mat);
    }

    private void writeRects(Mat mat) {
        String rectsPath = FilenameUtils.getFullPath(outFile) + "rects" + File.separator;
        new File(rectsPath).mkdirs();
        String file = rectsPath + FilenameUtils.getBaseName(outFile) + "-rects" + "." + FilenameUtils.getExtension(outFile);
        Imgcodecs.imwrite(file, mat);
    }

    private void writeDebugOutput(Mat mat, String operation) {
        if(writeDebug) {
            String debugPath = FilenameUtils.getFullPath(outFile) + "debug" + File.separator;
            new File(debugPath).mkdirs();
            String frameFile = debugPath + FilenameUtils.getBaseName(outFile) + "-" + debugOutputCounter++ + "-" + operation + "." + FilenameUtils.getExtension(outFile);
            Imgcodecs.imwrite(frameFile, mat);
        }
    }
}