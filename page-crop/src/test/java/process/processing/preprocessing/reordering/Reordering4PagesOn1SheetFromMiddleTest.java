package process.processing.preprocessing.reordering;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class Reordering4PagesOn1SheetFromMiddleTest {

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {Arrays.asList(new Integer[]{})},
                {Arrays.asList(new Integer[] {1, 2, 3, 4})},
                {Arrays.asList(new Integer[] {3, 4, 5, 6, 1, 2, 7, 8})},
                {Arrays.asList(new Integer[] {5, 6, 7, 8, 3, 4, 9, 10, 1, 2, 11, 12})},
                {Arrays.asList(new Integer[] {7, 8, 9, 10, 5, 6, 11, 12, 3, 4, 13, 14, 1, 2, 15, 16})}
        });
    }

    private final AbstractReorderer reorderer;
    private final List<Integer> expectedPagesList;

    public Reordering4PagesOn1SheetFromMiddleTest(List<Integer> expectedPagesList) {
        reorderer = new Reordering4PagesOn1SheetFromMiddle();
        this.expectedPagesList = expectedPagesList;
    }

    @Test
    public void testGetReorderedList() {
        int amountOfPages = expectedPagesList.size();
        List<Integer> actualPagesList = reorderer.getReorderedList(amountOfPages);
        assertThat(actualPagesList, is(expectedPagesList));
    }
}
