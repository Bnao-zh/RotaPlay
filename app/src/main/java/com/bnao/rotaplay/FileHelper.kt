package com.bnao.rotaplay

import android.content.Context
import android.util.Base64
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import android.webkit.JavascriptInterface

class FileHelper(private val context: Context) {

    private fun getBaseDir(storageType: Int): File? {
        return when (storageType) {
            0 -> context.filesDir // 内部存储: /data/data/<package>/files
            1 -> context.getExternalFilesDir(null) // 外部存储: /Android/data/<package>/files
            else -> null
        }
    }

    // 第一个方法：保存Base64数据到指定目录
    @Suppress("unused")
    @JavascriptInterface
    fun saveBase64ToFile(data: String, name: String, storageType: Int): Boolean {
        try {
            // 获取应用的外部文件根目录
            val baseDir = getBaseDir(storageType) ?: return false

            // 构建完整文件路径
            val file = File(baseDir, name)

            // 确保父目录存在（自动创建多级目录）
            val parentDir = file.parentFile
            if (parentDir != null && !parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    // mkdirs() 可能失败（如权限问题、存储不可用等）
                    return false
                }
            }

            // 解码 Base64 数据
            val decodedBytes = Base64.decode(data, Base64.DEFAULT)

            // 写入文件
            FileOutputStream(file).use { outputStream ->
                outputStream.write(decodedBytes)
            }

            // 所有步骤成功完成
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        } catch (e: IllegalArgumentException) {
            // Base64 字符串无效
            e.printStackTrace()
            return false
        } catch (e: Exception) {
            // 捕获其他潜在异常（如 SecurityException 等）
            e.printStackTrace()
            return false
        }
    }

    // 第二个方法：查找目录下的文件和文件夹
    @Suppress("unused")
    @JavascriptInterface
    fun listFilesInChartDirectory(name: String, storageType: Int): String {
        return try {
            // 获取应用的外部文件根目录
            val baseDir = getBaseDir(storageType) ?: return "[]"

            // 构建目标子目录路径
            val targetDir = File(baseDir, name)

            // 检查目标路径是否存在且是一个目录
            if (!targetDir.exists() || !targetDir.isDirectory) {
                return "[]" // 目录不存在或不是目录，返回空列表
            }

            // 列出该目录下的文件和子目录名（仅一级）
            val files = targetDir.list()

            if (files != null && files.isNotEmpty()) {
                // 返回 JSON 数组格式的字符串
                "[\"${files.joinToString("\", \"")}\"]"
            } else {
                "[]" // 目录为空或 list() 返回 null
            }
        } catch (_: SecurityException) {
            "[]"
        } catch (_: Exception) {
            "[]"
        }
    }



    // 第三个方法：读取文件并返回Data URL字符串
    @Suppress("unused")
    @JavascriptInterface
    fun readFileAsDataUrl(fileName: String, storageType: Int): String {
        try {
            // 获取应用的外部文件根目录（不再硬编码 "chart"）
            val baseDir = getBaseDir(storageType) ?: return ""

            // 构建完整文件路径（自动处理 "chart/1/a.txt" 这类路径）
            val file = File(baseDir, fileName)

            // 安全检查：防止路径遍历攻击（可选但推荐）
            if (!file.canonicalPath.startsWith(baseDir.canonicalPath)) {
                // 文件路径超出应用专属目录，拒绝访问
                return ""
            }

            if (!file.exists() || !file.isFile) {
                return ""
            }

            // 读取文件内容
            val fileBytes = FileInputStream(file).use { inputStream ->
                inputStream.readBytes()
            }

            // 转换为 Base64
            val base64String = Base64.encodeToString(fileBytes, Base64.DEFAULT)

            // 推断 MIME 类型
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
        } catch (e: SecurityException) {
            e.printStackTrace()
            return ""
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }
}
