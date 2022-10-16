package com.nunchuk.android.core.util

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.nunchuk.android.core.R
import com.nunchuk.android.core.signer.SignerModel
import com.nunchuk.android.type.SignerType
import com.nunchuk.android.type.SignerType.AIRGAP
import com.nunchuk.android.type.SignerType.COLDCARD_NFC
import com.nunchuk.android.type.SignerType.FOREIGN_SOFTWARE
import com.nunchuk.android.type.SignerType.HARDWARE
import com.nunchuk.android.type.SignerType.NFC
import com.nunchuk.android.type.SignerType.SOFTWARE
import com.nunchuk.android.type.SignerType.UNKNOWN

fun SignerType.toReadableString(context: Context, isPrimaryKey: Boolean): String {
    if (isPrimaryKey) return context.getString(R.string.nc_signer_type_primary_key)
    return when (this) {
        AIRGAP -> context.getString(R.string.nc_signer_type_air_gapped)
        SOFTWARE -> context.getString(R.string.nc_signer_type_software)
        HARDWARE -> context.getString(R.string.nc_signer_type_hardware)
        FOREIGN_SOFTWARE -> context.getString(R.string.nc_signer_type_foreign_software)
        NFC, COLDCARD_NFC -> context.getString(R.string.nc_nfc)
        UNKNOWN -> context.getString(R.string.nc_unknown)
    }
}

fun SignerModel.toReadableSignerType(context: Context, isIgnorePrimary: Boolean = false) =
    type.toReadableString(context, if (isIgnorePrimary) false else isPrimaryKey)

fun SignerType.toReadableDrawable(context: Context, isPrimaryKey: Boolean = false): Drawable? {
    if (isPrimaryKey) return ContextCompat.getDrawable(context, R.drawable.ic_signer_type_primary_key_small)
    return when (this) {
        AIRGAP, COLDCARD_NFC -> ContextCompat.getDrawable(context, R.drawable.ic_air_signer_small)
        SOFTWARE -> ContextCompat.getDrawable(context, R.drawable.ic_singer_type_software)
        HARDWARE -> ContextCompat.getDrawable(context, R.drawable.ic_signer_type_wired)
        FOREIGN_SOFTWARE -> ContextCompat.getDrawable(context, R.drawable.ic_singer_type_software)
        NFC -> ContextCompat.getDrawable(context, R.drawable.ic_nfc_card)
        UNKNOWN -> ContextCompat.getDrawable(context, R.drawable.ic_air_signer_small)
    }
}

fun SignerModel.toReadableSignerTypeDrawable(context: Context, isPrimaryKey: Boolean = false) = type.toReadableDrawable(context, isPrimaryKey)