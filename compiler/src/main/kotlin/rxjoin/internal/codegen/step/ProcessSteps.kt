package rxjoin.internal.codegen.step

import rxjoin.annotation.In
import rxjoin.annotation.Joined
import rxjoin.annotation.Out
import rxjoin.internal.codegen.Model
import rxjoin.internal.codegen.Model.JoinerModel
import rxjoin.internal.codegen.Model.JoinerModel.Entry
import rxjoin.internal.codegen.Model.JoinerModel.InOutRegistry
import rxjoin.internal.codegen.getValue
import rxjoin.internal.codegen.validator.InValidator
import rxjoin.internal.codegen.validator.JoinedValidator
import rxjoin.internal.codegen.validator.OutValidator
import rxjoin.internal.codegen.writer.JoinerMapImplWriter
import rxjoin.internal.codegen.writer.JoinerWriter
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

object ProcessSteps {

    private val steps = listOf(
            ProcessSteps.ReadJoinedClasses,
            ProcessSteps.ReadOutFields,
            ProcessSteps.ReadInFields,
            ProcessSteps.WriteJoiners,
            ProcessSteps.WriteJoinerMapImpls)

    fun execute(roundEnv: RoundEnvironment, filer: Filer, messager: Messager, types: Types,
            elements: Elements) {
        val model = Model(roundEnv, filer, messager, types, elements)
        ProcessSteps.steps.forEach {
            it.process(model)
        }
    }

    private object ReadJoinedClasses : ProcessStep {
        override fun process(model: Model) {
            val elements = model.roundEnv.getElementsAnnotatedWith(Joined::class.java)
            elements.forEach { joinedElement ->
                if (JoinedValidator.validate(joinedElement, model)) {
                    val joined = joinedElement.getAnnotation(Joined::class.java)
                    val value = joined.getValue()
                    joinedElement.enclosedElements.first { it.asType() == value }
                            .let {
                                model.joinerModels.add(
                                        JoinerModel(joinedElement as TypeElement, it))
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
                    val entry = model.joinerModels.first { it.enclosing == enclosingElement || it.enclosed.asType() == enclosingElement.asType() }
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
                    val joiner = model.joinerModels.first { it.enclosing == enclosingElement || it.enclosed.asType() == enclosingElement.asType() }
                    val entry = joiner.first { it.id == annotationValue }
                    entry.inEntries.add(Entry(enclosingElement, inElement))
                }
            }
        }
    }

    private object WriteJoiners : ProcessStep {
        override fun process(model: Model) {
            model.joinerModels.forEach { joinerModel ->
                JoinerWriter.write(joinerModel, model.filer)
            }
        }
    }

    private object WriteJoinerMapImpls : ProcessStep {
        override fun process(model: Model) {
            if (model.joinerModels.isNotEmpty()) {
                JoinerMapImplWriter.write(model, model.filer)
            }
        }
    }
}
