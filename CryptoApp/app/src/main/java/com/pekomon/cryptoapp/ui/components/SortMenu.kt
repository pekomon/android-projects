package com.pekomon.cryptoapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.pekomon.cryptoapp.data.SortOption

@Composable
fun SortMenu(
    currentSort: SortOption,
    onSortSelected: (SortOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Sort,
                contentDescription = "Sort options"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            listOf(
                SortOption.PRICE_DESC,
                SortOption.PRICE_ASC,
                SortOption.NAME_ASC,
                SortOption.NAME_DESC,
                SortOption.SYMBOL_ASC,
                SortOption.SYMBOL_DESC
            ).forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.displayName) },
                    onClick = {
                        onSortSelected(option)
                        expanded = false
                    },
                    leadingIcon = if (currentSort == option) {
                        { Icon(Icons.Default.Check, null) }
                    } else null
                )
            }
        }
    }
} 