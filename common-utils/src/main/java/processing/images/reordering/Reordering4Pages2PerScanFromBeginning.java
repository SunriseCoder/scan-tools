package processing.images.reordering;

public class Reordering4Pages2PerScanFromBeginning extends AbstractReorderer {

    @Override
    public int getReorderedPageNumber(int index, int amountOfPages) {
        /*
         *  Scan    Book
         *  2  3    1  2
         *  1  0    0  3
         *

         *
         *  6  7    3  4
         *  5  4    2  5
         *
         *  2  3    1  6
         *  1  0    0  7
         *

         *
         * 10 11    5  6
         *  9  8    4  7
         *
         *  6  7    3  8
         *  5  4    2  9
         *
         *  2  3    1 10
         *  1  0    0 11
         */

        int numberOfHalf = 2 * index / amountOfPages; // 0 or 1

        int result;
        if (numberOfHalf == 0) {
            result = 4 * (index / 2) + index % 2 + 1;
        } else {
            int reverseIndex = amountOfPages - index - 1;
            result = 4 * (reverseIndex / 2) + 3 * (reverseIndex % 2);
        }

        return result;
    }
}
