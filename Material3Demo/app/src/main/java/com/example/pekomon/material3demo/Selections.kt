package com.example.pekomon.material3demo

import android.widget.CheckBox
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp

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

    var triState by remember {
        mutableStateOf(ToggleableState.Indeterminate)
    }
    val toggleTriState = {
        triState = when(triState) {
            ToggleableState.Indeterminate -> ToggleableState.On
            ToggleableState.On -> ToggleableState.Off
            else -> ToggleableState.On
        }
        checkBoxes.indices.forEach { index ->
            checkBoxes[index] = checkBoxes[index].copy(
                checked = triState == ToggleableState.On
            )
        }
    }
    Row(
        verticalAlignment = CenterVertically,
        modifier = Modifier
            .clickable {
                toggleTriState()
            }
            .padding(end = 16.dp)
    ) {
        TriStateCheckbox(
            state = triState,
            onClick = toggleTriState
        )
        Text(text = "Select file types")
    }

    checkBoxes.forEachIndexed { index, info ->
        Row(
            verticalAlignment = CenterVertically,
            modifier = Modifier
                .padding(start = 32.dp)
                .clickable {
                    checkBoxes[index] = info.copy(
                        checked = !info.checked
                    )
                }
                .padding(end = 16.dp)

        ) {
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