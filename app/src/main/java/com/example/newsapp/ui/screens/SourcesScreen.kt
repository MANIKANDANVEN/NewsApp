package com.example.newsapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.newsapp.viewmodel.SourcesViewModel

@Composable
fun SourcesScreen(viewModel: SourcesViewModel = hiltViewModel()) {
    val sources by viewModel.availableSources.collectAsState()
    val selectedIds by viewModel.selectedSourceIds.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(sources) { source ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.onSourceToggled(source.id) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = source.name, style = MaterialTheme.typography.titleMedium)
                    source.description?.let {
                        Text(text = it, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                    }
                }
                Checkbox(
                    checked = selectedIds.contains(source.id),
                    onCheckedChange = { viewModel.onSourceToggled(source.id) }
                )
            }
            HorizontalDivider()
        }
    }
}