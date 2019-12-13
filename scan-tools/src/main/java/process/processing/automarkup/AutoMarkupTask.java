package process.processing.automarkup;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dto.IntPoint;
import dto.Point;
import multithreading.AbstractTask;
import structures.RGB;
import utils.ColorUtils;

public class AutoMarkupTask extends AbstractTask {
    private File sourceFile;
    private int threshold;
    private int areaSize;

    public AutoMarkupTask(String name) {
        super(name);
    }

    @Override
    protected void runWithExceptions() throws Exception {
        BufferedImage image = applicationContext.readImage(sourceFile);
        ImageMap imageMap = new ImageMap(image, threshold);
        ImageAnalyzer analyzer = new ImageAnalyzer(imageMap, areaSize);

        List<Point> selectionBoundaries = new ArrayList<>();
        int height = image.getHeight();
        int width = image.getWidth();
        int minImageSide = Math.min(height, width);

        // Top Left
        outer:
        for (int i = 0; i < minImageSide; i++) {
            for (int x = 0; x <= i; x++) {
                int y = i - x;
                boolean hasConnections = analyzer.findUsefulNeighbors(x, y);
                if (hasConnections) {
                    selectionBoundaries.add(new Point(x, y));
                    break outer;
                }
            }
        }

        // Top Right
        outer:
        for (int i = 0; i < minImageSide; i++) {
            for (int x = width - i - 1; x < width; x++) {
                int y = x + i - width + 1;
                boolean hasConnections = analyzer.findUsefulNeighbors(x, y);
                if (hasConnections) {
                    selectionBoundaries.add(new Point(x, y));
                    break outer;
                }
            }
        }

        // Bottom Right
        outer:
        for (int i = 0; i < minImageSide; i++) {
            for (int x = width - i - 1; x < width; x++) {
                int y = height + width - x - i - 2;
                boolean hasConnections = analyzer.findUsefulNeighbors(x, y);
                if (hasConnections) {
                    selectionBoundaries.add(new Point(x, y));
                    break outer;
                }
            }
        }

        // Bottom Left
        outer:
        for (int i = 0; i < minImageSide; i++) {
            for (int x = 0; x <= i; x++) {
                int y = x + height - i - 1;
                boolean hasConnections = analyzer.findUsefulNeighbors(x, y);
                if (hasConnections) {
                    selectionBoundaries.add(new Point(x, y));
                    break outer;
                }
            }
        }

        if (!selectionBoundaries.isEmpty()) {
            applicationContext.saveSelectionBoundaries(sourceFile.getParentFile(), sourceFile.getName(), selectionBoundaries);
        }
    }

    public void setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public void setAreaSize(int areaSize) {
        this.areaSize = areaSize;
    }

    private static class ImageMap {
        private BufferedImage image;
        private int threshold;

        private Status[][] imageMap;

        public ImageMap(BufferedImage image, int threshold) {
            this.image = image;
            this.threshold = threshold;
            imageMap = new Status[image.getWidth()][image.getHeight()];
        }

        public int getHeight() {
            return image.getHeight();
        }

        public int getWidth() {
            return image.getWidth();
        }

        public boolean isUseful(int x, int y) {
            Status mapValue = imageMap[x][y];
            if (mapValue == null) {
                mapValue = calculatePixelStatus(x, y);
                imageMap[x][y] = mapValue;
            }

            boolean isUseful = mapValue == Status.Useful;
            return isUseful;
        }

        private Status calculatePixelStatus(int x, int y) {
            if (x < 0 || x >= image.getWidth() || y < 0 || y >= image.getHeight()) {
                return null;
            }

            int color = image.getRGB(x, y);
            RGB rgb = ColorUtils.getRGB(color);
            double sum = Math.pow(rgb.r, 2) + Math.pow(rgb.g, 2) + Math.pow(rgb.b, 2);
            Status result = sum >= threshold ? Status.Useful : Status.NotUseful;
            return result;
        }

        private enum Status {
            Useful, NotUseful;
        }
    }

    private static class ImageAnalyzer {
        private ImageMap imageMap;
        private int areaSize;
        private List<IntPoint> queue;
        private boolean[][] checkedMap;

        public ImageAnalyzer(ImageMap imageMap, int areaSize) {
            this.imageMap = imageMap;
            this.areaSize = areaSize;

        }

        public boolean findUsefulNeighbors(int x, int y) {
            boolean isUseful = imageMap.isUseful(x, y);
            if (!isUseful) {
                return false;
            }

            int width = imageMap.getWidth();
            int height = imageMap.getHeight();
            queue = new ArrayList<>();
            checkedMap = new boolean[width][height];

            IntPoint point = new IntPoint(x, y);
            queue.add(point);
            checkedMap[x][y] = true;

            int usefulPointsCounter = 1;
            while (!queue.isEmpty()) {
                point = queue.remove(0);

                IntPoint[] pointsToCheck = new IntPoint[] {
                        new IntPoint(point.x + 1, point.y), new IntPoint(point.x - 1, point.y),
                        new IntPoint(point.x, point.y + 1), new IntPoint(point.x, point.y - 1)
                };

                for (int i = 0; i < pointsToCheck.length; i++) {
                    point = pointsToCheck[i];

                    boolean isOutOfBounds = point.x < 0 || point.x >= width || point.y < 0 || point.y >= height;
                    if (isOutOfBounds) {
                        continue;
                    }

                    boolean isCheckedAlready = checkedMap[point.x][point.y];
                    if (isCheckedAlready) {
                        continue;
                    }
                    checkedMap[point.x][point.y] = true;

                    isUseful = imageMap.isUseful(point.x, point.y);
                    if (isUseful) {
                        queue.add(point);
                        usefulPointsCounter++;
                    }

                    if (usefulPointsCounter >= areaSize) {
                        return true;
                    }
                }
            }

            return false;
        }
    }
}
