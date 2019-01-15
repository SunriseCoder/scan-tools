package processing.reorder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ScanPagesReorderTest {

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

    private final ScanPagesReorder scanPagesReorder;
    private final List<Integer> expectedPagesList;

    public ScanPagesReorderTest(List<Integer> expectedPagesList) {
        this.expectedPagesList = expectedPagesList;
        scanPagesReorder = new ScanPagesReorder();
    }

    @Test
    public void getReorderedList() {
        int amountOfPages = expectedPagesList.size();
        List<Integer> actualPagesList = scanPagesReorder.getReorderedList(amountOfPages);
        assertThat(actualPagesList, is(expectedPagesList));
    }
}
