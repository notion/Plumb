package rxjoin.internal.codegen.writer

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.JavaFile.Builder
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.TypeVariableName
import rxjoin.Joiner
import rxjoin.JoinerMap
import rxjoin.internal.codegen.Model
import rxjoin.internal.codegen.Model.JoinerModel
import java.util.HashMap
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.Modifier.PUBLIC

object JoinerMapImplWriter : AbsWriter<Model>() {

    val CLASS_NAME = "JoinerMapImpl"

    override fun _write(model: Model): Builder {
        val joinerMapImpl = TypeSpec.classBuilder(
                CLASS_NAME)
                .addSuperinterface(JoinerMap::class.java)
                .addModifiers(PUBLIC)
                .addField(mapDeclaration())
                .addMethod(constructor(
                        model.joinerModels))
                .addMethod(joinerForMethod())
                .build();

        return JavaFile.builder(PKG_NAME, joinerMapImpl)
    }

    private fun mapDeclaration(): FieldSpec {
        val mapClzName = ClassName.get(Map::class.java)
        val classClzName = ClassName.get(Class::class.java)
        val joinerClzName = ClassName.get(Joiner::class.java)
        val hashMapClzName = ClassName.get(HashMap::class.java)

        val parameterizedMap = ParameterizedTypeName.get(mapClzName, classClzName, joinerClzName)
        val parameterizedHashMap = ParameterizedTypeName.get(hashMapClzName, classClzName,
                joinerClzName)
        return FieldSpec.builder(parameterizedMap, "joinerMap")
                .addModifiers(PRIVATE)
                .initializer("new \$T()", parameterizedHashMap)
                .build()
    }

    private fun constructor(joinerModels: List<JoinerModel>): MethodSpec {
        val constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(PUBLIC)

        joinerModels.sortedBy { it.enclosing.simpleName.toString() }.forEach { joiner ->
            constructorBuilder.addStatement(
                    "joinerMap.put(\$T.class, new ${joiner.enclosing.simpleName}_Joiner())",
                    joiner.enclosing)
        }

        return constructorBuilder.build()
    }

    private fun joinerForMethod(): MethodSpec {
        val genericT = TypeVariableName.get("T")
        val genericR = TypeVariableName.get("R")

        val joinerForReturns = ParameterizedTypeName.get(ClassName.get(Joiner::class.java),
                genericT, genericR)
        val parameterSpec = ParameterSpec.builder(genericT, "t").build()

        return MethodSpec.methodBuilder("joinerFor")
                .addModifiers(PUBLIC)
                .addTypeVariable(genericT)
                .addTypeVariable(genericR)
                .returns(joinerForReturns)
                .addAnnotation(Override::class.java)
                .addParameter(parameterSpec)
                .addStatement("return joinerMap.get(t.getClass())")
                .build()
    }
}
