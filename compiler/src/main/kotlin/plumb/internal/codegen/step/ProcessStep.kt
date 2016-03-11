package plumb.internal.codegen.step

import plumb.internal.codegen.Model

interface ProcessStep {
    fun process(model: Model)
}
