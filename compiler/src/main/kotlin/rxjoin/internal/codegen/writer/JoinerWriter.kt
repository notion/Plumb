package rxjoin.internal.codegen.writer

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.JavaFile.Builder
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeSpec
import rx.subscriptions.CompositeSubscription
import rxjoin.Joiner
import rxjoin.internal.codegen.Model
import rxjoin.internal.codegen.Model.JoinerModel.Entry
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.TypeElement

object JoinerWriter : AbsWriter<Model.JoinerModel>() {

    val CLASS_SUFFIX = "_Joiner"

    override fun _write(model: Model.JoinerModel): Builder {
        val joinerMapImpl = TypeSpec.classBuilder(
                className(model.enclosing))
                .addModifiers(PUBLIC)
                .addSuperinterface(
                        interfaceDeclaration(model.enclosing,
                                model.enclosed))
                .addField(compositeSubscDeclaration())
                .addMethod(joinMethod(model))
                .addMethod(demolishMethod())
                .build();

        return JavaFile.builder(PKG_NAME, joinerMapImpl)
    }

    private fun className(
            joinedElement: Element) = joinedElement.simpleName.toString() + CLASS_SUFFIX

    private fun interfaceDeclaration(joinedElement: TypeElement,
            joinedToElement: Element): ParameterizedTypeName {
        val joinerClzName = ClassName.get(Joiner::class.java)
        val joinedClzName = ClassName.get(joinedElement)
        val joinedToClzName = ClassName.get(joinedToElement.asType())
        return ParameterizedTypeName.get(joinerClzName, joinedClzName, joinedToClzName)
    }

    private fun compositeSubscDeclaration(): FieldSpec {
        val type = CompositeSubscription::class.java
        return FieldSpec.builder(type, "subscriptions", PRIVATE)
                .build()
    }

    private fun joinMethod(model: Model.JoinerModel): MethodSpec {
        val joinedParam = ParameterSpec.builder(ClassName.get(model.enclosing),
                "joined").build()
        val joinedToParam = ParameterSpec.builder(ClassName.get(model.enclosed.asType()),
                "joinedTo").build()

        val builder = MethodSpec.methodBuilder("join")
                .addAnnotation(Override::class.java)
                .addModifiers(PUBLIC)
                .addParameter(joinedParam)
                .addParameter(joinedToParam)

        builder.addCode("" +
                "if (subscriptions != null && !subscriptions.isUnsubscribed()) {\n" +
                "   subscriptions.unsubscribe();\n" +
                "}\n"
        )

        builder.addStatement("subscriptions = new \$T()", CompositeSubscription::class.java)

        model.sortedBy { it.id }.forEach {
            val outEntry = it.outEntry
            it.inEntries.forEach { inEntry ->
                val outDecl = getQualifiedNameForElement(
                        outEntry, model)
                val inDecl = getQualifiedNameForElement(
                        inEntry, model)
                builder.addStatement("subscriptions.add(Utils.replicate($outDecl, $inDecl))")
            }
        }

        return builder.build()
    }

    private fun demolishMethod(): MethodSpec {
        return MethodSpec.methodBuilder("demolish")
                .addAnnotation(Override::class.java)
                .addModifiers(PUBLIC)
                .addStatement("subscriptions.unsubscribe()")
                .build()
    }

    private fun getQualifiedNameForElement(entry: Entry, model: Model.JoinerModel): String {
        val prefix = if (entry.enclosingElement.asType() == model.enclosing.asType()) {
            "joined"
        }
        else if (entry.enclosingElement.asType() == model.enclosed.asType()) {
            "joinedTo"
        }
        else {
            throw IllegalStateException()
        }

        return "$prefix.${entry.element}"
    }
}
