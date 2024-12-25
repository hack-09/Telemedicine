package com.example.telemedicine;

<<<<<<< HEAD
import static org.testng.Assert.assertEquals;

=======
>>>>>>> b3c56b5b1afab76afaefd452be7977574aa25928
import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

<<<<<<< HEAD
import org.testng.annotations.Test;

=======
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;
>>>>>>> b3c56b5b1afab76afaefd452be7977574aa25928

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
<<<<<<< HEAD
//@RunWith(AndroidJUnit4.class)
//public class ExampleInstrumentedTest {
//    @Test
//    public void useAppContext() {
//        // Context of the app under test.
//        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
//        assertEquals("com.example.telemedicine", appContext.getPackageName());
//    }
//
//}
=======
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.telemedicine", appContext.getPackageName());
    }
}
>>>>>>> b3c56b5b1afab76afaefd452be7977574aa25928
