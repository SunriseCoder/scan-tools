package process.processing.prepare.reordering;

public class Reordering4PagesOn1SheetFromMiddle extends AbstractReorderer {

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
