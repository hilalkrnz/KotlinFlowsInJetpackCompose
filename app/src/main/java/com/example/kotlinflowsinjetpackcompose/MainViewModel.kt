package com.example.kotlinflowsinjetpackcompose

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.reduce
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel : ViewModel() {

    /** * ColdFlow -> flow collect edilinceye kadar veri üretilmez. Aynı flow'u birden fazla kez collect edersek, her seferinde veri üretimi yeniden başlar.
     *  * HotFlow -> veri üretimi collect edilip edilmemesine bakılmadan başlar ve devam eder. SharedFlow, StateFlow
     */
    val counterFlow = flow {
        val countDownValue = 10
        var currentValue = countDownValue
        emit(countDownValue)
        while (currentValue > 0) {
            delay(1000L)
            currentValue--
            emit(currentValue)
        }
    }

    init {
        collectFlow()
        collectLatestFlow()
        collectFlowReduce()
        collectFlowFlat()
        collectFlowBuffer()
    }

    /** * collect -> Her emit edilen öğeyi işler ve önceki işlemin tamamlanmasını bekler.
     *
     * * collectLatest -> En son yayılan öğeyi işler. Yeni bir öğe geldiğinde önceki işlemeye devam ediyorsa iptal eder ve yeni öğe işlemeye başlar.
     *
     * * count -> Verilen koşulu sağlayan kaç parametre olduğunu döner.
     *
     * * reduce -> Koleksiyonun tüm öğelerini belirli bir işlemle birleştirerek tek bir değer elde eder.
     */
    private fun collectFlow() {
        viewModelScope.launch {
            val count = counterFlow
                .filter { count -> count % 2 == 0 }
                .map { count -> count * count }
                .onEach { Log.i("CounterStateLog", "Log 2 : $it") }
                .count {
                    it % 2 == 0
                }

            Log.i("CounterState", "Counter value: $count") //Counter value: 6
        }
    }

    private fun collectFlowReduce() {
        viewModelScope.launch {
            val reduce = counterFlow
                .reduce { accumulator, value ->
                    accumulator + value
                }

            val fold = counterFlow
                .fold(100) { accumulator, value ->
                    accumulator + value
                }

            Log.i("CounterState", "reduce value: $reduce \n fold value: $fold ")
        }
    }

    /** * flatMapConcat -> Öğeleri sırayla dönüştürür ve birleştirir.
     *
     * * flatMapLatest -> Yeni öğe geldiğinde önceki dönüşümleri iptal eder ve en son öğeyi işler.
     *
     * * flatMapMerge -> Öğeleri paralel olarak dönüştürür ve birleştirir.
     */
    private fun collectFlowFlat() {
        val flow = flow {
            emit("Hi ")
            delay(500L)
            emit("Are u ")
        }
        viewModelScope.launch {
            flow.flatMapMerge { message ->
                flow {
                    emit(message + "Hilal")
                    delay(500L)
                    emit(message + "Burak")
                }
            }.collect { message ->
                Log.i("CounterState", "$message ")
            }
        }
    }

    /** * buffer -> Veri akışını bir ara belleğe (buffer) alarak veri yayımını ve işlemini birbirinden ayırır.
     * Verileri bir ara bellekte tutar, böylece tüketici daha yavaş çalışırken bile üretici veri yaymaya devam edebilir.
     * Performansı artırabilir, özellikle de veri yayım ve işleme hızları farklı olduğunda.
     *
     * * conflate -> Daha hızlı veri yayımı sağlamak için önceki değerleri atlayarak sadece en son yayılan değeri işlemeyi sağlar.
     * Bu, veri akışının en son durumunu almak için kullanışlıdır ve aradaki değerlerin önemli olmadığı senaryolarda etkilidir.
     */
    private fun collectFlowBuffer() {
        val flow = flow {
            delay(250L)
            emit("Appetizer")
            delay(1000L)
            emit("Main dish")
            delay(100L)
            emit("Dessert")
        }
        viewModelScope.launch {
            flow.onEach { message ->
                Log.i("CounterState", "$message is delivered")
            }
                //.buffer()
                .conflate()
                .collect { message ->
                    Log.i("CounterState", "Now eating $message ")
                    delay(1500L)
                    Log.i("CounterState", "Finished eating $message ")

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