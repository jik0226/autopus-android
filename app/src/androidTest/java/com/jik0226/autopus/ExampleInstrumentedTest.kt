package com.jik0226.autopus

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun packageName_isCorrect() {
        assertEquals("com.jik0226.autopus", androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext.packageName)
    }
}
