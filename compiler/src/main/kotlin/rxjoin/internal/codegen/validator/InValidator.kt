package rxjoin.internal.codegen.validator

import rx.subjects.Subject
import rxjoin.annotation.In
import rxjoin.internal.codegen.Model
import rxjoin.internal.codegen.Model.JoinerModel
import rxjoin.internal.codegen.error
import javax.annotation.processing.Messager
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind.FIELD
import javax.lang.model.type.TypeKind.DECLARED
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

object InValidator : Validator {

    private lateinit var types: Types
    private lateinit var elements: Elements
    private lateinit var messager: Messager

    fun elementNotFieldError(element: Element) =
            "@In-annotated element ${element.simpleName} must be a FIELD. It is a ${element.kind}"

    fun fieldNotSubjectError(element: Element) =
            "@In-annotated element ${element.simpleName} must extend Subject<*, *>."

    fun fieldNotParameterizedSubject(element: Element) =
            "@In-annotated element ${element.simpleName} must be declared Subject<T, R>."

    fun noCorrespondingRegistryError(element: Element) =
            "@In-annotated element ${element.simpleName} has no corresponding @Out-annotated element."

    val subjectQualifiedName = Subject::class.java.canonicalName

    override fun validate(element: Element, model: Model): Boolean {
        types = model.types
        elements = model.elements
        messager = model.messager

        val isValidField = when (element.kind) {
            FIELD -> {
                validateField(element)
            }
            else -> {
                messager.error(
                        elementNotFieldError(element))
                false
            }
        }
        return isValidField && validateMatchingJoiner(element,
                model.joinerModels)
    }

    private fun validateField(element: Element): Boolean {
        return if (element.asType().kind == DECLARED) {
            val type = element.asType()
            if (types.isAssignable(type,
                    types.getDeclaredType(
                            elements.getTypeElement(
                                    subjectQualifiedName),
                            types.getWildcardType(null, null),
                            types.getWildcardType(null, null)))) {
                true
            }
            else {
                messager.error(
                        fieldNotSubjectError(element))
                false
            }
        }
        else {
            messager.error(
                    fieldNotParameterizedSubject(element))
            false
        }
    }

    private fun validateMatchingJoiner(element: Element,
            models: MutableList<JoinerModel>): Boolean {
        val annotationValue = element.getAnnotation(In::class.java).value
        val matchingRegistry = models.firstOrNull { it.registry.firstOrNull { it.id == annotationValue } != null }

        return if (matchingRegistry != null) {
            true
        }
        else {
            messager.error(
                    noCorrespondingRegistryError(element))
            false
        }
    }
}
