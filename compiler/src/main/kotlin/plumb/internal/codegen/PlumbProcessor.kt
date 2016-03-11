package plumb.internal.codegen

import plumb.annotation.In
import plumb.annotation.Out
import plumb.annotation.Plumbed
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror

class PlumbProcessor : AbstractProcessor() {

	private lateinit var filer: Filer
	private lateinit var messager: Messager

	override fun getSupportedSourceVersion(): SourceVersion {
		return SourceVersion.latestSupported();
	}

	override fun getSupportedAnnotationTypes(): Set<String> {
		return setOf(
				In::class.java.name,
				Out::class.java.name,
				Plumbed::class.java.name)
	}

	override fun init(processingEnv: ProcessingEnvironment) {
		super.init(processingEnv)
		messager = processingEnv.messager
		filer = processingEnv.filer
	}

	override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
		val modelOperations = ModelOperations(Model(), roundEnv, filer)
		modelOperations.populatePlumbedMap()
		modelOperations.populateOutsAndIns()
		modelOperations.generatePlumbers()
		modelOperations.generatePlumberMapImpl()
		return false
	}

}

fun Plumbed.getValue(): TypeMirror {
	try {
		return value as TypeMirror
	}
	catch (e: MirroredTypeException) {
		return e.typeMirror
	}
}
