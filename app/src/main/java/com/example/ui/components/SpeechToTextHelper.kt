package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import java.util.Locale

class SpeechToTextHelper(private val context: Context) {
    private val _isListening = mutableStateOf(false)
    val isListening: State<Boolean> = _isListening

    private val _recognizedText = mutableStateOf("")
    val recognizedText: State<String> = _recognizedText

    private val _errorState = mutableStateOf<String?>(null)
    val errorState: State<String?> = _errorState

    private var speechRecognizer: SpeechRecognizer? = null

    fun startListening(onResult: (String) -> Unit) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _errorState.value = "设备暂不支持系统语音识别"
            return
        }

        stopAndDestroy()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _isListening.value = true
                    _errorState.value = null
                    _recognizedText.value = "正在聆听，请说话..."
                }

                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    _isListening.value = false
                }

                override fun onError(error: Int) {
                    _isListening.value = false
                    val errorMsg = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "未听清，请再试一次"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "无说话声，请重试"
                        SpeechRecognizer.ERROR_AUDIO -> "麦克风录音异常"
                        SpeechRecognizer.ERROR_NETWORK -> "网络连接异常"
                        else -> "语音识别服务暂未响应"
                    }
                    _errorState.value = errorMsg
                }

                override fun onResults(results: Bundle?) {
                    _isListening.value = false
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val text = matches[0]
                        _recognizedText.value = text
                        onResult(text)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        _recognizedText.value = matches[0]
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.CHINESE.toString())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _isListening.value = false
            _errorState.value = "无法启动语音识别: ${e.localizedMessage}"
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        _isListening.value = false
    }

    fun stopAndDestroy() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            // ignore
        }
        speechRecognizer = null
        _isListening.value = false
    }
}
