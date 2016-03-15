package plumb.internal.codegen.validator

import plumb.internal.codegen.Model
import plumb.internal.codegen.error
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.tools.Diagnostic.Kind

object PlumbedValidator : Validator {

    override fun validate(element: Element, model: Model): Boolean {
        val messager = model.messager
        return if (element.kind != ElementKind.CLASS) {
            messager.error(classErrorMessage(element))
            false
        }
        else if (!element.modifiers.contains(Modifier.PUBLIC)) {
            messager.error(publicErrorMessage(element))
            false
        }
        else {
            true
        }
    }

    fun classErrorMessage(element: Element)
            = "Element ${element.simpleName} annotated with @Plumbed – must be a class."

    fun publicErrorMessage(element: Element)
            = "Element ${element.simpleName} annotated with @Plumbed – must be public."
}
