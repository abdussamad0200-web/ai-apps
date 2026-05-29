package com.example.viewmodel

import android.graphics.Bitmap
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.HistoryLog
import com.example.data.model.TelegramChannel
import com.example.data.model.UserProfile
import com.example.data.repository.DashboardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.URLEncoder

class DashboardViewModel(private val repository: DashboardRepository) : ViewModel() {

    // --- Navigation & Unified UI ---
    private val _currentTab = MutableStateFlow(0) // 0: Dashboard, 1: Telegram, 2: AI Hub, 3: Creative, 4: Profile
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    fun selectTab(tab: Int) {
        _currentTab.value = tab
    }

    // --- Local DB Flows ---
    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val telegramChannels: StateFlow<List<TelegramChannel>> = repository.allTelegramChannels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val historyLogs: StateFlow<List<HistoryLog>> = repository.allHistoryLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Provision default profile if empty
        viewModelScope.launch {
            repository.userProfile.first()?.let {
                Log.d("DashboardViewModel", "Profile loaded: ${it.name}")
            } ?: run {
                Log.d("DashboardViewModel", "Profile is empty. Provisioning default profile.")
                repository.updateProfile(UserProfile())
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // --- Profile Operations ---
    fun updateProfile(name: String, bio: String, status: String, avatarId: String) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            repository.updateProfile(
                current.copy(
                    name = name,
                    bio = bio,
                    status = status,
                    avatarIdentifier = avatarId,
                    lastUpdated = System.currentTimeMillis()
                )
            )
        }
    }

    // --- Telegram CRUD ---
    fun addTelegramChannel(name: String, identifier: String, botToken: String) {
        viewModelScope.launch {
            repository.addTelegramChannel(
                TelegramChannel(
                    name = name,
                    identifier = identifier,
                    botToken = botToken
                )
            )
            repository.addHistoryLog(
                "Telegram Manager",
                "Added Telegram Channel",
                "Channel: $name ($identifier) registered successfully."
            )
        }
    }

    fun removeTelegramChannel(channel: TelegramChannel) {
        viewModelScope.launch {
            repository.deleteTelegramChannel(channel)
            repository.addHistoryLog(
                "Telegram Manager",
                "Removed Telegram Channel",
                "Channel: ${channel.name} was removed."
            )
        }
    }

    // --- Telegram Broadcast Engine ---
    private val _telegramPostState = MutableStateFlow<String?>(null)
    val telegramPostState: StateFlow<String?> = _telegramPostState.asStateFlow()

    private val _isPosting = MutableStateFlow(false)
    val isPosting: StateFlow<Boolean> = _isPosting.asStateFlow()

    fun clearPostStatus() {
        _telegramPostState.value = null
    }

    fun broadcastToTelegram(channel: TelegramChannel, text: String, photoUrl: String?) {
        viewModelScope.launch {
            _isPosting.value = true
            _telegramPostState.value = "Broadcasting..."

            val success = if (!photoUrl.isNullOrEmpty()) {
                repository.sendTelegramPhoto(
                    token = channel.botToken,
                    chatIdentifier = channel.identifier,
                    photoUrl = photoUrl,
                    caption = text
                )
            } else {
                repository.sendTelegramMessage(
                    token = channel.botToken,
                    chatIdentifier = channel.identifier,
                    text = text
                )
            }

            if (success) {
                _telegramPostState.value = "Successfully posted to ${channel.name}!"
                repository.addHistoryLog(
                    "Telegram Broadcast",
                    "Post to Custom Channel: ${channel.name}",
                    "Sent message: \"$text\" with image: ${photoUrl ?: "None"}"
                )
            } else {
                _telegramPostState.value = "Failed to post. Check your Bot Token, Channel ID, Admin permissions, and connection."
            }
            _isPosting.value = false
        }
    }

    // --- AI Translator ---
    private val _translatorInput = MutableStateFlow("")
    val translatorInput: StateFlow<String> = _translatorInput.asStateFlow()

    private val _translatorTargetLang = MutableStateFlow("English")
    val translatorTargetLang: StateFlow<String> = _translatorTargetLang.asStateFlow()

    private val _translatorOutput = MutableStateFlow("")
    val translatorOutput: StateFlow<String> = _translatorOutput.asStateFlow()

    private val _isTranslating = MutableStateFlow(false)
    val isTranslating: StateFlow<Boolean> = _isTranslating.asStateFlow()

    fun updateTranslatorInput(text: String) {
        _translatorInput.value = text
    }

    fun selectTranslatorTarget(lang: String) {
        _translatorTargetLang.value = lang
    }

    fun performTranslation() {
        val input = _translatorInput.value.trim()
        val target = _translatorTargetLang.value
        if (input.isEmpty()) return

        viewModelScope.launch {
            _isTranslating.value = true
            _translatorOutput.value = "Translating text securely..."

            val prompt = "Translate the following text into $target. Provide only the clean translation in the response. Text: \"$input\""
            val result = repository.generateWithGemini(
                prompt = prompt,
                systemInstruction = "You are an expert multilingual translator agent. Translate input text into the target language with high semantic accuracy and clean output."
            )

            _translatorOutput.value = result
            repository.addHistoryLog("AI Translator", "Translated to $target", "Input: $input\nOutput: $result")
            _isTranslating.value = false
        }
    }

    // --- Social Metadata Gen ---
    private val _socialPlatform = MutableStateFlow("YouTube") // YouTube, Facebook, TikTok
    val socialPlatform: StateFlow<String> = _socialPlatform.asStateFlow()

    private val _socialTopic = MutableStateFlow("")
    val socialTopic: StateFlow<String> = _socialTopic.asStateFlow()

    private val _socialTone = MutableStateFlow("Catchy & Viral")
    val socialTone: StateFlow<String> = _socialTone.asStateFlow()

    private val _socialGeneratedTitle = MutableStateFlow("")
    val socialGeneratedTitle: StateFlow<String> = _socialGeneratedTitle.asStateFlow()

    private val _socialGeneratedDesc = MutableStateFlow("")
    val socialGeneratedDesc: StateFlow<String> = _socialGeneratedDesc.asStateFlow()

    private val _isGeneratingSocial = MutableStateFlow(false)
    val isGeneratingSocial: StateFlow<Boolean> = _isGeneratingSocial.asStateFlow()

    fun setSocialPlatform(platform: String) {
        _socialPlatform.value = platform
    }

    fun setSocialTopic(topic: String) {
        _socialTopic.value = topic
    }

    fun setSocialTone(tone: String) {
        _socialTone.value = tone
    }

    fun generateSocialContents() {
        val platform = _socialPlatform.value
        val topic = _socialTopic.value.trim()
        val tone = _socialTone.value
        if (topic.isEmpty()) return

        viewModelScope.launch {
            _isGeneratingSocial.value = true
            _socialGeneratedTitle.value = "Generating headline..."
            _socialGeneratedDesc.value = "Generating description and tags..."

            val prompt = "Generate highly optimized video details for $platform. Topic: \"$topic\", Style/Tone: \"$tone\". Return in this format:\n\nTITLE: [Write a killer, high-CTR title]\n\nDESCRIPTION:\n[Write a rich, detailed description with hooks, template timelines, and call to actions]\n\nHASHTAGS:\n[List 10 relevant popular trending tags starting with #]"
            val response = repository.generateWithGemini(
                prompt = prompt,
                systemInstruction = "You are a World-Class YouTuber and TikTok Viral Marketing consultant. Generate high-engagement titles, captions, descriptions and hash-tags for social media."
            )

            // Parse TITLE and DESCRIPTION
            var parsedTitle = "AI Generated Social Post"
            var parsedDesc = response

            if (response.contains("TITLE:") && response.contains("DESCRIPTION:")) {
                val titleIndex = response.indexOf("TITLE:") + "TITLE:".length
                val descIndex = response.indexOf("DESCRIPTION:")
                parsedTitle = response.substring(titleIndex, descIndex).trim()
                parsedDesc = response.substring(descIndex + "DESCRIPTION:".length).trim()
            }

            _socialGeneratedTitle.value = parsedTitle
            _socialGeneratedDesc.value = parsedDesc
            repository.addHistoryLog("Social Post Creator", "Platform: $platform", "Topic: $topic\nTitle: $parsedTitle")
            _isGeneratingSocial.value = false
        }
    }

    // --- Image to Prompt (Vision Analysis) ---
    private val _visionBitmap = MutableStateFlow<Bitmap?>(null)
    val visionBitmap: StateFlow<Bitmap?> = _visionBitmap.asStateFlow()

    private val _visionGeneratedPrompt = MutableStateFlow("")
    val visionGeneratedPrompt: StateFlow<String> = _visionGeneratedPrompt.asStateFlow()

    private val _isAnalyzingVision = MutableStateFlow(false)
    val isAnalyzingVision: StateFlow<Boolean> = _isAnalyzingVision.asStateFlow()

    fun loadVisionImage(bitmap: Bitmap) {
        _visionBitmap.value = bitmap
        _visionGeneratedPrompt.value = ""
    }

    fun generatePromptFromImage() {
        val bitmap = _visionBitmap.value ?: return
        viewModelScope.launch {
            _isAnalyzingVision.value = true
            _visionGeneratedPrompt.value = "Analyzing artistic contents... (this may take 15 seconds)"

            // Convert Bitmap to Base64
            val base64 = withContext(Dispatchers.IO) {
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
                Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
            }

            val prompt = "Analyze this image and write a highly detailed, descriptive creative prompt that can be used in stable diffusion or Midjourney to recreate a similar artwork. Focus on style, lighting, mood, objects, and technical details. Keep your response centered on the prompt text."
            val result = repository.generateWithGeminiVision(prompt, base64)

            _visionGeneratedPrompt.value = result
            repository.addHistoryLog("Image to Prompt", "Analyzed Picture", "Generated Prompt: ${result.take(120)}...")
            _isAnalyzingVision.value = false
        }
    }

    // --- Prompt to Image (AI image generator) ---
    private val _imagePromptText = MutableStateFlow("")
    val imagePromptText: StateFlow<String> = _imagePromptText.asStateFlow()

    private val _generatedImageUrl = MutableStateFlow<String?>(null)
    val generatedImageUrl: StateFlow<String?> = _generatedImageUrl.asStateFlow()

    private val _isGeneratingImage = MutableStateFlow(false)
    val isGeneratingImage: StateFlow<Boolean> = _isGeneratingImage.asStateFlow()

    fun updateImagePrompt(text: String) {
        _imagePromptText.value = text
    }

    fun generateImage() {
        val input = _imagePromptText.value.trim()
        if (input.isEmpty()) return

        viewModelScope.launch {
            _isGeneratingImage.value = true
            _generatedImageUrl.value = null

            // Enhance dynamic input prompt using Gemini for spectacular outputs
            val expansionPrompt = "Take this basic image description: \"$input\". Expand it into a beautiful, cinematic 1-sentence prompt emphasizing dynamic lighting, high details, and modern digital art aesthetics."
            val expandedPrompt = repository.generateWithGemini(
                prompt = expansionPrompt,
                systemInstruction = "You are a prompt designer. Generate a single highly detailed artistic sentence. Nothing else."
            )

            val finalPrompt = if (expandedPrompt.startsWith("Error")) input else expandedPrompt
            val encoded = URLEncoder.encode(finalPrompt, "UTF-8")
            
            // Append random query parameters to bypass coil cache and trigger fresh creation
            val timestamp = System.currentTimeMillis()
            val pollinationsUrl = "https://image.pollinations.ai/p/$encoded?width=768&height=768&seed=$timestamp&nologo=true"

            delay(1500) // Simulating rendering time
            _generatedImageUrl.value = pollinationsUrl
            repository.addHistoryLog("Prompt to Image", finalPrompt, "Generated Link: $pollinationsUrl")
            _isGeneratingImage.value = false
        }
    }

    // --- AI Song Maker & Composer ---
    private val _songTopic = MutableStateFlow("")
    val songTopic: StateFlow<String> = _songTopic.asStateFlow()

    private val _songGenre = MutableStateFlow("Lofi Acoustic") // Lofi Acoustic, Synthwave, Cyberbeat, Pop Rock
    val songGenre: StateFlow<String> = _songGenre.asStateFlow()

    private val _songTempo = MutableStateFlow("Medium (100 BPM)")
    val songTempo: StateFlow<String> = _songTempo.asStateFlow()

    private val _songLyricsOutput = MutableStateFlow("")
    val songLyricsOutput: StateFlow<String> = _songLyricsOutput.asStateFlow()

    private val _isComposingSong = MutableStateFlow(false)
    val isComposingSong: StateFlow<Boolean> = _isComposingSong.asStateFlow()

    private val _isSongPlaying = MutableStateFlow(false)
    val isSongPlaying: StateFlow<Boolean> = _isSongPlaying.asStateFlow()

    fun setSongTopic(topic: String) {
        _songTopic.value = topic
    }

    fun setSongGenre(genre: String) {
        _songGenre.value = genre
    }

    fun setSongTempo(tempo: String) {
        _songTempo.value = tempo
    }

    fun composeSong() {
        val topic = _songTopic.value.trim()
        val genre = _songGenre.value
        val tempo = _songTempo.value
        if (topic.isEmpty()) return

        viewModelScope.launch {
            _isComposingSong.value = true
            _songLyricsOutput.value = "AI is writing chords, lyrics, and arrangements..."

            val prompt = "Compose a complete original song about \"$topic\" in the \"$genre\" style, running at \"$tempo\" tempo. Provide suggested acoustic guitar/synth chords, structure labels (e.g. Intro, Verse 1, Chorus, Verse 2, Outro), full lyrics, and performance suggestions. Highlight chord progression clearly."
            val response = repository.generateWithGemini(
                prompt = prompt,
                systemInstruction = "You are a professional multi-platinum music artist, lyricist, and instrument composer. Compose highly poetic songs, emotional resonance with robust structures and beautiful chord progressions."
            )

            _songLyricsOutput.value = response
            repository.addHistoryLog("AI Song Composer", "$genre Track", "Topic: $topic\nTitle: ${genre.take(20)}")
            _isComposingSong.value = false
        }
    }

    // --- Procedural Synth Audio Generator ---
    private var synthJob: Job? = null

    fun toggleSongAudioPlayback() {
        if (_isSongPlaying.value) {
            stopProceduralSynth()
        } else {
            startProceduralSynth()
        }
    }

    private fun startProceduralSynth() {
        _isSongPlaying.value = true
        val bpm = when {
            _songTempo.value.contains("80") -> 80
            _songTempo.value.contains("130") -> 130
            else -> 100
        }
        val genre = _songGenre.value

        synthJob = viewModelScope.launch(Dispatchers.Default) {
            val sampleRate = 22050
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            try {
                audioTrack.play()
                val notes = when (genre) {
                    "Lofi Acoustic" -> doubleArrayOf(261.63, 329.63, 392.00, 440.00) // C, E, G, A
                    "Synthwave" -> doubleArrayOf(110.00, 130.81, 146.83, 164.81) // A, C, D, E (bass)
                    "Cyberbeat" -> doubleArrayOf(146.83, 174.61, 220.00, 261.63) // D, F, A, C
                    "Pop Rock" -> doubleArrayOf(261.63, 392.00, 440.00, 349.23) // C, G, A, F
                    else -> doubleArrayOf(261.63, 329.63, 392.00, 523.25) // C MAJOR
                }

                val beatMs = (60000 / bpm).toLong()
                var currentNoteIndex = 0

                val shortArraySize = 2048
                val buffer = ShortArray(shortArraySize)
                var phase = 0.0

                while (_isSongPlaying.value) {
                    val targetFreq = notes[currentNoteIndex]
                    // Play note with simple decay envelope and a sub-bass kick transient
                    for (i in 0 until shortArraySize) {
                        val progress = i.toDouble() / shortArraySize
                        val envelope = 1.0 - progress
                        
                        // Main tone wave
                        val sinValue = Math.sin(phase)
                        phase += (2 * Math.PI * targetFreq) / sampleRate
                        if (phase > 2 * Math.PI) {
                            phase -= 2 * Math.PI
                        }

                        // Add a low harmonic sub-bass warmth or beat pulse
                        val subBassHz = targetFreq / 2
                        val subValue = Math.sin(phase / 2)

                        val sample = ((sinValue * 0.4 + subValue * 0.3) * envelope * Short.MAX_VALUE).toInt()
                        buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                    }

                    audioTrack.write(buffer, 0, shortArraySize)
                    
                    // Increment note matching beat steps index
                    currentNoteIndex = (currentNoteIndex + 1) % notes.size
                    delay(beatMs / 2) // Eighth beats
                }
            } catch (e: Exception) {
                Log.e("AudioSynth", "Error: ", e)
            } finally {
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (ex: Exception) {}
            }
        }
    }

    private fun stopProceduralSynth() {
        _isSongPlaying.value = false
        synthJob?.cancel()
        synthJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopProceduralSynth()
    }
}

class DashboardViewModelFactory(private val repository: DashboardRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            return DashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class context")
    }
}
