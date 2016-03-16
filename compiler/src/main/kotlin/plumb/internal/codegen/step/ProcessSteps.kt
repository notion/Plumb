package plumb.internal.codegen.step

import plumb.annotation.In
import plumb.annotation.Out
import plumb.annotation.Plumbed
import plumb.internal.codegen.Model
import plumb.internal.codegen.Model.PlumberModel
import plumb.internal.codegen.Model.PlumberModel.Entry
import plumb.internal.codegen.Model.PlumberModel.InOutRegistry
import plumb.internal.codegen.getValue
import plumb.internal.codegen.validator.InValidator
import plumb.internal.codegen.validator.OutValidator
import plumb.internal.codegen.validator.PlumbedValidator
import plumb.internal.codegen.writer.PlumberMapImplWriter
import plumb.internal.codegen.writer.PlumberWriter
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

object ProcessSteps {

    private val steps = listOf(
            ReadPlumbedClasses,
            ReadOutFields,
            ReadInFields,
            WritePlumbers,
            WritePlumberMapImpls)

    fun execute(roundEnv: RoundEnvironment, filer: Filer, messager: Messager, types: Types,
            elements: Elements) {
        val model = Model(roundEnv, filer, messager, types, elements)
        steps.forEach {
            it.process(model)
        }
    }

    private object ReadPlumbedClasses : ProcessStep {
        override fun process(model: Model) {
            val elements = model.roundEnv.getElementsAnnotatedWith(Plumbed::class.java)
            elements.forEach { plumbedElement ->
                if (PlumbedValidator.validate(plumbedElement, model)) {
                    val plumbed = plumbedElement.getAnnotation(Plumbed::class.java)
                    val value = plumbed.getValue()
                    plumbedElement.enclosedElements.first { it.asType() == value }
                            .let {
                                model.plumberEntries.add(
                                        PlumberModel(plumbedElement as TypeElement, it))
                            }
                }
            }
        }
    }

    private object ReadOutFields : ProcessStep {
        override fun process(model: Model) {
            val outElements = model.roundEnv.getElementsAnnotatedWith(Out::class.java)
            outElements.forEach { outElement ->
                if (OutValidator.validate(outElement, model)) {
                    val annotationValue = outElement.getAnnotation(Out::class.java).value
                    val enclosingElement = outElement.enclosingElement
                    val entry = model.plumberEntries.first { it.enclosing == enclosingElement || it.enclosed.asType() == enclosingElement.asType() }
                    entry.add(InOutRegistry(annotationValue, Entry(enclosingElement, outElement)))
                }
            }
        }
    }

    private object ReadInFields : ProcessStep {
        override fun process(model: Model) {
            val inElements = model.roundEnv.getElementsAnnotatedWith(In::class.java)
            inElements.forEach { inElement ->
                if (InValidator.validate(inElement, model)) {
                    val annotationValue = inElement.getAnnotation(In::class.java).value
                    val enclosingElement = inElement.enclosingElement
                    val plumber = model.plumberEntries.first { it.enclosing == enclosingElement || it.enclosed.asType() == enclosingElement.asType() }
                    val entry = plumber.first { it.id == annotationValue }
                    entry.inEntries.add(Entry(enclosingElement, inElement))
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
