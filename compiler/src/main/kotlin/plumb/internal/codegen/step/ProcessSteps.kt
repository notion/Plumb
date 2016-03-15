package plumb.internal.codegen.step

import plumb.annotation.In
import plumb.annotation.Out
import plumb.annotation.Plumbed
import plumb.internal.codegen.Model
import plumb.internal.codegen.Model.PlumberModel
import plumb.internal.codegen.Model.PlumberModel.Entry
import plumb.internal.codegen.Model.PlumberModel.InOutRegistry
import plumb.internal.codegen.getValue
import plumb.internal.codegen.validator.PlumbedValidator
import plumb.internal.codegen.writer.PlumberMapImplWriter
import plumb.internal.codegen.writer.PlumberWriter
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement

object ProcessSteps {

    private val steps = listOf(
            ReadPlumbedClasses,
            ReadOutFields,
            ReadInFields,
            WritePlumbers,
            WritePlumberMapImpls)

    fun execute(roundEnv: RoundEnvironment, filer: Filer, messager: Messager) {
        val model = Model(roundEnv, filer, messager)
        steps.forEach {
            it.process(model)
        }
    }

    private object ReadPlumbedClasses : ProcessStep {
        override fun process(model: Model) {
            val elements = model.roundEnv.getElementsAnnotatedWith(Plumbed::class.java)
            elements.forEach { element ->
                if (PlumbedValidator.validate(element, model.messager)) {
                    val plumbed = element.getAnnotation(Plumbed::class.java)
                    val value = plumbed.getValue()
                    element.enclosedElements.first { it.asType() == value }
                            .let {
                                model.plumberEntries.add(PlumberModel(element as TypeElement, it))
                            }
                }
            }
        }
    }

    private object ReadOutFields : ProcessStep {
        override fun process(model: Model) {
            val outElements = model.roundEnv.getElementsAnnotatedWith(Out::class.java)
            outElements.forEach { out ->
                val annotationValue = out.getAnnotation(Out::class.java)?.value
                val enclosingElement = out.enclosingElement
                if (annotationValue != null) {
                    val entry = model.plumberEntries.firstOrNull { it.enclosing == enclosingElement || it.enclosed.asType() == enclosingElement.asType() }
                    // TODO assert uniqueness of @Out id
                    entry?.add(InOutRegistry(annotationValue, Entry(enclosingElement, out)))
                }
            }
        }
    }

    private object ReadInFields : ProcessStep {
        override fun process(model: Model) {
            val inElements = model.roundEnv.getElementsAnnotatedWith(In::class.java)
            inElements.forEach { inElement ->
                val annotationValue = inElement.getAnnotation(In::class.java)?.value
                val enclosingElement = inElement.enclosingElement
                if (annotationValue != null) {
                    val plumber = model.plumberEntries.firstOrNull { it.enclosing == enclosingElement || it.enclosed.asType() == enclosingElement.asType() }
                    val entry = plumber?.firstOrNull { it.id == annotationValue }
                    entry?.inEntries?.add(Entry(enclosingElement, inElement))
                }
            }
        }
    }

    private object WritePlumbers : ProcessStep {
        override fun process(model: Model) {
            model.plumberEntries.forEach { plumberModel ->
                PlumberWriter.write(plumberModel, model.filer)
            }
        }
    }

    private object WritePlumberMapImpls : ProcessStep {
        override fun process(model: Model) {
            if (model.plumberEntries.isNotEmpty()) {
                PlumberMapImplWriter.write(model, model.filer)
            }
        }
    }
}
