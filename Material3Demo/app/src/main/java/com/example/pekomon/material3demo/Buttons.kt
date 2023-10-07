package com.example.pekomon.material3demo

import android.widget.Space
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun Buttons() {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1 'Filled' button
        // Should be used only for "Final Action in the screen"
        Button(
            onClick = { /*TODO*/ }
        ) {
            Text(text = "Confirm")
        }

        // 2 Elevated button (has a shadow)
        // Use when button needs to stand out from the background
        // for example, when having a light colored image as background
        ElevatedButton(
            onClick = { /*TODO*/ }
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "",
                modifier = Modifier.size(18.dp) //Material3 defines this size
            )
            Spacer(modifier = Modifier.width(8.dp)) //Material3 defines also this spacing
            Text(text = "Add to reading list")
        }

        // 3 FilledTonalButton to use when there are several buttons on screen
        // They don't stand out as much as 'regular' (filled) buttons
        FilledTonalButton(
            onClick = { /*TODO*/ }
        ) {
            Text(text = "Open item in browser")
        }

        // 4 OutlinedButton is even less prominent button
        // To be used as 'Back' or for actions that are not so prominent for user
        OutlinedButton(
            onClick = { /*TODO*/ }
        ) {
            Text(text = "Back")
        }

        // 5 TextButton is the least prominent button
        // Use for a clickable thing where user is unlikely to click
        TextButton(
            onClick = { /*TODO*/ }
        ) {
            Text("Learn more")
        }

    }
    
}

@Preview
@Composable
fun ButtonsPreview() {
    Buttons()
}