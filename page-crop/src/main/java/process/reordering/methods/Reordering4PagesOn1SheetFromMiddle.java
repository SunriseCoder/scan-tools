package process.reordering.methods;

import java.util.ArrayList;
import java.util.List;

public class Reordering4PagesOn1SheetFromMiddle extends AbstractReorderingTask {

    protected List<Integer> getReorderedList(int amountOfPages) {
        ArrayList<Integer> reorderedList = new ArrayList<Integer>();

        for (int counterLeft = amountOfPages / 2, counterRight = amountOfPages / 2; counterLeft > 0; counterLeft -= 2, counterRight += 2) {
            reorderedList.add(counterLeft - 1);
            reorderedList.add(counterLeft);
            reorderedList.add(counterRight + 1);
            reorderedList.add(counterRight + 2);
        }

        return reorderedList;
    }
}
