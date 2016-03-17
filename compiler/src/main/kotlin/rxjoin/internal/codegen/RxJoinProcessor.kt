package rxjoin.internal.codegen

import rxjoin.annotation.In
import rxjoin.annotation.Joined
import rxjoin.annotation.Out
import rxjoin.internal.codegen.step.ProcessSteps
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

class RxJoinProcessor : AbstractProcessor() {

    private lateinit var filer: Filer
    private lateinit var messager: Messager
    private lateinit var types: Types
    private lateinit var elements: Elements

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported();
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(
                In::class.java.name,
                Out::class.java.name,
                Joined::class.java.name)
    }

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        messager = processingEnv.messager
        filer = processingEnv.filer
        types = processingEnv.typeUtils
        elements = processingEnv.elementUtils
    }

    override fun process(annotations: MutableSet<out TypeElement>,
            roundEnv: RoundEnvironment): Boolean {
        ProcessSteps.execute(roundEnv, filer, messager, types, elements)
        return false
    }

}
