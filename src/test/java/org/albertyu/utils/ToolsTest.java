package org.albertyu.utils;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class ToolsTest {

    @Test
    public void getChromeMajorVersion() {
        int version = Tools.getChromeMajorVersion();
        assertEquals(version, 75);
    }
}