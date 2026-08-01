package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExpenseCategory

@Composable
fun SearchBarAndFilter(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    selectedCategoryFilter: String?,
    onCategoryFilterSelect: (String?) -> Unit,
    selectedTypeFilter: String?, // null, "EXPENSE", "INCOME"
    onTypeFilterSelect: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("search_bar_and_filter")
    ) {
        // Search Input Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            placeholder = { Text("搜索账单、商户或备注...") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "清除搜索")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Category Filter Chips Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategoryFilter == null && selectedTypeFilter == null,
                    onClick = {
                        onCategoryFilterSelect(null)
                        onTypeFilterSelect(null)
                    },
                    label = { Text("全部", fontSize = 12.sp) }
                )
            }

            item {
                FilterChip(
                    selected = selectedTypeFilter == "EXPENSE",
                    onClick = {
                        onTypeFilterSelect(if (selectedTypeFilter == "EXPENSE") null else "EXPENSE")
                    },
                    label = { Text("仅支出 ↗", fontSize = 12.sp) }
                )
            }

            item {
                FilterChip(
                    selected = selectedTypeFilter == "INCOME",
                    onClick = {
                        onTypeFilterSelect(if (selectedTypeFilter == "INCOME") null else "INCOME")
                    },
                    label = { Text("仅收入 ↙", fontSize = 12.sp) }
                )
            }

            items(ExpenseCategory.Categories) { cat ->
                val isSelected = selectedCategoryFilter == cat.name
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        onCategoryFilterSelect(if (isSelected) null else cat.name)
                    },
                    label = { Text(cat.name, fontSize = 12.sp) }
                )
            }
        }
    }
}
