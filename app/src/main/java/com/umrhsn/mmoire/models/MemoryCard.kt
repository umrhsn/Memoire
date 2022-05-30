package com.umrhsn.mmoire.models

data class MemoryCard(
    val identifier: Int, // drawable resource
    val imageUrl: String? = null, // user images urls
    var isFaceUp: Boolean = false,
    var isMatched: Boolean = false
)
