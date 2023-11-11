package com.example.pekomon.material3demo

import android.widget.CheckBox
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier

@Composable
fun Selections() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding()
        ) {
            CheckBoxes()
        }
    }
}

data class ToggleableInfo(
    val checked: Boolean,
    val text: String
)

@Composable
private fun CheckBoxes() {
    val checkBoxes = remember {
        mutableStateListOf(
            ToggleableInfo(
                checked = false,
                text = "Images"
            ),
            ToggleableInfo(
                checked = false,
                text = "Audio"
            ),
            ToggleableInfo(
                checked = false,
                text = "Video"
            )
        )
    }

    checkBoxes.forEachIndexed { index, info ->
        Row(verticalAlignment = CenterVertically) {
            Checkbox(
                checked = info.checked,
                onCheckedChange = {
                    checkBoxes[index] = info.copy(
                        checked = it
                    )
                }
            )
            Text(text = info.text)
        }
    }
}