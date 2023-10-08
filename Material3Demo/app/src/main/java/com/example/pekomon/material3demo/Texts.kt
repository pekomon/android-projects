package com.example.pekomon.material3demo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pekomon.material3demo.ui.theme.Material3DemoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Texts() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
                ,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Note: BasicTextField is not part of M3 - skipping here
        var txt1 by remember { mutableStateOf("") }
        var txt2 by remember { mutableStateOf("") }
        var trailingIconText by remember { mutableStateOf("Click x to clear") }

        var txt4 by remember { mutableStateOf("") }

        // 1 TextField
        TextField(
            value = txt1,
            onValueChange = { txt1 = it },
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.End
            ),
            suffix = {
                Text(text = "kg")
            },
            supportingText = {
                Text(text = "*Required field")
            },
            isError = txt1.isEmpty(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    // Do something creative
                    println("Next clicked")
                }
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2 OutlinedTextField
        OutlinedTextField(
            value = txt4,
            onValueChange = { txt4 = it },
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.End
            ),
            suffix = {
                Text(text = "kg")
            },
            supportingText = {
                Text(text = "*Required field")
            },
            isError = txt4.isEmpty(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    // Do something creative
                    println("Next clicked")
                }
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Divider(modifier = Modifier.padding(8.dp))
        Text(text = "--- Stylings ---")

        // Exactly same parameters wit OutLinedTextField also
        TextField(
            value = "enabled == false",
            onValueChange = {  },
            enabled = false
        )
        TextField(
            value = "readOnly == true",
            onValueChange = {  },
            readOnly = true
        )
        TextField(
            value = "Underline & align end",
            onValueChange = {  },
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.End,
                textDecoration = TextDecoration.Underline
            )
        )
        TextField(
            value = txt2,
            onValueChange = { txt2 = it },
            label = {
                Text(text = "With label")
            }
        )
        TextField(
            value = txt2,
            onValueChange = { txt2 = it },
            placeholder = {
                Text(text = "Placeholder here")
            }
        )
        TextField(
            value = txt2,
            onValueChange = { txt2 = it },
            label = {
                Text(text = "Label & placeholder")
            },
            placeholder = {
                Text(text = "Name (placeholder)")
            }
        )
        TextField(
            value = "With leading icon",
            onValueChange = { },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.ImageSearch,
                    contentDescription = ""
                )
            }
        )
        TextField(
            value = trailingIconText,
            onValueChange = { trailingIconText = it },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "",
                    modifier = Modifier
                        .clickable { trailingIconText = "" }
                )
            }
        )
        TextField(
            value = "50 --With prefix",
            onValueChange = {  },
            prefix = {
                Text(text = "$")
            }
        )
        TextField(
            value = "With suffix --- 75",
            onValueChange = {  },
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.End,
            ),
            suffix = {
                Text(text = "$")
            }
        )
        TextField(
            value = "Supporting text below - also error",
            onValueChange = {  },
            supportingText = {
                Text(text = "*Required field")
            },
            isError = true
        )
        TextField(
            value = "password",
            onValueChange = {  },
            visualTransformation = PasswordVisualTransformation()
        )
        TextField(
            value = "Keyboard -> Next",
            onValueChange = {  },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    // Do something creative
                    println("Next clicked")
                }
            )
        )
    }
}

@Preview
@Composable
fun TextsPreview() {
    Material3DemoTheme {
        Texts()
    }
}