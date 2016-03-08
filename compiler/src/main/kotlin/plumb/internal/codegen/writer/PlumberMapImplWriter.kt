package plumb.internal.codegen.writer

import com.squareup.javapoet.*
import plumb.Plumber
import plumb.PlumberMap
import plumb.internal.codegen.Model
import java.util.*
import javax.annotation.processing.Filer
import javax.lang.model.element.Modifier

object PlumberMapImplWriter {

	val PKG_NAME = "plumb"
	val CLASS_NAME = "PlumberMapImpl"

	fun write(filer: Filer, model: Model) {
		val plumberMapImpl = TypeSpec.classBuilder(CLASS_NAME)
				.addSuperinterface(PlumberMap::class.java)
				.addModifiers(Modifier.PUBLIC)
				.addField(mapDeclaration())
				.addMethod(constructor(model))
				.addMethod(plumberForMethod())
				.build();

		val javaFile = JavaFile.builder(PKG_NAME, plumberMapImpl).build()
		javaFile.writeTo(filer)
	}

	private fun mapDeclaration(): FieldSpec {
		val mapClzName = ClassName.get(Map::class.java)
		val classClzName = ClassName.get(Class::class.java)
		val plumberClzName = ClassName.get(Plumber::class.java)
		val hashMapClzName = ClassName.get(HashMap::class.java)

		val parameterizedMap = ParameterizedTypeName.get(mapClzName, classClzName, plumberClzName)
		val parameterizedHashMap = ParameterizedTypeName.get(hashMapClzName, classClzName, plumberClzName)
		return FieldSpec.builder(parameterizedMap, "plumberMap")
				.addModifiers(Modifier.PRIVATE)
				.initializer("new \$T()", parameterizedHashMap)
				.build()
	}

	private fun constructor(model: Model): MethodSpec {
		val constructorBuilder = MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PUBLIC)

		model.plumbedMap.forEach { entry ->
			constructorBuilder.addStatement(
					"plumberMap.put(\$T.class, new ${entry.key.simpleName}_Plumber())", entry.key)
		}

		return constructorBuilder.build()
	}

	private fun plumberForMethod(): MethodSpec {
		val genericT = TypeVariableName.get("T")
		val genericR = TypeVariableName.get("R")

		val plumberForReturns = ParameterizedTypeName.get(ClassName.get(Plumber::class.java), genericT, genericR)
		val parameterSpec = ParameterSpec.builder(genericT, "t").build()

		return MethodSpec.methodBuilder("plumberFor")
				.addModifiers(Modifier.PUBLIC)
				.addTypeVariable(genericT)
				.addTypeVariable(genericR)
				.returns(plumberForReturns)
				.addAnnotation(Override::class.java)
				.addParameter(parameterSpec)
				.addStatement("return plumberMap.get(t.getClass())")
				.build()
	}
}
