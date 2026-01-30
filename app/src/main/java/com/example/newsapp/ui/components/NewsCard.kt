package com.example.newsapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.newsapp.data.remote.models.ArticleDto

@Composable
fun NewsCard(article: ArticleDto, onSave: () -> Unit, onClick: () -> Unit) {
    Card(modifier = Modifier.padding(8.dp).clickable { onClick() }) {
        Column {
            AsyncImage(model = article.urlToImage, contentDescription = null)
            Text(text = article.title, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
            Text(text = article.author ?: "Unknown", style = MaterialTheme.typography.bodySmall)
            Button(onClick = onSave) { Text("Save") }
        }
    }
}