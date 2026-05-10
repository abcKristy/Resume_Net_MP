package com.example.resume_net.data.mapper

import org.pytorch.IValue

/**
 * Маппинг выходных данных модели PyTorch в Kotlin-объекты.
 *
 * Модель возвращает Tuple из двух тензоров:
 * - outputs[0]: Tensor shape [1, 1] — оценка резюме 1-5
 * - outputs[1]: Tensor shape [1, 20] — вероятности 20 тегов
 */
object IValueMapper {

    /**
     * Извлекает оценку резюме из выходного IValue.
     *
     * @param output IValue от model.forward() — ожидается Tuple
     * @return оценка от 1.0 до 5.0
     */
    fun toScore(output: IValue): Float {
        // Модель возвращает Tuple, а не одиночный Tensor!
        val tuple = output.toTuple()
        val scoreTensor = tuple[0].toTensor()
        return scoreTensor.dataAsFloatArray[0]
    }

    /**
     * Извлекает вероятности тегов из выходного IValue.
     *
     * @param output IValue от model.forward() — ожидается Tuple
     * @return массив из 20 вероятностей [0, 1]
     */
    fun toProbs(output: IValue): FloatArray {
        val tuple = output.toTuple()
        val probsTensor = tuple[1].toTensor()
        return probsTensor.dataAsFloatArray
    }

    /**
     * Извлекает полный результат инференса.
     *
     * @param output IValue от model.forward()
     * @return Pair(оценка, вероятности)
     */
    fun toResult(output: IValue): Pair<Float, FloatArray> {
        val score = toScore(output)
        val probs = toProbs(output)
        return Pair(score, probs)
    }
}