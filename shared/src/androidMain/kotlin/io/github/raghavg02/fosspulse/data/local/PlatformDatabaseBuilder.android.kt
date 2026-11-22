package io.github.raghavg02.fosspulse.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

var appContext: Context? = null

actual fun getDatabaseBuilder(): RoomDatabase.Builder<FossPulseDatabase> {
    val context = checkNotNull(appContext) {
        "Android Application Context must be initialized via appContext before accessing the database."
    }
    val dbFile = context.getDatabasePath("fosspulse.db")
    return Room.databaseBuilder<FossPulseDatabase>(
        context = context,
        name = dbFile.absolutePath
    )
}
