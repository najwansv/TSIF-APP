
package com.samsung.android.health.sdk.sample.healthdiary.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.samsung.android.health.sdk.sample.healthdiary.R
import com.samsung.android.health.sdk.sample.healthdiary.adapters.HeartRateAdapter
import com.samsung.android.health.sdk.sample.healthdiary.databinding.HealthMainBinding
import com.samsung.android.health.sdk.sample.healthdiary.utils.AppConstants
import com.samsung.android.health.sdk.sample.healthdiary.utils.formatString
import com.samsung.android.health.sdk.sample.healthdiary.utils.showToast
import com.samsung.android.health.sdk.sample.healthdiary.viewmodel.HealthMainViewModel
import com.samsung.android.health.sdk.sample.healthdiary.viewmodel.HealthViewModelFactory
import com.samsung.android.health.sdk.sample.healthdiary.viewmodel.HeartRateViewModel
import com.samsung.android.sdk.health.data.HealthDataStore
import com.samsung.android.sdk.health.data.permission.AccessType
import com.samsung.android.sdk.health.data.permission.Permission
import com.samsung.android.sdk.health.data.request.DataTypes
import com.samsung.android.sdk.health.data.request.LocalTimeFilter
import com.samsung.android.sdk.health.data.request.Ordering
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import java.time.LocalDateTime



class HealthMainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var healthMainViewModel: HealthMainViewModel
    private lateinit var heartRateViewModel: HeartRateViewModel
    private lateinit var bpmTextView: TextView


    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        healthMainViewModel = ViewModelProvider(
            this, HealthViewModelFactory(this)
        )[HealthMainViewModel::class.java]

        heartRateViewModel = ViewModelProvider(
            this, HealthViewModelFactory(this)
        )[HeartRateViewModel::class.java]

        /**  Initialize OnClickListener on Heart Rate, Sleep, Nutrition and Step buttons */
        DataBindingUtil
            .setContentView<HealthMainBinding>(this, R.layout.health_main)
            .run {
                cvNutrition.setOnClickListener(this@HealthMainActivity)
                cvStep.setOnClickListener(this@HealthMainActivity)
                cvHeartRate.setOnClickListener(this@HealthMainActivity)
                cvSleep.setOnClickListener(this@HealthMainActivity)
            }

        /** Show toast on exception occurrence **/
        healthMainViewModel.exceptionResponse.observe(this) { message ->
            showToast(this, message)
        }

        bpmTextView = findViewById(R.id.BPM)
        collectResponse()
        observeHeartRateData()
        startHeartRateUpdates()

//        JANLUP HAPUS INI RNG
        heartRateViewModel.generateDummyHourlyHeartRateData()

        heartRateViewModel.generateDummyDailyHeartRateData()

    }
    private fun observeHeartRateData() {
        heartRateViewModel.fiveMinutesHR.observe(this) { heartRate ->
            if (heartRate.isNotEmpty()) {
                val avg = heartRate[11].avg
                val Stime = heartRate[11].startTime
                val Etime = heartRate[11].endTime

                println("$Stime - $Etime")
                bpmTextView.text = formatString(avg)
            }
        }
    }

    private fun startHeartRateUpdates() {
        runnable = Runnable {
            heartRateViewModel.readFromWatches(LocalDateTime.now())
            handler.postDelayed(runnable, 5 * 60 * 1000) // 5 minutes
        }
        handler.post(runnable)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.connect_samsung_health, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.permission -> {
                healthMainViewModel.connectToSamsungHealth(this)
                true
            }

            else -> false
        }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.cv_nutrition -> {
                val permSet = mutableSetOf(
                    Permission.of(DataTypes.NUTRITION, AccessType.READ)
                )
                healthMainViewModel.checkForPermission(
                    this,
                    permSet,
                    AppConstants.NUTRITION_ACTIVITY
                )
            }

            R.id.cv_step -> {
                val permSet = mutableSetOf(
                    Permission.of(DataTypes.STEPS, AccessType.READ)
                )
                healthMainViewModel.checkForPermission(this, permSet, AppConstants.STEP_ACTIVITY)
            }

            R.id.cv_heart_rate -> {
                val permSet = mutableSetOf(
                    Permission.of(DataTypes.HEART_RATE, AccessType.READ)
                )
                healthMainViewModel.checkForPermission(
                    this,
                    permSet,
                    AppConstants.HEART_RATE_ACTIVITY
                )
            }

            R.id.cv_sleep -> {
                val permSet = mutableSetOf(
                    Permission.of(DataTypes.SLEEP, AccessType.READ),
                    Permission.of(DataTypes.BLOOD_OXYGEN, AccessType.READ),
                    Permission.of(DataTypes.SKIN_TEMPERATURE, AccessType.READ)
                )
                healthMainViewModel.checkForPermission(this, permSet, AppConstants.SLEEP_ACTIVITY)
            }
        }
    }

    private fun collectResponse() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {

                /**  Handle response of permission request */
                launch {
                    healthMainViewModel.permissionResponse.collect { result ->
                        if (result.first == AppConstants.SUCCESS) {
                            launchRespectiveActivity(result.second)
                        } else if (result.first != AppConstants.WAITING) {
                            showToast(this@HealthMainActivity, result.first)
                        }
                        healthMainViewModel.resetPermissionResponse()
                    }
                }
            }
        }
    }

    private fun launchRespectiveActivity(activityId: Int) {
        val intent = when (activityId) {
            AppConstants.NUTRITION_ACTIVITY -> Intent(this, NutritionActivity::class.java)
            AppConstants.STEP_ACTIVITY -> Intent(this, StepActivity::class.java)
            AppConstants.HEART_RATE_ACTIVITY -> Intent(this, HeartRateActivity::class.java)
            AppConstants.SLEEP_ACTIVITY -> Intent(this, SleepActivity::class.java)
            else -> null
        }
        if (intent != null) {
            startActivity(intent)
        }
    }
}
