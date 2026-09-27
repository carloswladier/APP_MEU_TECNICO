package com.example.data.remote

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig

class HostingerConfigManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("hostinger_prefs", Context.MODE_PRIVATE)

    fun getConfig(): HostingerConfig {
        val defaultApiUrl = try { BuildConfig.HOSTINGER_API_URL.ifBlank { "https://seusite.com.br/api/indicadores_api.php" } } catch (_: Exception) { "https://seusite.com.br/api/indicadores_api.php" }
        val defaultApiKey = try { BuildConfig.HOSTINGER_API_KEY.ifBlank { "claro_indicadores_sec_2026" } } catch (_: Exception) { "claro_indicadores_sec_2026" }
        val defaultOwner = try { BuildConfig.GITHUB_OWNER.ifBlank { "carloswladier" } } catch (_: Exception) { "carloswladier" }
        val defaultRepo = try { BuildConfig.GITHUB_REPO.ifBlank { "indicadores-claro" } } catch (_: Exception) { "indicadores-claro" }
        val defaultBranch = try { BuildConfig.GITHUB_BRANCH.ifBlank { "main" } } catch (_: Exception) { "main" }
        val defaultFolder = try { BuildConfig.GITHUB_FOLDER.let { if (it == "default" || it == "none" || it == "root") "" else it } } catch (_: Exception) { "" }
        val defaultToken = try { BuildConfig.GITHUB_TOKEN.let { if (it == "not_set" || it == "none" || it == "DEFAULT") "" else it } } catch (_: Exception) { "" }

        return HostingerConfig(
            apiUrl = prefs.getString(KEY_API_URL, defaultApiUrl) ?: defaultApiUrl,
            apiKey = prefs.getString(KEY_API_KEY, defaultApiKey) ?: defaultApiKey,
            githubOwner = prefs.getString(KEY_GH_OWNER, defaultOwner) ?: defaultOwner,
            githubRepo = prefs.getString(KEY_GH_REPO, defaultRepo) ?: defaultRepo,
            githubBranch = prefs.getString(KEY_GH_BRANCH, defaultBranch) ?: defaultBranch,
            githubFolderPath = prefs.getString(KEY_GH_FOLDER, defaultFolder) ?: defaultFolder,
            githubToken = prefs.getString(KEY_GH_TOKEN, defaultToken) ?: defaultToken,
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
            .putString(KEY_GH_TOKEN, config.githubToken.trim())
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
        private const val KEY_GH_TOKEN = "gh_token"
        private const val KEY_AUTO_SYNC = "auto_sync"
        private const val KEY_LAST_SYNC_TIME = "last_sync_time"
        private const val KEY_LAST_SYNC_STATUS = "last_sync_status"
    }
}
