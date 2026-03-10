package wang.zengye.dsm.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import wang.zengye.dsm.ui.theme.DarkMode

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dsm_settings")

object SettingsManager {
    private lateinit var context: Context
    private lateinit var encryptedPrefs: EncryptedSharedPreferences
    private lateinit var masterKey: MasterKey
    
    // 加密存储的密码
    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    // 加密存储的会话令牌
    private val _sid = MutableStateFlow("")
    val sid: StateFlow<String> = _sid.asStateFlow()

    private val _cookie = MutableStateFlow("")
    val cookie: StateFlow<String> = _cookie.asStateFlow()

    private val _synoToken = MutableStateFlow("")
    val synoToken: StateFlow<String> = _synoToken.asStateFlow()

    // 加密存储的服务器列表
    private val _servers = MutableStateFlow("")
    val servers: StateFlow<String> = _servers.asStateFlow()
    
    private const val ENCRYPTED_PREFS_NAME = "dsm_secure"
    private const val KEY_PASSWORD = "encrypted_password"
    private const val KEY_SID = "encrypted_sid"
    private const val KEY_COOKIE = "encrypted_cookie"
    private const val KEY_SYNO_TOKEN = "encrypted_syno_token"
    private const val KEY_SERVERS = "encrypted_servers"
    
    // Keys for DataStore (非敏感配置)
    private val DARK_MODE = intPreferencesKey("dark_mode")
    private val HOST = stringPreferencesKey("host")
    private val ACCOUNT = stringPreferencesKey("account")
    private val PASSWORD_OLD = stringPreferencesKey("password") // 保留用于清理旧数据
    private val SID_OLD = stringPreferencesKey("sid") // 保留用于迁移
    private val COOKIE_OLD = stringPreferencesKey("cookie") // 保留用于迁移
    private val SYNO_TOKEN_OLD = stringPreferencesKey("syno_token") // 保留用于迁移
    private val SERVERS_OLD = stringPreferencesKey("servers") // 保留用于迁移
    private val REFRESH_DURATION = intPreferencesKey("refresh_duration")
    private val DOWNLOAD_WIFI_ONLY = booleanPreferencesKey("download_wifi_only")
    private val DOWNLOAD_SAVE_PATH = stringPreferencesKey("download_save_path")
    private val DOWNLOAD_DIRECTORY_URI = stringPreferencesKey("download_directory_uri")
    private val CHECK_SSL = booleanPreferencesKey("check_ssl")
    private val VIBRATE_ON = booleanPreferencesKey("vibrate_on")
    private val LAUNCH_AUTH = booleanPreferencesKey("launch_auth")
    private val AGREEMENT = booleanPreferencesKey("agreement")
    
    // Flows
    val darkMode: Flow<DarkMode> get() = context.dataStore.data.map { preferences ->
        when (preferences[DARK_MODE] ?: 2) {
            0 -> DarkMode.LIGHT
            1 -> DarkMode.DARK
            else -> DarkMode.SYSTEM
        }
    }
    
    val host: Flow<String> get() = context.dataStore.data.map { it[HOST] ?: "" }
    val account: Flow<String> get() = context.dataStore.data.map { it[ACCOUNT] ?: "" }
    val refreshDuration: Flow<Int> get() = context.dataStore.data.map { it[REFRESH_DURATION] ?: 10 }
    val downloadWifiOnly: Flow<Boolean> get() = context.dataStore.data.map { it[DOWNLOAD_WIFI_ONLY] ?: true }
    val downloadSavePath: Flow<String> get() = context.dataStore.data.map { it[DOWNLOAD_SAVE_PATH] ?: "" }
    val downloadDirectoryUri: Flow<String> get() = context.dataStore.data.map { it[DOWNLOAD_DIRECTORY_URI] ?: "" }
    val checkSsl: Flow<Boolean> get() = context.dataStore.data.map { it[CHECK_SSL] ?: true }
    val vibrateOn: Flow<Boolean> get() = context.dataStore.data.map { it[VIBRATE_ON] ?: true }
    val launchAuth: Flow<Boolean> get() = context.dataStore.data.map { it[LAUNCH_AUTH] ?: false }
    val agreement: Flow<Boolean> get() = context.dataStore.data.map { it[AGREEMENT] ?: false }
    
    fun init(context: Context) {
        this.context = context.applicationContext
        
        // 初始化加密存储
        masterKey = MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        
        encryptedPrefs = EncryptedSharedPreferences.create(
            context.applicationContext,
            ENCRYPTED_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
        
        // 加载已保存的密码
        _password.value = encryptedPrefs.getString(KEY_PASSWORD, "") ?: ""

        // 加载加密存储的会话令牌和服务器列表
        _sid.value = encryptedPrefs.getString(KEY_SID, "") ?: ""
        _cookie.value = encryptedPrefs.getString(KEY_COOKIE, "") ?: ""
        _synoToken.value = encryptedPrefs.getString(KEY_SYNO_TOKEN, "") ?: ""
        _servers.value = encryptedPrefs.getString(KEY_SERVERS, "") ?: ""
    }

    /**
     * 从旧的 DataStore 迁移敏感数据到 EncryptedSharedPreferences
     * 应在协程中调用
     */
    suspend fun migrateIfNeeded() {
        context.dataStore.edit { preferences ->
            // 迁移 SID
            preferences[SID_OLD]?.let { oldSid ->
                if (oldSid.isNotEmpty() && _sid.value.isEmpty()) {
                    encryptedPrefs.edit().putString(KEY_SID, oldSid).apply()
                    _sid.value = oldSid
                }
                preferences.remove(SID_OLD)
            }
            // 迁移 Cookie
            preferences[COOKIE_OLD]?.let { oldCookie ->
                if (oldCookie.isNotEmpty() && _cookie.value.isEmpty()) {
                    encryptedPrefs.edit().putString(KEY_COOKIE, oldCookie).apply()
                    _cookie.value = oldCookie
                }
                preferences.remove(COOKIE_OLD)
            }
            // 迁移 SynoToken
            preferences[SYNO_TOKEN_OLD]?.let { oldToken ->
                if (oldToken.isNotEmpty() && _synoToken.value.isEmpty()) {
                    encryptedPrefs.edit().putString(KEY_SYNO_TOKEN, oldToken).apply()
                    _synoToken.value = oldToken
                }
                preferences.remove(SYNO_TOKEN_OLD)
            }
            // 迁移 Servers
            preferences[SERVERS_OLD]?.let { oldServers ->
                if (oldServers.isNotEmpty() && _servers.value.isEmpty()) {
                    encryptedPrefs.edit().putString(KEY_SERVERS, oldServers).apply()
                    _servers.value = oldServers
                }
                preferences.remove(SERVERS_OLD)
            }
            // 清理旧密码
            preferences.remove(PASSWORD_OLD)
        }
    }
    
    suspend fun setDarkMode(mode: DarkMode) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE] = when (mode) {
                DarkMode.LIGHT -> 0
                DarkMode.DARK -> 1
                DarkMode.SYSTEM -> 2
            }
        }
    }
    
    suspend fun setHost(value: String) {
        context.dataStore.edit { it[HOST] = value }
    }
    
    suspend fun setAccount(value: String) {
        context.dataStore.edit { it[ACCOUNT] = value }
    }
    
    /**
     * 使用加密存储保存密码
     */
    fun setPassword(value: String) {
        encryptedPrefs.edit().putString(KEY_PASSWORD, value).apply()
        _password.value = value
    }
    
    /**
     * 获取加密存储的密码
     */
    fun getPassword(): String {
        return encryptedPrefs.getString(KEY_PASSWORD, "") ?: ""
    }
    
    fun setSid(value: String) {
        encryptedPrefs.edit().putString(KEY_SID, value).apply()
        _sid.value = value
    }

    fun setCookie(value: String) {
        encryptedPrefs.edit().putString(KEY_COOKIE, value).apply()
        _cookie.value = value
    }

    fun setSynoToken(value: String) {
        encryptedPrefs.edit().putString(KEY_SYNO_TOKEN, value).apply()
        _synoToken.value = value
    }

    fun setServers(value: String) {
        encryptedPrefs.edit().putString(KEY_SERVERS, value).apply()
        _servers.value = value
    }
    
    suspend fun setRefreshDuration(value: Int) {
        context.dataStore.edit { it[REFRESH_DURATION] = value }
    }
    
    suspend fun setDownloadWifiOnly(value: Boolean) {
        context.dataStore.edit { it[DOWNLOAD_WIFI_ONLY] = value }
    }
    
    suspend fun setDownloadSavePath(value: String) {
        context.dataStore.edit { it[DOWNLOAD_SAVE_PATH] = value }
    }

    suspend fun setDownloadDirectoryUri(value: String) {
        context.dataStore.edit { it[DOWNLOAD_DIRECTORY_URI] = value }
    }

    suspend fun setCheckSsl(value: Boolean) {
        context.dataStore.edit { it[CHECK_SSL] = value }
    }
    
    suspend fun setVibrateOn(value: Boolean) {
        context.dataStore.edit { it[VIBRATE_ON] = value }
    }
    
    suspend fun setLaunchAuth(value: Boolean) {
        context.dataStore.edit { it[LAUNCH_AUTH] = value }
    }
    
    suspend fun setAgreement(value: Boolean) {
        context.dataStore.edit { it[AGREEMENT] = value }
    }
    
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(SID_OLD)
            preferences.remove(COOKIE_OLD)
            preferences.remove(SYNO_TOKEN_OLD)
            preferences.remove(PASSWORD_OLD)
        }
        // 清除加密存储的敏感数据
        encryptedPrefs.edit()
            .remove(KEY_PASSWORD)
            .remove(KEY_SID)
            .remove(KEY_COOKIE)
            .remove(KEY_SYNO_TOKEN)
            .apply()
        _password.value = ""
        _sid.value = ""
        _cookie.value = ""
        _synoToken.value = ""
    }
}
