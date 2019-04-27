package process.processing.preprocessing.reordering;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import process.processing.prepare.reordering.AbstractReorderer;
import process.processing.prepare.reordering.Reordering4PagesOn1SheetFromMiddle;

@RunWith(Parameterized.class)
public class Reordering4PagesOn1SheetFromMiddleTest {

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {Arrays.asList(new Integer[]{})},
                {Arrays.asList(new Integer[] {1, 2, 3, 4})},
                {Arrays.asList(new Integer[] {5, 6, 1, 2, 3, 4, 7, 8})},
                {Arrays.asList(new Integer[] {9, 10, 5, 6, 1, 2, 3, 4, 7, 8, 11, 12})},
                {Arrays.asList(new Integer[] {13, 14, 9, 10, 5, 6, 1, 2, 3, 4, 7, 8, 11, 12, 15, 16})}
        });
    }

    private final AbstractReorderer reorderer;
    private final List<Integer> expectedPagesList;

    public Reordering4PagesOn1SheetFromMiddleTest(List<Integer> expectedPagesList) {
        reorderer = new Reordering4PagesOn1SheetFromMiddle();
        this.expectedPagesList = expectedPagesList;
    }

    @Test
    public void testGetReorderedPageNumber() {
        int amountOfPages = expectedPagesList.size();

        for (int i = 0; i < amountOfPages; i++) {
            int expected = expectedPagesList.get(i);
            int actual = reorderer.getReorderedPageNumber(i, amountOfPages) + 1;

            assertEquals(expected, actual);
        }
    }
}
