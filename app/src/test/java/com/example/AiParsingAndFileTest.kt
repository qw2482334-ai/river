package com.example

import com.example.data.AiConfigManager
import com.example.data.GeminiService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class AiParsingAndFileTest {

    @Test
    fun testVoiceAndTextParsing_taxiExpense() = runBlocking {
        val mockConfigManager = AiConfigManager(null)
        val geminiService = GeminiService(mockConfigManager)

        val result = geminiService.parseExpenseFromText("打车去机场花了128元")
        assertTrue(result.isSuccess)
        val parsed = result.getOrNull()
        assertNotNull(parsed)
        assertEquals(128.0, parsed!!.amount, 0.01)
        assertEquals("EXPENSE", parsed.type)
        assertEquals("交通", parsed.category)
    }

    @Test
    fun testVoiceAndTextParsing_incomeDraftFee() = runBlocking {
        val mockConfigManager = AiConfigManager(null)
        val geminiService = GeminiService(mockConfigManager)

        val result = geminiService.parseExpenseFromText("收到本月项目稿费1800元")
        assertTrue(result.isSuccess)
        val parsed = result.getOrNull()
        assertNotNull(parsed)
        assertEquals(1800.0, parsed!!.amount, 0.01)
        assertEquals("INCOME", parsed.type)
    }

    @Test
    fun testDocumentFileParsing_csvOrTextBill() = runBlocking {
        val mockConfigManager = AiConfigManager(null)
        val geminiService = GeminiService(mockConfigManager)

        // Simulate file content from a TXT or CSV bill document file
        val fileContent = "超市买水果牛奶花了86元"
        val bytes = fileContent.toByteArray(Charsets.UTF_8)
        val extractedText = String(bytes, Charsets.UTF_8).trim()

        assertEquals("超市买水果牛奶花了86元", extractedText)

        val result = geminiService.parseExpenseFromText(extractedText)
        assertTrue(result.isSuccess)
        val parsed = result.getOrNull()
        assertNotNull(parsed)
        assertEquals(86.0, parsed!!.amount, 0.01)
        assertEquals("EXPENSE", parsed.type)
        assertEquals("购物", parsed.category)
    }

    @Test
    fun testFinancialAdviceGeneration() = runBlocking {
        val mockConfigManager = AiConfigManager(null)
        val geminiService = GeminiService(mockConfigManager)

        val result = geminiService.generateFinancialAdvice(
            userQuestion = "如何才能更快存够5万元？",
            totalIncome = 8000.0,
            totalExpense = 3500.0,
            categoryBreakdown = "餐饮: ￥1200, 居住: ￥1500, 交通: ￥800",
            recentExpenses = "打车去机场(￥128)"
        )

        assertTrue(result.isSuccess)
        val advice = result.getOrNull()
        assertNotNull(advice)
        assertTrue(advice!!.contains("5万") || advice.contains("攒钱"))
    }
}
