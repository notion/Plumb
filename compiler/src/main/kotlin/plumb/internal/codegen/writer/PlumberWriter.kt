package plumb.internal.codegen.writer

import com.squareup.javapoet.*
import plumb.Plumber
import javax.annotation.processing.Filer
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

object PlumberWriter {

	val PKG_NAME = "plumb"
	val CLASS_SUFFIX = "_Plumber"

	fun write(filer: Filer, plumbed: TypeElement, plumbedTo: TypeElement) {
		val plumberMapImpl = TypeSpec.classBuilder(className(plumbed))
				.addModifiers(Modifier.PUBLIC)
				.addSuperinterface(interfaceDeclaration(plumbed, plumbedTo))
				.addMethod(plumbMethod(plumbed, plumbedTo))
				.addMethod(demolishmethod())
				.build();

		val javaFile = JavaFile.builder(PKG_NAME, plumberMapImpl).build()
		javaFile.writeTo(filer)
	}

	private fun className(plumbedElement: Element) = plumbedElement.simpleName.toString() + CLASS_SUFFIX

	private fun interfaceDeclaration(plumbed: TypeElement, plumbedTo: TypeElement): ParameterizedTypeName {
		val plumberClzName = ClassName.get(Plumber::class.java)
		val plumbedClzName = ClassName.get(plumbed)
		val plumbedToClzName = ClassName.get(plumbedTo)
		return ParameterizedTypeName.get(plumberClzName, plumbedClzName, plumbedToClzName)
	}

	private fun plumbMethod(plumbed: TypeElement, plumbedTo: TypeElement): MethodSpec {
		val plumbedParam = ParameterSpec.builder(ClassName.get(plumbed), "plumbed").build()
		val plumbedToParam = ParameterSpec.builder(ClassName.get(plumbedTo), "plumbedTo").build()

		return MethodSpec.methodBuilder("plumb")
				.addAnnotation(Override::class.java)
				.addModifiers(Modifier.PUBLIC)
				.addParameter(plumbedParam)
				.addParameter(plumbedToParam)
				.build()
	}

	private fun demolishmethod(): MethodSpec {
		return MethodSpec.methodBuilder("demolish")
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(Override::class.java)
				.addModifiers(Modifier.PUBLIC)
				.build()
	}
}
