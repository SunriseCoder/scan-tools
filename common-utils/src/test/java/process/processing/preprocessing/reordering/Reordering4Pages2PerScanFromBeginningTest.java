package process.processing.preprocessing.reordering;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import processing.images.reordering.AbstractReorderer;
import processing.images.reordering.Reordering4Pages2PerScanFromBeginning;

@RunWith(Parameterized.class)
public class Reordering4Pages2PerScanFromBeginningTest {

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {Arrays.asList(new Integer[]{})},
                {Arrays.asList(new Integer[] {2, 3, 4, 1})},
                {Arrays.asList(new Integer[] {2, 3, 6, 7, 8, 5, 4, 1})},
                {Arrays.asList(new Integer[] {2, 3, 6, 7, 10, 11, 12, 9, 8, 5, 4, 1})},
                {Arrays.asList(new Integer[] {2, 3, 6, 7, 10, 11, 14, 15, 16, 13, 12, 9, 8, 5, 4, 1})}
        });
    }

    private final AbstractReorderer reorderer;
    private final List<Integer> expectedPagesList;

    public Reordering4Pages2PerScanFromBeginningTest(List<Integer> expectedPagesList) {
        reorderer = new Reordering4Pages2PerScanFromBeginning();
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
