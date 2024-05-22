package com.example.kotlinflowsinjetpackcompose

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    /** * ColdFlow -> flow collect edilinceye kadar veri üretilmez. Aynı flow'u birden fazla kez collect edersek, her seferinde veri üretimi yeniden başlar.
     *  * HotFlow -> veri üretimi collect edilip edilmemesine bakılmadan başlar ve devam eder. SharedFlow, StateFlow
     */
    val counterFlow = flow<Int> {
        val countDownValue = 10
        var currentValue = countDownValue
        emit(countDownValue)
        while (currentValue > 0) {
            delay(1000L)
            currentValue --
            emit(currentValue)
        }
    }

    init {
        //collectFlow()
        collectLatestFlow()
    }

/** * collect -> Her emit edilen öğeyi işler ve önceki işlemin tamamlanmasını bekler.
 *
 * * collectLatest -> En son yayılan öğeyi işler. Yeni bir öğe geldiğinde önceki işlemeye devam ediyorsa iptal eder ve yeni öğe işlemeye başlar.
 */
    private fun collectFlow() {
        viewModelScope.launch {
            counterFlow.collect {
                Log.i("CounterState", "Counter value: $it")
            }
        }
    }

    private fun collectLatestFlow() {
        viewModelScope.launch {
            counterFlow.collectLatest {
                delay(1500L)
                Log.i("CounterState", "Counter value: $it")
            }
        }
    }
}