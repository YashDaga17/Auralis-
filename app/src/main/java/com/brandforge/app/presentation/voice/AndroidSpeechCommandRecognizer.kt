package com.brandforge.app.presentation.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

class AndroidSpeechCommandRecognizer(
    context: Context,
    private val onTranscript: (String) -> Unit,
    private val onPartialTranscript: (String) -> Unit,
    private val onError: (String) -> Unit,
) : RecognitionListener {
    private val recognizer = SpeechRecognizer.createSpeechRecognizer(context.applicationContext).apply {
        setRecognitionListener(this@AndroidSpeechCommandRecognizer)
    }

    fun start() {
        if (!SpeechRecognizer.isRecognitionAvailable(recognizerContext)) {
            onError("SpeechRecognizer unavailable on this device")
            return
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "BrandForge voice command")
        }
        recognizer.startListening(intent)
    }

    fun stop() {
        recognizer.stopListening()
    }

    fun release() {
        recognizer.destroy()
    }

    override fun onReadyForSpeech(params: Bundle?) = Unit
    override fun onBeginningOfSpeech() = Unit
    override fun onRmsChanged(rmsdB: Float) = Unit
    override fun onBufferReceived(buffer: ByteArray?) = Unit
    override fun onEndOfSpeech() = Unit
    override fun onEvent(eventType: Int, params: Bundle?) = Unit

    override fun onError(error: Int) {
        onError("Voice command failed: $error")
    }

    override fun onResults(results: Bundle?) {
        val transcript = results
            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.firstOrNull()
            .orEmpty()
        if (transcript.isBlank()) {
            onError("No voice command detected")
        } else {
            onTranscript(transcript)
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        partialResults
            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.firstOrNull()
            ?.takeIf { it.isNotBlank() }
            ?.let(onPartialTranscript)
    }

    private val recognizerContext: Context = context.applicationContext
}
