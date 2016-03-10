package plumb.internal.codegen.writer

import com.squareup.javapoet.*
import plumb.Plumber
import rx.subscriptions.CompositeSubscription
import javax.annotation.processing.Filer
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

object PlumberWriter {

	val PKG_NAME = "plumb"
	val CLASS_SUFFIX = "_Plumber"

	fun write(filer: Filer, plumbed: TypeElement, plumbedTo: TypeElement, outsAndIns: Map<Element, Element>) {
		val plumberMapImpl = TypeSpec.classBuilder(className(plumbed))
				.addModifiers(Modifier.PUBLIC)
				.addSuperinterface(interfaceDeclaration(plumbed, plumbedTo))
				.addField(compositeSubscDeclaration())
				.addMethod(plumbMethod(plumbed, plumbedTo, outsAndIns))
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

	private fun compositeSubscDeclaration(): FieldSpec {
		val type = CompositeSubscription::class.java
		return FieldSpec.builder(type, "subscriptions", Modifier.PRIVATE)
				.initializer("new \$T()", type)
				.build()
	}

	private fun plumbMethod(plumbed: TypeElement, plumbedTo: TypeElement, outsAndIns: Map<Element, Element>): MethodSpec {
		val plumbedParam = ParameterSpec.builder(ClassName.get(plumbed), "plumbed").build()
		val plumbedToParam = ParameterSpec.builder(ClassName.get(plumbedTo), "plumbedTo").build()

		val builder = MethodSpec.methodBuilder("plumb")
				.addAnnotation(Override::class.java)
				.addModifiers(Modifier.PUBLIC)
				.addParameter(plumbedParam)
				.addParameter(plumbedToParam)

		outsAndIns.forEach {
			val outDecl = getQualifiedNameForElement(it.key, plumbed, plumbedTo)
			val inDecl = getQualifiedNameForElement(it.value, plumbed, plumbedTo)
			builder.addStatement("subscriptions.add(Utils.replicate($outDecl, $inDecl))")
		}

		return builder.build()
	}

	private fun demolishmethod(): MethodSpec {
		return MethodSpec.methodBuilder("demolish")
				.addAnnotation(Override::class.java)
				.addModifiers(Modifier.PUBLIC)
				.addStatement("subscriptions.unsubscribe()")
				.build()
	}

	private fun getQualifiedNameForElement(element: Element, plumbed: Element, plumbedTo: TypeElement): String {
		val prefix = if (element.enclosingElement == plumbed) {
			"plumbed"
		}
		else if (element.enclosingElement == plumbedTo) {
			"plumbedTo"
		}
		else {
			throw IllegalStateException()
		}

		return "$prefix.$element"
	}
}
