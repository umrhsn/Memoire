package com.umrhsn.mmoire.models

data class MemoryCard(
    val identifier: Int, // drawable resource
    val imageUrl: String? = null, // user images urls
    val isFaceUp: Boolean = false,
    val isMatched: Boolean = false
)
