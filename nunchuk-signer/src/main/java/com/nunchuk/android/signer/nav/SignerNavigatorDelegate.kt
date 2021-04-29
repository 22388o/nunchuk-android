package com.nunchuk.android.signer.nav

import android.content.Context
import com.nunchuk.android.nav.SignerNavigator
import com.nunchuk.android.signer.AirSignerIntroActivity
import com.nunchuk.android.signer.SignerIntroActivity
import com.nunchuk.android.signer.SoftwareSignerIntroActivity
import com.nunchuk.android.signer.add.AddSignerActivity
import com.nunchuk.android.signer.details.SignerInfoActivity
import com.nunchuk.android.signer.ss.confirm.ConfirmSeedActivity
import com.nunchuk.android.signer.ss.create.CreateNewSeedActivity
import com.nunchuk.android.signer.ss.name.AddSoftwareSignerNameActivity
import com.nunchuk.android.signer.ss.passphrase.SetPassphraseActivity
import com.nunchuk.android.signer.ss.recover.RecoverSeedActivity

interface SignerNavigatorDelegate : SignerNavigator {

    override fun openSignerIntroScreen(activityContext: Context) {
        SignerIntroActivity.start(activityContext)
    }

    override fun openSignerInfoScreen(activityContext: Context, signerName: String, signerSpec: String, justAdded: Boolean) {
        SignerInfoActivity.start(
            activityContext = activityContext,
            signerName = signerName,
            signerSpec = signerSpec,
            justAdded = justAdded
        )
    }

    override fun openAddAirSignerIntroScreen(activityContext: Context) {
        AirSignerIntroActivity.start(activityContext)
    }

    override fun openAddAirSignerScreen(activityContext: Context) {
        AddSignerActivity.start(activityContext)
    }

    override fun openAddSoftwareSignerScreen(activityContext: Context) {
        SoftwareSignerIntroActivity.start(activityContext)
    }

    override fun openCreateNewSeedScreen(activityContext: Context) {
        CreateNewSeedActivity.start(activityContext)
    }

    override fun openRecoverSeedScreen(activityContext: Context) {
        RecoverSeedActivity.start(activityContext)
    }

    override fun openSelectPhraseScreen(activityContext: Context, mnemonic: String) {
        ConfirmSeedActivity.start(activityContext, mnemonic)
    }

    override fun openAddSoftwareSignerNameScreen(activityContext: Context, mnemonic: String) {
        AddSoftwareSignerNameActivity.start(activityContext, mnemonic)
    }

    override fun openSetPassphraseScreen(activityContext: Context, mnemonic: String, signerName: String) {
        SetPassphraseActivity.start(activityContext, mnemonic, signerName)
    }

}