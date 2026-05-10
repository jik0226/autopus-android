package app.gadi

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun packageName_isCorrect() {
        assertEquals("app.gadi", androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext.packageName)
    }
}
