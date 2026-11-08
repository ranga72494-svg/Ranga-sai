package com.example

import com.example.data.ai.JarvisAiEngine
import com.example.data.model.ActionType
import com.example.data.model.LanguageMode
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testJarvisAiEnglishFlashlight() = runBlocking {
    val engine = JarvisAiEngine()
    val result = engine.processQuery("Turn on flashlight", LanguageMode.ENGLISH)
    assertEquals(ActionType.FLASHLIGHT_ON, result.actionType)
    assertTrue("Should address Boss", result.textReply.contains("Boss"))
  }

  @Test
  fun testJarvisAiTeluguFlashlight() = runBlocking {
    val engine = JarvisAiEngine()
    val result = engine.processQuery("లైట్ వెలిగించు", LanguageMode.TELUGU)
    assertEquals(ActionType.FLASHLIGHT_ON, result.actionType)
    assertTrue("Should address బాస్ in Telugu", result.textReply.contains("బాస్"))
  }

  @Test
  fun testJarvisAiVolumeControl() = runBlocking {
    val engine = JarvisAiEngine()
    val result = engine.processQuery("Increase volume", LanguageMode.ENGLISH)
    assertEquals(ActionType.VOLUME_UP, result.actionType)
  }
}
