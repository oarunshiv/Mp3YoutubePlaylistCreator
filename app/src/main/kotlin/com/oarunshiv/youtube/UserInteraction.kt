package com.oarunshiv.youtube

fun getUserResponse(printString: String, additionalValidation: (String) -> Boolean = { true }): String {
    print(printString)
    val response = readLine()
    require(!response.isNullOrEmpty()) { "Please enter valid response" }
    require(additionalValidation(response))
    return response
}