package com.masonsoft.imsdk.sample;

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class PlainTest {

    @Test
    public void doTest() {
        final long timeS = 1623981374;
        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timeS * 1000L)));
    }

}
