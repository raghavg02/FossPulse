package io.github.raghavg02.fosspulse.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

actual fun getDatabaseBuilder(): RoomDatabase.Builder<FossPulseDatabase> {
    val dbDir = File(System.getProperty("user.home"), ".fosspulse").apply {
        if (!exists()) mkdirs()
    }
    val dbFile = File(dbDir, "fosspulse.db")
    return Room.databaseBuilder<FossPulseDatabase>(
        name = dbFile.absolutePath
    )
}
