package foo;


import org.junit.Test;

import static org.junit.Assert.*;


/**
 * Unit test for simple App.
 */
public class AppTest {
    @Test
    public void testApp() {
        int i = 0;
        int j = i++;
        System.out.println(j + "," + i + "=" + Integer.MAX_VALUE);
        System.out.println(Integer.parseInt("000123     ".trim()));
        assertTrue(true);
    }
}
