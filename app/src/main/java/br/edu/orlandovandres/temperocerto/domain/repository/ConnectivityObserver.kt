package br.edu.orlandovandres.temperocerto.domain.repository

import kotlinx.coroutines.flow.Flow

interface ConnectivityObserver {
    val isOnline: Flow<Boolean>
    fun currentIsOnline(): Boolean
}
