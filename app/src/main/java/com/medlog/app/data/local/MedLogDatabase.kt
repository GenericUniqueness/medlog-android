package com.medlog.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.medlog.app.data.local.converter.Converters
import com.medlog.app.data.local.dao.*
import com.medlog.app.data.local.entity.*

@Database(
    entities = [
        ProfileEntity::class,
        ConditionEntity::class,
        ConditionNoteEntity::class,
        MedicationEntity::class,
        MedicationLogEntity::class,
        MedicationChangeEntity::class,
        AppointmentEntity::class,
        FileAttachmentEntity::class,
        SectionEntity::class,
        SectionEntryEntity::class,
        ClutterItemEntity::class,
        JournalEntryEntity::class,
        AppSettingEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class MedLogDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun conditionDao(): ConditionDao
    abstract fun conditionNoteDao(): ConditionNoteDao
    abstract fun medicationDao(): MedicationDao
    abstract fun medicationLogDao(): MedicationLogDao
    abstract fun medicationChangeDao(): MedicationChangeDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun fileAttachmentDao(): FileAttachmentDao
    abstract fun sectionDao(): SectionDao
    abstract fun sectionEntryDao(): SectionEntryDao
    abstract fun clutterItemDao(): ClutterItemDao
    abstract fun journalEntryDao(): JournalEntryDao
    abstract fun appSettingDao(): AppSettingDao
}
