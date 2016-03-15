package plumb.internal.codegen.validator

import javax.annotation.processing.Messager
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.tools.Diagnostic.Kind

object PlumbedValidator : Validator {
    override fun validate(element: Element, messager: Messager): Boolean {
        return if (element.kind != ElementKind.CLASS) {
            messager.printMessage(Kind.ERROR,
                    "Element ${element.simpleName} annotated with @Plumbed – must be a class.")
            false
        }
        else if (!element.modifiers.contains(Modifier.PUBLIC)) {
            messager.printMessage(Kind.ERROR,
                    "Element ${element.simpleName} annotated with @Plumbed – must be public.")
            false
        }
        else {
            true
        }
    }
}
