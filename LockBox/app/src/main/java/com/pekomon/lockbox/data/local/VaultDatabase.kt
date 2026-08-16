package com.pekomon.lockbox.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.time.Instant

@Database(
    entities = [
        VaultEntryMetadataEntity::class,
        EncryptedSecretPayloadEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(VaultTypeConverters::class)
abstract class VaultDatabase : RoomDatabase() {
    abstract fun vaultEntryDao(): VaultEntryDao
}

class VaultTypeConverters {
    @TypeConverter
    fun instantToEpochMillis(value: Instant): Long = value.toEpochMilli()

    @TypeConverter
    fun epochMillisToInstant(value: Long): Instant = Instant.ofEpochMilli(value)
}
