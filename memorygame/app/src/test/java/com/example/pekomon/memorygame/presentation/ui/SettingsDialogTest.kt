package com.example.pekomon.memorygame.presentation.ui

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SettingsDialogTest {
    @get:Rule
    val composeRule = createComposeRule()

//     @Test
//     fun musicSliderCallsCallbackOnChange() {
//         var volume = -1f
//         composeRule.setContent {
//             SettingsDialog(
//                 initialEffectVolume = 0f,
//                 initialMusicVolume = 0f,
//                 onEffectVolumeChanged = {},
//                 onMusicVolumeChanged = { volume = it },
//                 onDismiss = {}
//             )
//         }
//         composeRule.onNodeWithTag("musicSlider")
//             .performSemanticsAction(SemanticsActions.SetProgress) { it(0.7f) }
//         assertEquals(0.7f, volume, 0.0001f)
//     }
}
