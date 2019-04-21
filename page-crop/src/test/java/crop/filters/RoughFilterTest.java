package crop.filters;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class RoughFilterTest extends BaseImageFilterTest {

    @Before
    public void beforeTest() {
        filter = new RoughFilter();
        filter.setImage(image);
    }

    @Test
    public void testGetColor() {
        int x = 1;
        int y = 1;

        assertEquals(getExpectedPixel(x, y), filter.getRGB(x, y));
    }
}
