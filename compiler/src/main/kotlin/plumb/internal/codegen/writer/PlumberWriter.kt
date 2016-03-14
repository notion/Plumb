package plumb.internal.codegen.writer

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeSpec
import plumb.Plumber
import plumb.internal.codegen.Model.PlumberModel
import rx.subscriptions.CompositeSubscription
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

object PlumberWriter : AbsWriter<PlumberModel>() {

    val PKG_NAME = "plumb"
    val CLASS_SUFFIX = "_Plumber"

    override fun _write(model: PlumberModel): JavaFile.Builder {
        val plumberMapImpl = TypeSpec.classBuilder(className(model.enclosing))
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(
                        interfaceDeclaration(model.enclosing, model.enclosed))
                .addField(compositeSubscDeclaration())
                .addMethod(plumbMethod(model))
                .addMethod(demolishMethod())
                .build();

        return JavaFile.builder(PKG_NAME, plumberMapImpl)
    }

    private fun className(
            plumbedElement: Element) = plumbedElement.simpleName.toString() + CLASS_SUFFIX

    private fun interfaceDeclaration(plumbed: TypeElement,
            plumbedTo: Element): ParameterizedTypeName {
        val plumberClzName = ClassName.get(Plumber::class.java)
        val plumbedClzName = ClassName.get(plumbed)
        val plumbedToClzName = ClassName.get(plumbedTo.asType())
        return ParameterizedTypeName.get(plumberClzName, plumbedClzName, plumbedToClzName)
    }

    private fun compositeSubscDeclaration(): FieldSpec {
        val type = CompositeSubscription::class.java
        return FieldSpec.builder(type, "subscriptions", Modifier.PRIVATE)
                .build()
    }

    private fun plumbMethod(plumberModel: PlumberModel): MethodSpec {
        val plumbedParam = ParameterSpec.builder(ClassName.get(plumberModel.enclosing),
                "plumbed").build()
        val plumbedToParam = ParameterSpec.builder(ClassName.get(plumberModel.enclosed.asType()),
                "plumbedTo").build()

        val builder = MethodSpec.methodBuilder("plumb")
                .addAnnotation(Override::class.java)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(plumbedParam)
                .addParameter(plumbedToParam)

        builder.addCode("" +
        "if (subscriptions != null && !subscriptions.isUnsubscribed()) {\n" +
        "   subscriptions.unsubscribe();\n" +
        "}"
        )

        builder.addStatement("subscriptions = new \$T()", CompositeSubscription::class.java)

        plumberModel.sortedBy { it.id }.forEach {
            val outEntry = it.outEntry
            it.inEntries.forEach { inEntry ->
                val outDecl = getQualifiedNameForElement(outEntry, plumberModel)
                val inDecl = getQualifiedNameForElement(inEntry, plumberModel)
                builder.addStatement("subscriptions.add(Utils.replicate($outDecl, $inDecl))")
            }
        }

        return builder.build()
    }

    private fun demolishMethod(): MethodSpec {
        return MethodSpec.methodBuilder("demolish")
                .addAnnotation(Override::class.java)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("subscriptions.unsubscribe()")
                .build()
    }

    private fun getQualifiedNameForElement(entry: PlumberModel.Entry, model: PlumberModel): String {
        val prefix = if (entry.enclosingElement.asType() == model.enclosing.asType()) {
            "plumbed"
        }
        else if (entry.enclosingElement.asType() == model.enclosed.asType()) {
            "plumbedTo"
        }
        else {
            throw IllegalStateException()
        }

        return "$prefix.${entry.element}"
    }
}
