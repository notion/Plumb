package plumb.internal.codegen

import com.google.auto.service.AutoService
import plumb.annotation.In
import plumb.annotation.Out
import plumb.annotation.Plumbed
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

@AutoService(Processor::class)
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
		return false
	}

}
