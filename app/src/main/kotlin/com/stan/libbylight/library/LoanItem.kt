package com.stan.libbylight.library

data class LoanItem(
    val title: String,
    val author: String,
    val loanUrl: String,
    val coverUrl: String = "",
    val dueText: String = "",
    val progressText: String = "",
)
