package br.edu.orlandovandres.temperocerto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import br.edu.orlandovandres.temperocerto.domain.repository.ConnectivityObserver
import br.edu.orlandovandres.temperocerto.presentation.navigation.TemperoCertoAppRoot
import br.edu.orlandovandres.temperocerto.presentation.theme.TemperoCertoTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Activity principal da aplicação TemperoCerto.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var connectivityObserver: ConnectivityObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TemperoCertoTheme {
                TemperoCertoAppRoot(connectivityObserver = connectivityObserver)
            }
        }
    }
}
