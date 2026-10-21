package com.king_grey.retro_snake.sound

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.sin

enum class Tone { EAT, CRASH, CLICK, FANFARE }

/** Square-wave blips generated in code, so the app ships with no audio files. */
class Sfx {
    private val rate = 22050
    private val tracks = HashMap<Tone, AudioTrack?>()

    private fun wave(notes: List<Pair<Double, Int>>, volume: Double): ShortArray {
        val out = ArrayList<Short>()
        for ((freq, ms) in notes) {
            val n = rate * ms / 1000
            for (i in 0 until n) {
                val env = 1.0 - i.toDouble() / n
                val sq = if (sin(2 * PI * freq * i / rate) >= 0) 1.0 else -1.0
                out.add((sq * env * volume * Short.MAX_VALUE).toInt().toShort())
            }
        }
        return out.toShortArray()
    }

    private fun build(t: Tone): AudioTrack? = try {
        val data = when (t) {
            Tone.EAT -> wave(listOf(660.0 to 40, 990.0 to 60), 0.25)
            Tone.CRASH -> wave(listOf(220.0 to 90, 165.0 to 90, 110.0 to 180), 0.3)
            Tone.CLICK -> wave(listOf(520.0 to 30), 0.2)
            Tone.FANFARE -> wave(listOf(523.0 to 90, 659.0 to 90, 784.0 to 90, 1047.0 to 200), 0.25)
        }
        val track = AudioTrack(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
            AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(rate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build(),
            data.size * 2,
            AudioTrack.MODE_STATIC,
            AudioManager.AUDIO_SESSION_ID_GENERATE,
        )
        track.write(data, 0, data.size)
        track
    } catch (e: Exception) {
        null
    }

    fun play(t: Tone) {
        try {
            val track = tracks.getOrPut(t) { build(t) } ?: return
            track.stop()
            track.reloadStaticData()
            track.play()
        } catch (e: Exception) {
            // Sound is optional; never crash the game over it.
        }
    }

    fun release() {
        tracks.values.forEach { try { it?.release() } catch (e: Exception) {} }
        tracks.clear()
    }
}
