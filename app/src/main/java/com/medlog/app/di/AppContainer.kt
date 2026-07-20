package com.medlog.app.di

import android.content.Context
import androidx.room.Room
import com.medlog.app.data.local.MedLogDatabase
import com.medlog.app.data.local.dao.*
import com.medlog.app.data.repository.*

class AppContainer(private val context: Context) {

    val database: MedLogDatabase by lazy {
        Room.databaseBuilder(
            context,
            MedLogDatabase::class.java,
            "medlog_database"
        ).build()
    }

    // DAOs
    val profileDao: ProfileDao by lazy { database.profileDao() }
    val conditionDao: ConditionDao by lazy { database.conditionDao() }
    val conditionNoteDao: ConditionNoteDao by lazy { database.conditionNoteDao() }
    val medicationDao: MedicationDao by lazy { database.medicationDao() }
    val medicationLogDao: MedicationLogDao by lazy { database.medicationLogDao() }
    val medicationChangeDao: MedicationChangeDao by lazy { database.medicationChangeDao() }
    val appointmentDao: AppointmentDao by lazy { database.appointmentDao() }
    val fileAttachmentDao: FileAttachmentDao by lazy { database.fileAttachmentDao() }
    val sectionDao: SectionDao by lazy { database.sectionDao() }
    val sectionEntryDao: SectionEntryDao by lazy { database.sectionEntryDao() }
    val clutterItemDao: ClutterItemDao by lazy { database.clutterItemDao() }
    val journalEntryDao: JournalEntryDao by lazy { database.journalEntryDao() }
    val appSettingDao: AppSettingDao by lazy { database.appSettingDao() }

    // Repositories
    val profileRepository: ProfileRepository by lazy { ProfileRepositoryImpl(profileDao) }
    val conditionRepository: ConditionRepository by lazy { ConditionRepositoryImpl(conditionDao, conditionNoteDao) }
    val medicationRepository: MedicationRepository by lazy { MedicationRepositoryImpl(medicationDao, medicationLogDao, medicationChangeDao) }
    val appointmentRepository: AppointmentRepository by lazy { AppointmentRepositoryImpl(appointmentDao) }
    val fileAttachmentRepository: FileAttachmentRepository by lazy { FileAttachmentRepositoryImpl(fileAttachmentDao, context) }
    val sectionRepository: SectionRepository by lazy { SectionRepositoryImpl(sectionDao, sectionEntryDao) }
    val clutterRepository: ClutterRepository by lazy { ClutterRepositoryImpl(clutterItemDao) }
    val journalRepository: JournalRepository by lazy { JournalRepositoryImpl(journalEntryDao) }
    val settingsRepository: SettingsRepository by lazy { SettingsRepositoryImpl(appSettingDao) }
    val searchRepository: SearchRepository by lazy { SearchRepositoryImpl(medicationDao, conditionDao, appointmentDao, journalEntryDao, clutterItemDao) }
}
