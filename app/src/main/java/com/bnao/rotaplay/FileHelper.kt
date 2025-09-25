package com.bnao.rotaplay

import android.content.Context
import android.util.Base64
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import android.webkit.JavascriptInterface

class FileHelper(private val context: Context) {

    // 第一个方法：保存Base64数据到指定目录
    @Suppress("unused")
    @JavascriptInterface
    fun saveBase64ToFile(data: String, name: String): Pair<String, String> {
        try {
            // 获取应用目录
            val filesDir = context.getExternalFilesDir("chart") ?: return Pair("", "")
            if (!filesDir.exists()) {
                filesDir.mkdirs()
            }

            // 创建文件
            val file = File(filesDir, name)

            // 解码Base64数据
            val decodedBytes = Base64.decode(data, Base64.DEFAULT)

            // 写入文件
            FileOutputStream(file).use { outputStream ->
                outputStream.write(decodedBytes)
            }

            // 返回Base64和文件名
            return Pair(data, file.name)
        } catch (e: IOException) {
            e.printStackTrace()
            return Pair("", "")
        }
    }

    // 第二个方法：查找目录下的文件和文件夹
    @Suppress("unused")
    @JavascriptInterface
    fun listFilesInChartDirectory(): String {
        return try {
            val filesDir = context.getExternalFilesDir("chart")
            val files = filesDir?.list()

            if (files != null) {
                if (files.isEmpty()) {
                    "[]" // 空列表
                } else {
                    // 返回JSON数组格式的字符串
                    "[\"${files.joinToString("\", \"")}\"]"
                }
            } else {
                "[]" // 目录不存在时返回空列表
            }
        } catch (_: SecurityException) {
            "[]" // 异常时返回空列表
        } catch (_: Exception) {
            "[]" // 异常时返回空列表
        }
    }



    // 第三个方法：读取文件并返回Data URL字符串
    @Suppress("unused")
    @JavascriptInterface
    fun readFileAsDataUrl(fileName: String): String {
        try {
            val filesDir = context.getExternalFilesDir("chart")
            val file = File(filesDir, fileName)

            if (!file.exists()) {
                return ""
            }

            // 读取文件内容
            val fileBytes = FileInputStream(file).use { inputStream ->
                inputStream.readBytes()
            }

            // 转换为Base64
            val base64String = Base64.encodeToString(fileBytes, Base64.DEFAULT)

            // 构建Data URL
            val mimeType = when (file.extension.lowercase()) {
                // 图片类型
                "jpg", "jpeg" -> "image/jpeg"
                "png" -> "image/png"
                "gif" -> "image/gif"
                "bmp" -> "image/bmp"
                "webp" -> "image/webp"

                // 文本类型
                "txt" -> "text/plain"
                "html", "htm" -> "text/html"
                "css" -> "text/css"
                "js" -> "text/javascript"
                "json" -> "application/json"
                "xml" -> "application/xml"
                "csv" -> "text/csv"
                "md" -> "text/markdown"

                // 音频类型
                "mp3" -> "audio/mpeg"
                "wav" -> "audio/wav"
                "ogg" -> "audio/ogg"
                "aac" -> "audio/aac"
                "flac" -> "audio/flac"

                // 压缩文件
                "zip" -> "application/zip"
                "rar" -> "application/x-rar-compressed"
                "7z" -> "application/x-7z-compressed"
                "tar" -> "application/x-tar"
                "gz" -> "application/gzip"

                else -> "application/octet-stream"
            }


            return "data:$mimeType;base64,$base64String"
        } catch (e: IOException) {
            e.printStackTrace()
            return ""
        }
    }
}
