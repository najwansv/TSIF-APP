
package com.samsung.android.health.sdk.sample.healthdiary.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.samsung.android.health.sdk.sample.healthdiary.R
import com.samsung.android.health.sdk.sample.healthdiary.databinding.HeartRateListBinding
import com.samsung.android.health.sdk.sample.healthdiary.utils.formatString
import com.samsung.android.health.sdk.sample.healthdiary.viewmodel.HeartRateViewModel

class HeartRateAdapter : RecyclerView.Adapter<HeartRateAdapter.ViewHolder>() {

    private var heartRateList: MutableList<HeartRateViewModel.HeartRate> = mutableListOf()
    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeartRateAdapter.ViewHolder {
        val binding =
            HeartRateListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val heartRate = heartRateList[position]
        val heartRateTime = "${heartRate.startTime} - ${heartRate.endTime}"
        holder.binding.run {
            this.heartRateTime.text = heartRateTime
            this.heartRateValue.text = formatString(heartRate.avg)
        }
        if (heartRate.max != 0f) {
            val maxHeartRate =
                heartRate.max.toInt().toString() + context.getString(R.string.heart_rate_unit)
            holder.binding.maxHeartRateValue.text = maxHeartRate
        } else {
            holder.binding.maxHeartRateValue.text = context.getString(R.string.no_data)
        }
        if (heartRate.min != 1000f) {
            val minHeartRate =
                heartRate.min.toInt().toString() + context.getString(R.string.heart_rate_unit)
            holder.binding.minHeartRateValue.text = minHeartRate
        } else {
            holder.binding.minHeartRateValue.text = context.getString(R.string.no_data)
        }
    }

    override fun getItemCount(): Int {
        return heartRateList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(heartRateData: List<HeartRateViewModel.HeartRate>, filterAbove110: Boolean = true) {
        heartRateList.clear()
        if (filterAbove110) {
            heartRateList.addAll(heartRateData.filter { it.avg > 110 })
        } else {
            heartRateList.addAll(heartRateData)
        }
//        heartRateList.addAll(heartRateData)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: HeartRateListBinding) :
        RecyclerView.ViewHolder(binding.root)
}
