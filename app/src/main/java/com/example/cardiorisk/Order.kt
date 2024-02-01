package com.example.cardiorisk

data class Order(
    val id: String = "",
    val status: String = "",
    val age: String = "",
    val gender: String = "",
    val hdlCholesterol: String = "",
    val totalCholesterol: String = "",
    val systolicBloodPressure: String = "",
    val smoker: String = "",
    val diabetes: String = ""
)