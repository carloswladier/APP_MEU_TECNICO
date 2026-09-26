package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlin.random.Random

class AppRepository(
    private val userDao: UserDao,
    private val indicatorDao: IndicatorDao,
    private val uploadLogDao: UploadLogDao,
    private val notificationDao: NotificationDao
) {
    suspend fun ensureSeeded() {
        val userCount = userDao.getUserCount()
        if (userCount == 0) {
            userDao.insertUsers(SampleData.getInitialUsers())
        } else {
            val adminUser = userDao.getUserByLogin("admin")
            if (adminUser != null && adminUser.passwordHash != "C.1985.w") {
                userDao.updatePassword(adminUser.id, "C.1985.w")
            }
        }
        val recordCount = indicatorDao.getRecordCount()
        if (recordCount == 0) {
            indicatorDao.insertRecords(SampleData.getInitialRecords())
            SampleData.getInitialNotifications().forEach {
                notificationDao.insertNotification(it)
            }
            uploadLogDao.insertLog(
                UploadLog(
                    fileName = "planilha_indicadores_oficial_2026.xlsx",
                    uploadedBy = "admin",
                    recordCount = SampleData.getInitialRecords().size,
                    status = "PROCESSADO_COM_SUCESSO"
                )
            )
        }
    }

    suspend fun authenticate(login: String, password: String): User? {
        ensureSeeded()
        return userDao.authenticate(login.trim(), password.trim())
    }

    suspend fun getUserByLogin(login: String): User? {
        ensureSeeded()
        return userDao.getUserByLogin(login.trim())
    }

    suspend fun getUserByEmail(email: String): User? {
        ensureSeeded()
        return userDao.getUserByEmail(email.trim())
    }

    suspend fun resetPasswordForLogin(login: String): Triple<Boolean, String, String> {
        ensureSeeded()
        val user = userDao.getUserByLogin(login.trim()) ?: return Triple(false, "", "")
        
        // Gerar nova senha temporária segura
        val randomSuffix = Random.nextInt(1000, 9999)
        val newTemporaryPassword = "Ind@$randomSuffix!"
        userDao.updatePassword(user.id, newTemporaryPassword)
        
        // Retorna sucesso, e-mail cadastrado pelo ADM, e a nova senha gerada
        return Triple(true, user.email, newTemporaryPassword)
    }

    fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers()

    suspend fun insertUser(user: User): Long {
        return userDao.insertUser(user)
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    suspend fun deleteUser(user: User) {
        userDao.deleteUser(user)
    }

    fun getRecordsForTechnician(tecnicoLogin: String, indicatorType: String): Flow<List<IndicatorRecord>> {
        return indicatorDao.getRecordsForTechnician(tecnicoLogin.trim(), indicatorType)
    }

    fun getRecordsForAdmin(indicatorType: String): Flow<List<IndicatorRecord>> {
        return indicatorDao.getRecordsForAdmin(indicatorType)
    }

    fun getAllRecords(): Flow<List<IndicatorRecord>> {
        return indicatorDao.getAllRecords()
    }

    fun getAvailableMonths(): Flow<List<String>> = indicatorDao.getAvailableMonths()

    fun getAvailablePeriods(): Flow<List<String>> = indicatorDao.getAvailablePeriods()

    fun getDistinctTechnicianLogins(): Flow<List<String>> = indicatorDao.getDistinctTechnicianLogins()

    suspend fun insertProcessedRecords(records: List<IndicatorRecord>, fileName: String, uploaderLogin: String) {
        indicatorDao.insertRecords(records)
        uploadLogDao.insertLog(
            UploadLog(
                fileName = fileName,
                uploadedBy = uploaderLogin,
                recordCount = records.size,
                status = "PROCESSADO_COM_SUCESSO"
            )
        )
        notificationDao.insertNotification(
            AppNotification(
                title = "📢 Nova Planilha Disponibilizada!",
                message = "O administrador carregou o arquivo '$fileName'. Os indicadores foram processados em tempo real.",
                fileName = fileName
            )
        )
    }

    suspend fun replaceRecordsForIndicator(
        indicatorType: String,
        records: List<IndicatorRecord>,
        fileName: String,
        source: String
    ) {
        if (records.isNotEmpty()) {
            indicatorDao.deleteByIndicatorType(indicatorType)
            indicatorDao.insertRecords(records)
            uploadLogDao.insertLog(
                UploadLog(
                    fileName = fileName,
                    uploadedBy = source,
                    recordCount = records.size,
                    status = "SINCRONIZADO_GITHUB_HOSTINGER"
                )
            )
            notificationDao.insertNotification(
                AppNotification(
                    title = "☁️ Planilha Sincronizada ($fileName)",
                    message = "Sincronizados ${records.size} registros do indicador $indicatorType a partir de $source.",
                    fileName = fileName
                )
            )
        }
    }

    suspend fun insertRemoteRecords(records: List<IndicatorRecord>, source: String = "Hostinger MySQL") {
        if (records.isNotEmpty()) {
            indicatorDao.insertRecords(records)
            uploadLogDao.insertLog(
                UploadLog(
                    fileName = "hostinger_sync_mysql",
                    uploadedBy = source,
                    recordCount = records.size,
                    status = "SINCRONIZADO_HOSTINGER"
                )
            )
            notificationDao.insertNotification(
                AppNotification(
                    title = "🔄 Sincronização Hostinger Concluída",
                    message = "Recebidos ${records.size} registros atualizados do banco MySQL da Hostinger.",
                    fileName = "hostinger_mysql"
                )
            )
        }
    }

    fun getNotifications(): Flow<List<AppNotification>> = notificationDao.getAllNotifications()

    fun getUnreadNotificationsCount(): Flow<Int> = notificationDao.getUnreadCount()

    suspend fun markNotificationAsRead(id: Long) = notificationDao.markAsRead(id)

    suspend fun markAllNotificationsAsRead() = notificationDao.markAllAsRead()

    fun getUploadLogs(): Flow<List<UploadLog>> = uploadLogDao.getAllLogs()

    suspend fun reloadSampleData() {
        indicatorDao.clearAllRecords()
        indicatorDao.insertRecords(SampleData.getInitialRecords())
    }
}
