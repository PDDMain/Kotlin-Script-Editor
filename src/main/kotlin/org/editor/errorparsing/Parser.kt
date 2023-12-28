package org.editor.errorparsing

fun parseErrors(errors: List<List<String>>) : List<ErrorDescription> {
    val listOfErrors = ArrayList<ErrorDescription>()
    for (index in errors.indices) {
        val firstWord = errors[index][0].split(":")
        if (firstWord.size == 4 && firstWord[1].all { it.isDigit() } && firstWord[1].all { it.isDigit()}) {
            listOfErrors.add(ErrorDescription(index, firstWord[1].toInt(), firstWord[2].toInt()))
        }
    }
    return listOfErrors
}