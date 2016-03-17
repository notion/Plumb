package rxjoin.internal.codegen.validator

import rx.Observable
import rxjoin.annotation.Out
import rxjoin.internal.codegen.Model
import rxjoin.internal.codegen.Model.JoinerModel
import rxjoin.internal.codegen.error
import javax.annotation.processing.Messager
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind.FIELD
import javax.lang.model.element.ElementKind.METHOD
import javax.lang.model.type.ExecutableType
import javax.lang.model.type.TypeKind.DECLARED
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

object OutValidator : Validator {

    private lateinit var types: Types
    private lateinit var elements: Elements
    private lateinit var messager: Messager

    // Error messages

    fun fieldNotObservableError(
            element: Element) = "@Out-annotated Field ${element.simpleName} is not an Observable."

    fun fieldNotParameterizedObservable(
            element: Element) = "@Out-annotated Field ${element.simpleName} is not a parameterized Observable."

    fun methodDoesNotReturnObservableError(
            element: Element) = "@Out-annotated Field ${element.simpleName} is not an Observable."

    fun methodHasArgumentsError(
            element: Element) = "@Out-annotated Method ${element.simpleName} has arguments. Must have zero arguments."

    fun elementNotFieldOrMethodError(
            element: Element) = "@Out-annotated Element ${element.simpleName} is a ${element.kind}. Must be FIELD or METHOD."

    fun duplicateOutValueError(
            annotationValue: String) = "Multiple @Out-annotated Elements with value $annotationValue."

    fun notEnclosedByJoinedClass(element: Element)
            = "@Out-annotated Element ${element.simpleName} is not enclosed by @Joined class or class being joined to."

    private val observableQualifiedName = Observable::class.java.canonicalName

    override fun validate(element: Element, model: Model): Boolean {
        this.types = model.types
        this.elements = model.elements
        this.messager = model.messager
        val joinModels = model.joinerModels

        val fieldOrMethodValid = when (element.kind) {
            FIELD -> {
                validateField(element)
            }
            METHOD -> {
                validateMethod(element)
            }
            else -> {
                messager.error(
                        elementNotFieldOrMethodError(element))
                false
            }
        }

        val joinModelsValid = validateJoinModels(element,
                joinModels)

        return fieldOrMethodValid && joinModelsValid
    }

    // Helpers

    // Field must be a DeclaredType Observable (e.g. Observable<Integer> vs Observable, String).

    private fun validateField(element: Element): Boolean {
        return if (element.asType().kind == DECLARED) {
            val type = element.asType()
            if (!types.isAssignable(type,
                    types.getDeclaredType(
                            elements.getTypeElement(
                                    observableQualifiedName),
                            types.getWildcardType(null, null)))) {
                messager.error(
                        fieldNotObservableError(element))
                false
            }
            else {
                true
            }
        }
        else {
            messager.error(
                    fieldNotParameterizedObservable(element))
            false
        }
    }

    // Method must return DeclaredType Observable (e.g. Observable<Integer> vs Observable, String).
    // Method must have zero parameters.

    private fun validateMethod(element: Element): Boolean {
        val executableType = element.asType() as ExecutableType
        val typeValid = if (executableType.kind == DECLARED) {
            val type = element.asType()
            if (!types.isAssignable(type,
                    types.getDeclaredType(
                            elements.getTypeElement(
                                    observableQualifiedName),
                            types.getWildcardType(null, null)))) {
                messager.error(
                        fieldNotObservableError(element))
                false
            }
            else {
                true
            }
        }
        else {
            val returnType = executableType.returnType
            if (returnType.toString() != observableQualifiedName) {
                messager.error(
                        methodDoesNotReturnObservableError(
                                element))
            }
            else {
                messager.error(
                        fieldNotParameterizedObservable(
                                element))
            }
            false
        }

        val hasZeroParams = if (executableType.parameterTypes.size != 0) {
            messager.error(
                    methodHasArgumentsError(element))
            false
        }
        else {
            true
        }

        return typeValid && hasZeroParams
    }

    // When adding a new @Out-annotated element, its annotation value must be unique relative
    // to previously recorded @Out-annotated elements.

    private fun validateJoinModels(element: Element, models: MutableList<JoinerModel>): Boolean {
        val entry = models
                .firstOrNull {
                    it.enclosing == element.enclosingElement
                            || it.enclosed.asType() == element.enclosingElement.asType()
                }

        if (entry == null) {
            messager.error(
                    notEnclosedByJoinedClass(element))
            return false
        }

        val inOutRegistries = entry.registry

        // Uniqueness amongst @Outs
        val annotationValue = element.getAnnotation(Out::class.java).value
        if (inOutRegistries.firstOrNull { it.id == annotationValue } != null) {
            messager.error(
                    duplicateOutValueError(annotationValue))
            return false
        }
        return true
    }
}
