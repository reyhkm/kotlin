package com.example.periodtracker.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.periodtracker.data.dao.CycleDao
import com.example.periodtracker.data.model.Cycle
import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

data class DashboardUiState(
    val periodStatus: String = "",
    val dayInfo: String = "",
    val nextPeriodPrediction: String = "Loading...",
    val averageCycleLength: String = "--",
    val averagePeriodLength: String = "--",
    val isPeriodOngoing: Boolean = false,
    val chartEntryProducer: ChartEntryModelProducer = ChartEntryModelProducer()
)

class DashboardViewModel(private val cycleDao: CycleDao) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            cycleDao.getAllCycles().collectLatest { cycles ->
                processCycleData(cycles)
            }
        }
    }

    private fun processCycleData(cycles: List<Cycle>) {
        if (cycles.isEmpty()) {
            _uiState.update {
                it.copy(
                    periodStatus = "Welcome, Jihan!",
                    dayInfo = "Log your first period to begin, love.",
                    nextPeriodPrediction = "No data yet."
                )
            }
            return
        }

        val latestCycle = cycles.first()
        val isPeriodOngoing = latestCycle.endDate == null

        val avgCycleLength = calculateAverageCycleLength(cycles)
        val avgPeriodLength = calculateAveragePeriodLength(cycles)

        val periodStatus: String
        val dayInfo: String
        val nextPeriodPrediction: String

        if (isPeriodOngoing) {
            val daysIntoPeriod = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - latestCycle.startDate) + 1
            periodStatus = "Day $daysIntoPeriod"
            dayInfo = "of your period"
            nextPeriodPrediction = "Period in progress"
        } else {
            val nextPeriodStartDate = calculateNextPeriodDate(latestCycle.startDate, avgCycleLength)
            val daysUntilNext = TimeUnit.MILLISECONDS.toDays(nextPeriodStartDate - System.currentTimeMillis())
            periodStatus = if (daysUntilNext > 0) daysUntilNext.toString() else "Due"
            dayInfo = if (daysUntilNext > 1) "days until next period" else if (daysUntilNext == 1L) "day until next period" else "Your period is expected"
            nextPeriodPrediction = "Around ${com.example.periodtracker.util.DateUtils.formatDate(nextPeriodStartDate)}"
        }

        // Chart Data
        val chartEntries = cycles.filter { it.endDate != null }.reversed().takeLast(6).mapIndexed { index, cycle ->
            val cycleLength = TimeUnit.MILLISECONDS.toDays(cycles.getOrNull(index + 1)?.startDate?.minus(cycle.startDate) ?: 0)
            object : ChartEntry {
                override val x: Float = index.toFloat()
                override val y: Float = if(cycleLength > 0) cycleLength.toFloat() else 0f
            }
        }.filter { it.y > 0 }

        _uiState.value.chartEntryProducer.setEntries(chartEntries)

        _uiState.update {
            it.copy(
                periodStatus = periodStatus,
                dayInfo = dayInfo,
                nextPeriodPrediction = nextPeriodPrediction,
                averageCycleLength = if (avgCycleLength > 0) "$avgCycleLength days" else "--",
                averagePeriodLength = if (avgPeriodLength > 0) "$avgPeriodLength days" else "--",
                isPeriodOngoing = isPeriodOngoing
            )
        }
    }

    fun logPeriodStart() {
        viewModelScope.launch {
            val newCycle = Cycle(startDate = System.currentTimeMillis())
            cycleDao.insert(newCycle)
        }
    }

    fun logPeriodEnd() {
        viewModelScope.launch {
            val latestCycle = cycleDao.getLatestCycle()
            latestCycle?.let {
                if (it.endDate == null) {
                    val updatedCycle = it.copy(endDate = System.currentTimeMillis())
                    cycleDao.update(updatedCycle)
                }
            }
        }
    }

    private fun calculateAverageCycleLength(cycles: List<Cycle>): Int {
        if (cycles.size < 2) return 28 // Default
        val completedCycles = cycles.filter { it.endDate != null }
        if (completedCycles.size < 2) return 28 // Default

        val cycleLengths = completedCycles.zipWithNext { a, b ->
            TimeUnit.MILLISECONDS.toDays(a.startDate - b.startDate)
        }
        return if (cycleLengths.isNotEmpty()) cycleLengths.average().toInt() else 28
    }

    private fun calculateAveragePeriodLength(cycles: List<Cycle>): Int {
        val completedCycles = cycles.filter { it.endDate != null }
        if (completedCycles.isEmpty()) return 5 // Default

        val periodLengths = completedCycles.map { 
            TimeUnit.MILLISECONDS.toDays(it.endDate!! - it.startDate)
        }
        return if (periodLengths.isNotEmpty()) periodLengths.average().toInt() else 5
    }

    private fun calculateNextPeriodDate(lastStartDate: Long, avgCycleLength: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = lastStartDate
        calendar.add(Calendar.DAY_OF_YEAR, avgCycleLength)
        return calendar.timeInMillis
    }

    class Factory(private val dao: CycleDao) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DashboardViewModel(dao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}