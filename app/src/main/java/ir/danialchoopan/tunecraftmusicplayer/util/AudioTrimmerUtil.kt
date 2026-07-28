package ir.danialchoopan.tunecraftmusicplayer.util

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer

/**
 * ابزار برش فایل‌های صوتی (Audio Trimmer Utility)
 * 
 * این کلاس برای برش قطعات صوتی و ساخت زنگ تلفن (Ringtone) یا ذخیره قسمتی از آهنگ استفاده می‌شود.
 * برای جلوگیری از کرش در اندروید ۱۰ به بالا (Scoped Storage)، فایل خروجی در دایرکتوری اختصاصی
 * برنامه ذخیره می‌شود و سپس در سیستم ثبت می‌گردد.
 */
object AudioTrimmerUtil {

    /**
     * برش فایل صوتی براساس بازه زمانی داده‌شده
     *
     * @param context کانتکست برنامه
     * @param inputUriOrPath مسیر فایل یا آدرس Content URI
     * @param startMs زمان شروع به میلی‌ثانیه
     * @param endMs زمان پایان به میلی‌ثانیه
     * @param outputFileName نام پیشنهادی فایل خروجی
     * @return Result شامل فایل برش خورده یا خطای ایجاد شده
     */
    suspend fun trimAudio(
        context: Context,
        inputUriOrPath: String,
        startMs: Long,
        endMs: Long,
        outputFileName: String
    ): Result<File> = withContext(Dispatchers.IO) {
        var extractor: MediaExtractor? = null
        var muxer: MediaMuxer? = null

        try {
            extractor = MediaExtractor()

            // بارگذاری منبع صوتی با رعایت Scoped Storage و پشتیبانی از Content URI و مسیر مستقیم
            if (inputUriOrPath.startsWith("content://") || inputUriOrPath.startsWith("file://")) {
                val uri = Uri.parse(inputUriOrPath)
                context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                    extractor.setDataSource(pfd.fileDescriptor)
                } ?: extractor.setDataSource(context, uri, null)
            } else {
                val file = File(inputUriOrPath)
                if (file.exists()) {
                    extractor.setDataSource(file.absolutePath)
                } else {
                    // در صورت عدم دسترسی مستقیم، از طریق Uri تلاش می‌کنیم
                    extractor.setDataSource(context, Uri.parse(inputUriOrPath), null)
                }
            }

            // استخراج تراک صوتی (Audio Track)
            var audioTrackIndex = -1
            var audioFormat: MediaFormat? = null

            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("audio/")) {
                    audioTrackIndex = i
                    audioFormat = format
                    break
                }
            }

            if (audioTrackIndex == -1 || audioFormat == null) {
                return@withContext Result.failure(Exception("هیچ تراک صوتی قابل پردازشی پیدا نشد"))
            }

            extractor.selectTrack(audioTrackIndex)

            // تعیین مسیر ذخیره‌سازی ایمن با پشتیبانی از اندروید ۵ تا ۱۵
            val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
                ?: File(context.filesDir, "TuneCraft_Trims").apply { mkdirs() }

            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }

            val sanitizedName = outputFileName.replace(Regex("[^a-zA-Z0-9_\\-\\u0600-\\u06FF]"), "_")
            val isM4a = inputUriOrPath.lowercase().contains(".m4a") || inputUriOrPath.lowercase().contains(".aac")
            val extension = if (isM4a) ".m4a" else ".mp3"
            val outputFile = File(outputDir, "${sanitizedName}_trimmed$extension")

            // تنظیم MediaMuxer جهت بسته‌بندی فایل خروجی MP4 / M4A
            val outputFormat = MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
            muxer = MediaMuxer(outputFile.absolutePath, outputFormat)
            val muxerTrackIndex = muxer.addTrack(audioFormat)
            muxer.start()

            val startUs = startMs * 1000L
            val endUs = endMs * 1000L

            extractor.seekTo(startUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC)

            // بررسی مقدار بفر جهت جلوگیری از NullPointerException
            val bufferSize = if (audioFormat.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                audioFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE).coerceAtLeast(1024 * 256)
            } else {
                1024 * 1024
            }

            val buffer = ByteBuffer.allocate(bufferSize)
            val bufferInfo = MediaCodec.BufferInfo()

            while (true) {
                bufferInfo.offset = 0
                bufferInfo.size = extractor.readSampleData(buffer, 0)

                if (bufferInfo.size < 0) {
                    break // پایان فایل
                }

                val presentationTimeUs = extractor.sampleTime
                if (presentationTimeUs > endUs) {
                    break // رسیدن به زمان پایان انتخاب‌شده
                }

                bufferInfo.presentationTimeUs = (presentationTimeUs - startUs).coerceAtLeast(0L)
                bufferInfo.flags = extractor.sampleFlags

                muxer.writeSampleData(muxerTrackIndex, buffer, bufferInfo)
                extractor.advance()
            }

            // اسکن فایل خروجی توسط MediaScanner جهت نمایش در گالری و فایل‌های سیستم
            android.media.MediaScannerConnection.scanFile(
                context,
                arrayOf(outputFile.absolutePath),
                null,
                null
            )

            Result.success(outputFile)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        } finally {
            try {
                muxer?.stop()
                muxer?.release()
            } catch (ignored: Exception) {}
            try {
                extractor?.release()
            } catch (ignored: Exception) {}
        }
    }
}

