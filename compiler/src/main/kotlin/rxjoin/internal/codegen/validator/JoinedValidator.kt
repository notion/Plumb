package rxjoin.internal.codegen.validator

import rxjoin.internal.codegen.Model
import rxjoin.internal.codegen.error
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind.CLASS
import javax.lang.model.element.Modifier.PUBLIC

object JoinedValidator : Validator {

    override fun validate(element: Element, model: Model): Boolean {
        val messager = model.messager
        return if (element.kind != CLASS) {
            messager.error(classErrorMessage(element))
            false
        }
        else if (!element.modifiers.contains(PUBLIC)) {
            messager.error(publicErrorMessage(element))
            false
        }
        else {
            true
        }
    }

    fun classErrorMessage(element: Element)
            = "Element ${element.simpleName} annotated with @Joined – must be a class."

    fun publicErrorMessage(element: Element)
            = "Element ${element.simpleName} annotated with @Joined – must be public."
}
