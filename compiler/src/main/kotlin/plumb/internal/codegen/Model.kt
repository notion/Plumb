package plumb.internal.codegen

import java.util.*
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement

class Model(val roundEnv: RoundEnvironment) {
    val plumbedClassesMap = HashMap<TypeElement, TypeElement>()
}
