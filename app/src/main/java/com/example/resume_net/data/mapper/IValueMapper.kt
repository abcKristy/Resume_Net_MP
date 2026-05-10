package com.example.resume_net.data.mapper

import org.pytorch.IValue
import org.pytorch.Tensor

fun LongArray.toTensor(): Tensor {
    return Tensor.fromBlob(this, longArrayOf(1, this.size.toLong()))
}

fun IValue.toScore(): Float {
    val tensor = this.toTensor()
    val score = tensor.dataAsFloatArray[0]
    return score.coerceIn(1f, 5f)
}

fun IValue.toProbs(): FloatArray {
    val tensor = this.toTensor()
    return tensor.dataAsFloatArray
}