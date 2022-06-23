package com.nunchuk.android.usecase

import com.nunchuk.android.nativelib.NunchukNativeSdk
import com.nunchuk.android.utils.CrashlyticsReporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

interface EnableAutoBackupUseCase {
    fun execute(
        enable: Boolean
    ): Flow<Unit>
}

internal class EnableAutoBackupUseCaseImpl @Inject constructor(
    private val nativeSdk: NunchukNativeSdk
) : EnableAutoBackupUseCase {

    override fun execute(
        enable: Boolean
    ) = flow {
        emit(
            try {
                nativeSdk.enableAutoBackUp(enable = enable)
            } catch (t: Throwable) {
                CrashlyticsReporter.recordException(t)
            }
        )
    }

}