package process;

import process.utils.ImageHelper;

public class CombineImagesApp {

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            printUsage();
            System.exit(-1);
        }

        String inputFileName1 = args[0];
        String inputFileName2 = args[1];
        String outputFileName = args[2];

        long startTime = System.currentTimeMillis();

        ImageHelper.combineImageFiles(".", inputFileName1, inputFileName2, outputFileName);

        System.out.println();
        System.out.println("Done, took: " + (System.currentTimeMillis() - startTime) + " ms.");
    }

    private static void printUsage() {
        System.out.println("Usage: " + CombineImagesApp.class.getSimpleName() + " <input file 1> <input file 2> <output file>");
    }
}
