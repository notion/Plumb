package plumb.internal.codegen

import plumb.annotation.Plumbed
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement

object ProcessSteps {

    fun execute(roundEnv: RoundEnvironment) {
        val model = Model(roundEnv)
        steps.forEach {
            it.process(model)
        }
    }

    private val steps = listOf(ReadPlumbedClassesStep)

    private object ReadPlumbedClassesStep : ProcessStep {
        override fun process(model: Model) {
            val elements = model.roundEnv.getElementsAnnotatedWith(Plumbed::class.java)
            elements.forEach { element ->
                //TODO assert Plumbed item is a Type
                if (element is TypeElement) {
                    val plumbed = element.getAnnotation(Plumbed::class.java)
                    val value = plumbed.getValue()
                    element.enclosedElements.first { it.asType() == value }
                            .let {
                                model.plumbedClassesMap.put(element, it as TypeElement)
                            }
                }
            }
        }
    }
}
