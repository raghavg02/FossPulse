package io.github.raghavg02.fosspulse.data.local

import androidx.room.RoomDatabase

expect fun getDatabaseBuilder(): RoomDatabase.Builder<FossPulseDatabase>
