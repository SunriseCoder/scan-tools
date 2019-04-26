package process.processing.preprocessing.reordering;

import java.util.ArrayList;
import java.util.List;

public class Reordering4PagesOn1SheetFromMiddle extends AbstractReorderer {

    @Override
    public List<Integer> getReorderedList(int amountOfPages) {
        ArrayList<Integer> reorderedList = new ArrayList<Integer>();

        for (int counterLeft = amountOfPages / 2, counterRight = amountOfPages / 2; counterLeft > 0; counterLeft -= 2, counterRight += 2) {
            reorderedList.add(counterLeft - 1);
            reorderedList.add(counterLeft);
            reorderedList.add(counterRight + 1);
            reorderedList.add(counterRight + 2);
        }

        return reorderedList;
    }

    @Override
    public int getReorderedPageNumber(int index, int amountOfPages) {
        int halfPages = amountOfPages / 2;

        int halfPart = index / halfPages;
        int remainder = index % 2;

        int distanceFromCenter = index < halfPages ? halfPages - index - 1 : index - halfPages;
        distanceFromCenter = distanceFromCenter >> 1 << 1;

        int result = 2 * distanceFromCenter + 2 * halfPart + remainder;

        return result;
    }
}
