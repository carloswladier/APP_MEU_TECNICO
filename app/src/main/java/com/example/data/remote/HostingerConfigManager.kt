package com.example.data.remote

import android.content.Context
import android.content.SharedPreferences

class HostingerConfigManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("hostinger_prefs", Context.MODE_PRIVATE)

    fun getConfig(): HostingerConfig {
        return HostingerConfig(
            apiUrl = prefs.getString(KEY_API_URL, "https://seusite.com.br/api/indicadores_api.php") ?: "https://seusite.com.br/api/indicadores_api.php",
            apiKey = prefs.getString(KEY_API_KEY, "claro_indicadores_sec_2026") ?: "claro_indicadores_sec_2026",
            githubOwner = prefs.getString(KEY_GH_OWNER, "carloswladier") ?: "carloswladier",
            githubRepo = prefs.getString(KEY_GH_REPO, "indicadores-claro") ?: "indicadores-claro",
            githubBranch = prefs.getString(KEY_GH_BRANCH, "main") ?: "main",
            githubFolderPath = prefs.getString(KEY_GH_FOLDER, "") ?: "",
            autoSyncOnStart = prefs.getBoolean(KEY_AUTO_SYNC, false),
            lastSyncTimestamp = prefs.getLong(KEY_LAST_SYNC_TIME, 0L),
            lastSyncStatus = prefs.getString(KEY_LAST_SYNC_STATUS, "Aguardando primeira sincronização") ?: "Aguardando primeira sincronização"
        )
    }

    fun saveConfig(config: HostingerConfig) {
        prefs.edit()
            .putString(KEY_API_URL, config.apiUrl.trim())
            .putString(KEY_API_KEY, config.apiKey.trim())
            .putString(KEY_GH_OWNER, config.githubOwner.trim())
            .putString(KEY_GH_REPO, config.githubRepo.trim())
            .putString(KEY_GH_BRANCH, config.githubBranch.trim())
            .putString(KEY_GH_FOLDER, config.githubFolderPath.trim())
            .putBoolean(KEY_AUTO_SYNC, config.autoSyncOnStart)
            .putLong(KEY_LAST_SYNC_TIME, config.lastSyncTimestamp)
            .putString(KEY_LAST_SYNC_STATUS, config.lastSyncStatus)
            .apply()
    }

    fun updateSyncStatus(status: String, timestamp: Long = System.currentTimeMillis()) {
        prefs.edit()
            .putString(KEY_LAST_SYNC_STATUS, status)
            .putLong(KEY_LAST_SYNC_TIME, timestamp)
            .apply()
    }

    companion object {
        private const val KEY_API_URL = "api_url"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_GH_OWNER = "gh_owner"
        private const val KEY_GH_REPO = "gh_repo"
        private const val KEY_GH_BRANCH = "gh_branch"
        private const val KEY_GH_FOLDER = "gh_folder"
        private const val KEY_AUTO_SYNC = "auto_sync"
        private const val KEY_LAST_SYNC_TIME = "last_sync_time"
        private const val KEY_LAST_SYNC_STATUS = "last_sync_status"
    }
}
