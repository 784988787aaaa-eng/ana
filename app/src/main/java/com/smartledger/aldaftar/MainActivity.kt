package com.smartledger.aldaftar

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.smartledger.aldaftar.ui.root.SmartLedgerApp
import com.smartledger.aldaftar.ui.viewmodel.FinanceViewModel
import com.smartledger.aldaftar.ui.viewmodel.HabayebFinanceViewModel

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val factory = (application as FinanceApplication).viewModelFactory
        val financeViewModel = ViewModelProvider(this, factory)[FinanceViewModel::class.java]
        val habayebViewModel = ViewModelProvider(this, factory)[HabayebFinanceViewModel::class.java]
        splashScreen.setKeepOnScreenCondition { !financeViewModel.isSettingsLoaded.value }

        setContent {
            SmartLedgerApp(
                financeViewModel = financeViewModel,
                habayebViewModel = habayebViewModel,
                viewModelFactory = factory,
                onExit = ::finishAffinity
            )
        }
    }
}
