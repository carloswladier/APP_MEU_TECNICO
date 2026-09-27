package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [User::class, IndicatorRecord::class, UploadLog::class, AppNotification::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun indicatorDao(): IndicatorDao
    abstract fun uploadLogDao(): UploadLogDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "indicadores_tecnicos.db"
                ).fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getDatabase(context)
                            database.userDao().insertUsers(SampleData.getInitialUsers())
                            database.indicatorDao().insertRecords(SampleData.getInitialRecords())
                            SampleData.getInitialNotifications().forEach {
                                database.notificationDao().insertNotification(it)
                            }
                            database.uploadLogDao().insertLog(
                                UploadLog(
                                    fileName = "planilha_indicadores_oficial_2026.xlsx",
                                    uploadedBy = "admin",
                                    recordCount = SampleData.getInitialRecords().size,
                                    status = "PROCESSADO_COM_SUCESSO"
                                )
                            )
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
