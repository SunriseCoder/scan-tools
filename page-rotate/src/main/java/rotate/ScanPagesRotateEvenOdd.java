package rotate;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ScanPagesRotateEvenOdd {
    public static void main(String[] args) throws IOException {
        File inputFolder = new File("data/in");
        if (!inputFolder.exists() || !inputFolder.isDirectory()) {
        	System.out.println("Input folder '" + inputFolder.getAbsolutePath() + "' does not exist.");
        	System.exit(-1);
        }

        File outputFolder = new File("data/out");
        if (!outputFolder.exists() || !outputFolder.isDirectory()) {
        	System.out.println("Input folder '" + outputFolder.getAbsolutePath() + "' does not exist.");
        	System.exit(-1);
        }

        String pageNumberToRotateAsString = args.length >= 1 ? args[0] : "1";
        int pageNumberToRotate = Integer.parseInt(pageNumberToRotateAsString);
        String rotationAngleClockwise = args.length >= 2 ? args[1] : "180";
        
        String[] filenames = inputFolder.list();
        int amountOfPages = filenames.length;

        ScanPagesRotator rotator = new ScanPagesRotator();
        File[] files = inputFolder.listFiles();

        System.out.println("Starting file processing");
        for (int i = 0; i < amountOfPages; i++) {
        	File file = files[i];
        	BufferedImage sourceImage = ImageIO.read(file);

        	BufferedImage targetImage;
        	if (isNeedToBeRotated(file.getName(), pageNumberToRotate)) {
        		targetImage = rotator.rotatePage(sourceImage, rotationAngleClockwise);
        	} else {
        		targetImage = sourceImage;
        	}

        	saveFile(outputFolder, file.getName(), targetImage);

        	System.out.println("File " + file.getName() + " is done");
        }
        System.out.println("All files are done");
    }

	private static boolean isNeedToBeRotated(String filename, int pageNumberToRotate) {
		int positionOfLastDot = filename.lastIndexOf(".");
		String lastSymbolBeforeDot = filename.substring(positionOfLastDot - 1, positionOfLastDot);
		int lastDigit = Integer.parseInt(lastSymbolBeforeDot);

		boolean result = lastDigit % 2 == pageNumberToRotate;

		return result;
	}

	private static File saveFile(File outputFolder, String filename, BufferedImage rotatedImage) throws IOException {
		File newFile = new File(outputFolder, filename);

		String format = getFormat(filename);

		ImageIO.write(rotatedImage, format, newFile);
		return newFile;
	}

	private static String getFormat(String filename) {
		int positionOfLastDot = filename.lastIndexOf(".");
		String format = filename.substring(positionOfLastDot + 1);
		return format;
	}
}
