package com.example.deuktemsiru_seller

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class CleanArchitectureTest {
    @Test fun `domain and viewmodels are independent of data and views`() {
        val root = File("src/main/java/com/example/deuktemsiru_seller")
        assertTrue(root.resolve("domain/repository").isDirectory)
        assertTrue(root.resolve("presentation/viewmodel").isDirectory)
        val violations = root.walkTopDown().filter { it.extension == "kt" }.flatMap { file ->
            val path = file.relativeTo(root).invariantSeparatorsPath
            file.readLines().mapIndexedNotNull { i, line ->
                val domain = path.startsWith("domain/")
                val vm = path.startsWith("presentation/viewmodel/")
                val data = path.startsWith("data/")
                val reverse = (domain || vm) && line.startsWith("import com.example.deuktemsiru_seller.data.") ||
                    domain && line.startsWith("import com.example.deuktemsiru_seller.presentation.") ||
                    data && line.startsWith("import com.example.deuktemsiru_seller.presentation.")
                val framework = domain && listOf("android.", "androidx.", "retrofit2.", "okhttp3.", "com.google.gson.").any { line.startsWith("import $it") }
                val ui = vm && listOf("android.content.", "android.view.", "android.widget.", "com.example.deuktemsiru_seller.databinding.").any { line.startsWith("import $it") }
                val directApi = path.startsWith("presentation/") && "RetrofitClient.api" in line
                if (reverse || framework || ui || directApi) "$path:${i + 1}: $line" else null
            }
        }.toList()
        assertTrue(violations.joinToString("\n"), violations.isEmpty())
    }
}
