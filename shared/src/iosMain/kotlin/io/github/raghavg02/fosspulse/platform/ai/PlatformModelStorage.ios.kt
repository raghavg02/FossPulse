package io.github.raghavg02.fosspulse.platform.ai

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileHandle
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.closeFile
import platform.Foundation.create
import platform.Foundation.fileHandleForWritingAtPath
import platform.Foundation.seekToEndOfFile
import platform.Foundation.writeData

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual class PlatformModelStorage actual constructor() {

    private val modelsDir: String
        get() {
            val paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
            val docDir = paths.firstOrNull() as? String ?: "."
            val modelsPath = "$docDir/models"
            val fm = NSFileManager.defaultManager
            if (!fm.fileExistsAtPath(modelsPath)) {
                fm.createDirectoryAtPath(modelsPath, withIntermediateDirectories = true, attributes = null, error = null)
            }
            return modelsPath
        }

    actual fun getModelFileSizeBytes(fileName: String): Long {
        val path = "$modelsDir/$fileName"
        val fm = NSFileManager.defaultManager
        if (!fm.fileExistsAtPath(path)) return 0L
        val attrs = fm.attributesOfItemAtPath(path, error = null) ?: return 0L
        val size = attrs["NSFileSize"] as? Long ?: 0L
        return size
    }

    actual fun isModelFilePresent(fileName: String): Boolean {
        val path = "$modelsDir/$fileName"
        return NSFileManager.defaultManager.fileExistsAtPath(path) && getModelFileSizeBytes(fileName) > 0L
    }

    actual fun deleteModelFile(fileName: String): Boolean {
        val path = "$modelsDir/$fileName"
        val fm = NSFileManager.defaultManager
        return if (fm.fileExistsAtPath(path)) {
            fm.removeItemAtPath(path, error = null)
        } else {
            false
        }
    }

    actual fun getModelFilePath(fileName: String): String {
        return "$modelsDir/$fileName"
    }

    actual suspend fun writeChunk(fileName: String, bytes: ByteArray, append: Boolean) = withContext(Dispatchers.Default) {
        val path = "$modelsDir/$fileName"
        val fm = NSFileManager.defaultManager
        if (!append || !fm.fileExistsAtPath(path)) {
            fm.createFileAtPath(path, contents = null, attributes = null)
        }
        val handle = NSFileHandle.fileHandleForWritingAtPath(path) ?: return@withContext
        try {
            if (append) {
                handle.seekToEndOfFile()
            }
            val nsData: NSData = bytes.usePinned { pinned ->
                NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
            }
            handle.writeData(nsData)
        } finally {
            handle.closeFile()
        }
    }
}
