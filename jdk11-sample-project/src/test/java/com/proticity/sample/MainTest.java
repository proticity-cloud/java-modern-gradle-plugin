package com.proticity.sample;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * A main class test.
 */
public class MainTest {
    /**
     * The test of the main class.
     */
    @Test
    public void testMain() {
        Assertions.assertEquals(0, Main.main(new String[]{}));
    }
}
