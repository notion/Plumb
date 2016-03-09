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

	private lateinit var typeUtils: Types
	private lateinit var elementUtils: Elements

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

	override fun process(annotations: MutableSet<out TypeElement>,
			roundEnv: RoundEnvironment): Boolean {
		val modelOperations = ModelOperations(Model(), filer)

		/*
			TODO -- Encapsulate everything in ModelOperations,
			Such that all we have to do is say modelOperations.process(), and we're off to the races.
			Or consider something else.
		 */

		val plumbed = roundEnv.getElementsAnnotatedWith(Plumbed::class.java)
		modelOperations.populatePlumbedMap(plumbed)

		val out = roundEnv.getElementsAnnotatedWith(Out::class.java)
		out.forEach {
			System.out.println("SWAG ${it.kind} ${it.enclosingElement}")
		}

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
