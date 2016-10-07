package org.thehellnet.shab.mobile.service.location.nmea;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by sardylan on 05/10/16.
 */
public class UtilityTest {

    @Test
    public void testParseLatitude() throws Exception {
        String inputValue = "3913.401092";
        String inputDirection = "N";
        double expected = 39.0;
        double actual = Utility.parseLatitude(inputValue, inputDirection);
        assertEquals(expected, actual, 1.0f);
    }

    @Test
    public void testParseLongitude() throws Exception {
        String inputValue = "00906.085359";
        String inputDirection = "E";
        double expected = 9.0;
        double actual = Utility.parseLongitude(inputValue, inputDirection);
        assertEquals(expected, actual, 1.0f);
    }
}