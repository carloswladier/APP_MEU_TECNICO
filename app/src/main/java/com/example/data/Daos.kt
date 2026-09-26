package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY role ASC, name ASC")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE LOWER(login) = LOWER(:login) LIMIT 1")
    suspend fun getUserByLogin(login: String): User?

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE LOWER(login) = LOWER(:login) AND passwordHash = :password LIMIT 1")
    suspend fun authenticate(login: String, password: String): User?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>)

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("UPDATE users SET passwordHash = :newPassword WHERE id = :userId")
    suspend fun updatePassword(userId: Long, newPassword: String)
}

@Dao
interface IndicatorDao {
    @Query("SELECT * FROM indicator_records ORDER BY id DESC")
    fun getAllRecords(): Flow<List<IndicatorRecord>>

    @Query("""
        SELECT * FROM indicator_records 
        WHERE LOWER(tecnicoLogin) = LOWER(:tecnicoLogin) 
        AND indicatorType = :indicatorType
        ORDER BY id DESC
    """)
    fun getRecordsForTechnician(tecnicoLogin: String, indicatorType: String): Flow<List<IndicatorRecord>>

    @Query("""
        SELECT * FROM indicator_records 
        WHERE indicatorType = :indicatorType
        ORDER BY id DESC
    """)
    fun getRecordsForAdmin(indicatorType: String): Flow<List<IndicatorRecord>>

    @Query("SELECT DISTINCT mes FROM indicator_records ORDER BY mesNumero ASC")
    fun getAvailableMonths(): Flow<List<String>>

    @Query("SELECT DISTINCT periodo FROM indicator_records")
    fun getAvailablePeriods(): Flow<List<String>>

    @Query("SELECT DISTINCT tecnicoLogin FROM indicator_records ORDER BY tecnicoLogin ASC")
    fun getDistinctTechnicianLogins(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM indicator_records")
    suspend fun getRecordCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<IndicatorRecord>)

    @Query("DELETE FROM indicator_records WHERE origemArquivo = :fileName")
    suspend fun deleteByFileName(fileName: String)

    @Query("DELETE FROM indicator_records WHERE indicatorType = :indicatorType")
    suspend fun deleteByIndicatorType(indicatorType: String)

    @Query("DELETE FROM indicator_records")
    suspend fun clearAllRecords()
}

@Dao
interface UploadLogDao {
    @Query("SELECT * FROM upload_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<UploadLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: UploadLog): Long
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<AppNotification>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: AppNotification): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()
}
