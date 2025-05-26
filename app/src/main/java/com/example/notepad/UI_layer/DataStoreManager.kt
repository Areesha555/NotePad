import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property for DataStore
val Context.dataStore by preferencesDataStore(name = "settings")

class DataStoreManager(private val context: Context) {

    companion object {
        val PRINTER_IP_KEY = stringPreferencesKey("printer_ip")
    }


    suspend fun savePrinterIp(ip: String) {
        context.dataStore.edit { preferences ->
            preferences[PRINTER_IP_KEY] = ip
        }
    }


    val printerIpFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PRINTER_IP_KEY] ?: ""
        }
}
