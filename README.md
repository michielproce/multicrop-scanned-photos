# multicrop-scanned-photos
Detects all photos in the provided scans and extracts them to separate files. Used when digitizing a large collection of photographs.

## Example
The following scan:

<img src="https://raw.githubusercontent.com/michielproce/multicrop-scanned-photos/master/readme-img/in.jpg" width="400">

Is split into 4 separate images:

1. <img src="https://raw.githubusercontent.com/michielproce/multicrop-scanned-photos/master/readme-img/out-0.jpg" width="200">
2. <img src="https://raw.githubusercontent.com/michielproce/multicrop-scanned-photos/master/readme-img/out-1.jpg" width="200">
3. <img src="https://raw.githubusercontent.com/michielproce/multicrop-scanned-photos/master/readme-img/out-2.jpg" width="200">
4. <img src="https://raw.githubusercontent.com/michielproce/multicrop-scanned-photos/master/readme-img/out-3.jpg" width="200">

## Prerequisites
Current release is created for Java 8 on Windows 10 64-bit. Tested with TIFF files scanned at 600DPI.

Make sure you have Java installed.

## Running
Download [`multicrop-scanned-photos-1.0.zip`](https://github.com/michielproce/multicrop-scanned-photos/releases/download/v1.0/multicrop-scanned-photos-1.0.zip) in the releases tab of Github.

Run `multicrop-scanned-photos-example.bat` to extract the 4 photos from `example-in` directory to the `example-out` directory.

To extract your own photos, simply place them in the `example-in` directory and run `multicrop-scanned-photos-example.bat`. Subdirectories will be recreated in the output directory. Alternatively you can change `multicrop-scanned-photos-example.bat` to use different directories

## Options
Edit `multicrop-scanned-photos.properties` to change options:

| Option&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|Valid&nbsp;values| Default | Description
| ---------------- | -------------- | ------------- | ------------
| `write-debug`    | true/false     | false         | When true, creates a `debug` directory with all the steps in the process. Great when determining threshold
| `write-rects`    | true/false     | false         | When true, creates a `rects` directory with the original scans and the detected photos outlined in green. Great to quickly find any missing/wrongly cropped photos when working with a large amount of scans
| `threshold`      | int 0-255      | 200           | The threshold used to separate the background (white) from the foreground (photos). 200-220 seem to be good values
| `ignore-edge`    | positive int   | 100           | Scanners can leave artifacts around the edges of scans. This is the number of pixels around the edge that are ignored. The default of 100 should be good
| `postcrop`       | positive int   | 25            | Edges won't be perfect. Cut this amount of pixels from the edges of detected photos
| `min-area-ratio` | float 0.0-1.0  | 0.05          | The minimum area of detected photos relative to the entire scan. Increase this if parts of photos are being extracted
| `max-area-ratio` | float 0.0-1.0  | 0.9           | The maximum area of detected photos relative to the entire scan.


## Internal process
1. Crop `ignore-edge` pixels around the edges
2. Add a white border of `ignore-edge` pixels
3. Transform to greyscale
4. Threshold according to `threshold` value
5. Detect contours
6. Detect rects, compensating for the removed border in step 1 and cropping according to `postcrop`
7. Export to separate files

<img src="https://raw.githubusercontent.com/michielproce/multicrop-scanned-photos/master/readme-img/process.gif" width="800">


## Acknowledgments
Inspired by: https://github.com/polm/ndl-crop

Couldn't have done it without: https://opencv.org/

## OpenCV copyright notice
Copyright (C) 2000-2019, Intel Corporation, all rights reserved.
Copyright (C) 2009-2011, Willow Garage Inc., all rights reserved.
Copyright (C) 2009-2016, NVIDIA Corporation, all rights reserved.
Copyright (C) 2010-2013, Advanced Micro Devices, Inc., all rights reserved.
Copyright (C) 2015-2016, OpenCV Foundation, all rights reserved.
Copyright (C) 2015-2016, Itseez Inc., all rights reserved.
Third party copyrights are property of their respective owners.

This software is provided by the copyright holders and contributors “as is” and any express or implied warranties, including, but not limited to, the implied warranties of merchantability and fitness for a particular purpose are disclaimed. In no event shall copyright holders or contributors be liable for any direct, indirect, incidental, special, exemplary, or consequential damages (including, but not limited to, procurement of substitute goods or services; loss of use, data, or profits; or business interruption) however caused and on any theory of liability, whether in contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use of this software, even if advised of the possibility of such damage.
