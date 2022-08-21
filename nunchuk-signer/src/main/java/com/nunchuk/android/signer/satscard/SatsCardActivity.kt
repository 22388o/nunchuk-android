package com.nunchuk.android.signer.satscard

import android.app.Activity
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import com.nunchuk.android.core.base.BaseActivity
import com.nunchuk.android.model.SatsCardStatus
import com.nunchuk.android.signer.R
import com.nunchuk.android.signer.databinding.ActivitySatsCardBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SatsCardActivity : BaseActivity<ActivitySatsCardBinding>() {
    override fun initializeBinding(): ActivitySatsCardBinding {
        return ActivitySatsCardBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        val inflater = navHost.navController.navInflater
        val graph = inflater.inflate(R.navigation.satscard_navigation)
        val args: SatsCardArgs = SatsCardArgs.deserializeBundle(intent.extras!!)
        val startDestinationId = if (args.isShowUnseal) {
            R.id.satsCardUnsealSlotFragment
        } else {
            R.id.satsCardSlotFragment
        }
        graph.setStartDestination(startDestinationId)
        navHost.navController.setGraph(graph, intent.extras)
    }

    companion object {
        fun navigate(activity: Activity, status: SatsCardStatus, hasWallet: Boolean, isShowUnseal: Boolean = false) {
            activity.startActivity(SatsCardArgs(status, hasWallet, isShowUnseal).buildIntent(activity))
        }
    }
}