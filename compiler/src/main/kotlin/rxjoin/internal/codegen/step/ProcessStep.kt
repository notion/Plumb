package rxjoin.internal.codegen.step

import rxjoin.internal.codegen.Model

interface ProcessStep {
    fun process(model: Model)
}
