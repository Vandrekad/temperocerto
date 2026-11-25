package br.edu.orlandovandres.temperocerto.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import br.edu.orlandovandres.temperocerto.domain.repository.ConnectivityObserver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Observador de conectividade seguro contra memory leaks utilizando ApplicationContext
 * e registro/desregistro simétrico de NetworkCallback em callbackFlow.
 */
@Singleton
class NetworkConnectivityObserver @Inject constructor(
    @ApplicationContext private val context: Context
) : ConnectivityObserver {

    private val connectivityManager: ConnectivityManager? =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    override val isOnline: Flow<Boolean> = callbackFlow {
        val manager = connectivityManager
        if (manager == null) {
            trySend(false)
            close()
            return@callbackFlow
        }

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(currentIsOnline())
            }

            override fun onLost(network: Network) {
                trySend(currentIsOnline())
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                trySend(hasValidInternet(networkCapabilities))
            }
        }

        trySend(currentIsOnline())

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        runCatching { manager.registerNetworkCallback(request, callback) }
            .onFailure { trySend(currentIsOnline()) }

        awaitClose {
            runCatching { manager.unregisterNetworkCallback(callback) }
        }
    }.distinctUntilChanged()

    override fun currentIsOnline(): Boolean {
        val manager = connectivityManager ?: return false
        val activeNetwork = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(activeNetwork) ?: return false
        return hasValidInternet(capabilities)
    }

    private fun hasValidInternet(capabilities: NetworkCapabilities): Boolean {
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
    }
}
