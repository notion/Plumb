package plumb.internal.codegen.validator

import javax.annotation.processing.Messager
import javax.lang.model.element.Element

interface Validator {

    fun validate(element: Element, messager: Messager): Boolean
}
