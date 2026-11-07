package io.github.mangi.eta.agent.voice

import io.github.mangi.eta.data.model.AsrProvider
import io.github.mangi.eta.data.model.SpeechCredentials
import io.github.mangi.eta.data.model.SpeechSettings
import io.github.mangi.eta.data.model.TtsProvider
import io.github.mangi.eta.data.repository.SpeechCredentialsUnavailable
import java.io.IOException
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

internal enum class SpeechErrorCode {
    CONFIGURATION, CREDENTIALS, PERMISSION, AUDIO, NETWORK, TIMEOUT, AUTHENTICATION,
    RATE_LIMITED, SERVER, PROTOCOL, NO_SPEECH, BACKPRESSURE, STORAGE,
}

internal class SpeechFailure(val code: SpeechErrorCode, val userMessage: String) : IOException(code.name)

internal fun Throwable.speechFailure(): SpeechFailure = when (this) {
    is SpeechFailure -> this
    is SpeechCredentialsUnavailable -> SpeechFailure(SpeechErrorCode.CREDENTIALS, "Kredencialet zanore nuk u deshifruan. Shkruaji dhe ruaji përsëri")
    is SecurityException -> SpeechFailure(SpeechErrorCode.PERMISSION, "Lejo Eta-n të përdorë mikrofonin")
    is kotlinx.coroutines.TimeoutCancellationException -> SpeechFailure(SpeechErrorCode.TIMEOUT, "Shërbimi zanor nuk u përgjigj në kohë. Provo përsëri")
    is IOException -> SpeechFailure(SpeechErrorCode.NETWORK, "Nuk mund të lidhem me shërbimin zanor. Kontrollo rrjetin")
    else -> SpeechFailure(SpeechErrorCode.PROTOCOL, "Shërbimi zanor ktheu të dhëna të papërpunueshme")
}

internal fun validateSpeechSettings(settings: SpeechSettings, secrets: SpeechCredentials, synthesis: Boolean) {
    fun required(value: String, name: String) {
        if (value.isBlank()) throw SpeechFailure(SpeechErrorCode.CONFIGURATION, "Konfiguro fillimisht $name")
    }
    if (synthesis) {
        when (settings.tts) {
            TtsProvider.NONE -> throw SpeechFailure(SpeechErrorCode.CONFIGURATION, "Konfiguro fillimisht leximin me zë te Cilësimet")
            TtsProvider.QWEN -> {
                required(secrets.qwenTts, "Çelësi API i leximit zanor Qwen")
                required(settings.qwenVoice, "Zëri")
                speechBaseUrl(settings.qwenTts.endpoint())
            }
            TtsProvider.DOUBAO -> {
                required(secrets.doubaoTts, "Kredencialet e leximit zanor Doubao")
                required(settings.doubaoVoice, "Zëri")
                speechBaseUrl(settings.doubaoTts.baseUrl)
                if (settings.doubaoTts.legacyAuth) required(settings.doubaoTts.appId, "ID e aplikacionit Doubao")
            }
        }
    } else when (settings.asr) {
        AsrProvider.SYSTEM -> Unit
        AsrProvider.DOUBAO -> {
            required(secrets.doubaoAsr, "Kredencialet e njohjes zanore Doubao")
            speechBaseUrl(settings.doubaoAsr.baseUrl)
            if (settings.doubaoAsr.legacyAuth) required(settings.doubaoAsr.appId, "ID e aplikacionit Doubao")
        }
        else -> {
            required(secrets.qwenAsr, "Çelësi API i njohjes zanore Qwen")
            speechBaseUrl(settings.qwenAsr.endpoint())
            if (settings.asr == AsrProvider.QWEN_FILE) {
                required(settings.oss.bucket, "OSS Bucket")
                required(settings.oss.region, "Rajoni OSS")
                required(secrets.ossAccessKeyId, "OSS AccessKey ID")
                required(secrets.ossAccessKeySecret, "OSS AccessKey Secret")
                speechBaseUrl(settings.oss.endpoint)
                if (!Regex("[a-z0-9][a-z0-9-]{1,61}[a-z0-9]").matches(settings.oss.bucket) ||
                    settings.oss.prefix.split('/').any { it == ".." || it == "." }
                ) throw SpeechFailure(SpeechErrorCode.CONFIGURATION, "Kontrollo bucket-in OSS dhe prefiksin e dosjes")
            }
        }
    }
}

internal fun speechBaseUrl(value: String): String {
    val url = value.trim().toHttpUrlOrNull()
    if (url == null || !url.isHttps || url.username.isNotEmpty() || url.password.isNotEmpty() ||
        url.query != null || url.fragment != null
    ) throw SpeechFailure(SpeechErrorCode.CONFIGURATION, "Adresa e shërbimit duhet të jetë HTTPS, pa autentikim dhe parametra kërkese")
    return url.toString().trimEnd('/')
}
