package plumb.internal.codegen

import plumb.annotation.In
import plumb.annotation.Out
import plumb.annotation.Plumbed
import plumb.internal.codegen.Model.PlumberModel
import plumb.internal.codegen.Model.PlumberModel.Entry
import plumb.internal.codegen.Model.PlumberModel.InOutRegistry
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement

object ProcessSteps {

    private val steps = listOf(
            ReadPlumbedClasses,
            ReadOutFields,
            ReadInFields)

    fun execute(roundEnv: RoundEnvironment) {
        val model = Model(roundEnv)
        steps.forEach {
            it.process(model)
        }
    }

    private object ReadPlumbedClasses : ProcessStep {
        override fun process(model: Model) {
            val elements = model.roundEnv.getElementsAnnotatedWith(Plumbed::class.java)
            elements.forEach { element ->
                //TODO assert Plumbed item is a Type
                if (element is TypeElement) {
                    val plumbed = element.getAnnotation(Plumbed::class.java)
                    val value = plumbed.getValue()
                    element.enclosedElements.first { it.asType() == value }
                            .let {
                                model.plumberEntries.add(PlumberModel(element, it as TypeElement))
                            }
                }
            }
        }
    }

    private object ReadOutFields : ProcessStep {
        override fun process(model: Model) {
            model.plumberEntries.forEach { plumber ->
                plumber.getAllElements().forEach { encapsulatingClass ->
                    encapsulatingClass.enclosedElements
                            .forEach { field ->
                                val annotation = field.getAnnotation(Out::class.java)
                                if (annotation != null) {
                                    val id = annotation.value
                                    // TODO validate OUT uniqueness
                                    plumber.add(
                                            InOutRegistry(id, Entry(encapsulatingClass, field)))
                                }
                            }
                }
            }
        }
    }

    private object ReadInFields : ProcessStep {
        override fun process(model: Model) {
            model.plumberEntries.forEach { plumber ->
                plumber.getAllElements().forEach { encapsulatingClass ->
                    encapsulatingClass.enclosedElements
                            .forEach { field ->
                                val annotation = field.getAnnotation(In::class.java)
                                if (annotation != null) {
                                    val id = annotation.value
                                    val registry = plumber.firstOrNull { it.id == id }
                                    if (registry == null) {
                                        // TODO error condition
                                    }
                                    else {
                                        registry.inEntries.add(Entry(encapsulatingClass, field))
                                    }
                                }
                            }
                }
            }
        }
    }
}
