package net.khalegh.batsapp.tools;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Video {
    private static final String IMAGEMAT = "png";
    private static final String ROTATE = "rotate";

    /**
     * The middle frame of the default captured video is the cover
     */
    public static final int MOD = 2;

//    public static void main(String[] args) throws Exception {
//        System.out.println(randomGrabberFFmpegImage("/home/sparrow/Videos/oceans.mp4", 2));
//    }

    /**
     * Get video thumbnails
     *
     * @param filePath: video path
     * @param mod:      video length / mod gets the few frames
     * @throws Exception
     */
    public static String randomGrabberFFmpegImage(String filePath, int mod) throws Exception {
        String targetFilePath = "";
        FFmpegFrameGrabber ff = FFmpegFrameGrabber.createDefault(filePath);
        ff.start();
        String rotate = ff.getVideoMetadata(ROTATE);
        int ffLength = ff.getLengthInFrames();
        Frame f;
        int i = 0;
        int index = ffLength / mod;
        while (i < ffLength) {
            f = ff.grabImage();
            if (i == index) {
                if (null != rotate && rotate.length() > 1) {
                    OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
                    IplImage src = converter.convert(f);
                    f = converter.convert(rotate(src, Integer.valueOf(rotate)));
                }
                targetFilePath = getImagePath(filePath, i);
                doExecuteFrame(f, targetFilePath);
                break;
            }
            i++;
        }
        ff.stop();
        return targetFilePath;
    }

    /**
     * Generate thumbnail storage path based on video path
     *
     * @param filePath: video path
     * @param index:    the few frames
     * @return: the storage path of the thumbnail
     */
    private static String getImagePath(String filePath, int index) {
        return filePath.concat(".").concat(IMAGEMAT);
//        if (filePath.contains(".") && filePath.lastIndexOf(".") < filePath.length() - 1) {
//            //filePath = filePath.substring(0, filePath.lastIndexOf(".")).concat("_").concat(String.valueOf(index)).concat(".").concat(IMAGEMAT);
//            filePath = filePath.substring(0, filePath.lastIndexOf(".")).concat(".").concat(IMAGEMAT);
//        }
//        return filePath;
    }

    /**
     * Rotate the picture
     *
     * @param src
     * @param angle
     * @return
     */
    public static IplImage rotate(IplImage src, int angle) {
        IplImage img = IplImage.create(src.height(), src.width(), src.depth(), src.nChannels());
        opencv_core.cvTranspose(src, img);
        opencv_core.cvFlip(img, img, angle);
        return img;
    }

    /**
     * Capture thumbnails
     *
     * @param f
     * @param targerFilePath: cover image
     */
    public static void doExecuteFrame(Frame f, String targerFilePath) {
        if (null == f || null == f.image) {
            return;
        }
        Java2DFrameConverter converter = new Java2DFrameConverter();
        BufferedImage bi = converter.getBufferedImage(f);
        File output = new File(targerFilePath);
        try {
            ImageIO.write(bi, IMAGEMAT, output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Randomly generate random number sets based on video length
     *
     * @param baseNum: the base number, here is the video length
     * @param length:  random number set length
     * @return: a collection of random numbers
     */
    public static List<Integer> random(int baseNum, int length) {
        List<Integer> list = new ArrayList<Integer>(length);
        while (list.size() < length) {
            Integer next = (int) (Math.random() * baseNum);
            if (list.contains(next)) {
                continue;
            }
            list.add(next);
        }
        Collections.sort(list);
        return list;
    }


}