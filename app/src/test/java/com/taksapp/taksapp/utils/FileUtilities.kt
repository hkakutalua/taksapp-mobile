package com.taksapp.taksapp.utils

import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader

final class FileUtilities {
    companion object {
        fun getFileContent(filePath: String): String {
            val url = FileUtilities::class.java
                .classLoader
                .getResource(filePath)

            val inputStream = FileInputStream(url.path)
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val stringBuilder = StringBuilder()

            bufferedReader.forEachLine { line -> stringBuilder.append(line) }

            return stringBuilder.toString()
        }
    }
}