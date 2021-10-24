package com.nunchuk.android.usecase

import com.nunchuk.android.nativelib.NunchukNativeSdk
import com.nunchuk.android.utils.CrashlyticsReporter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

interface EnableAutoBackupUseCase {
    fun execute(syncRoomId: String): Flow<Unit>
}

internal class EnableAutoBackupUseCaseImpl @Inject constructor(
    private val nativeSdk: NunchukNativeSdk
) : EnableAutoBackupUseCase {

    override fun execute(syncRoomId: String) = flow {
        emit(
            try {
                nativeSdk.enableAutoBackUp(syncRoomId)
            } catch (t: Throwable) {
                CrashlyticsReporter.recordException(t)
            }
        )
    }

}