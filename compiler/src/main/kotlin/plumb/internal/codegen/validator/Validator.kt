package plumb.internal.codegen.validator

import plumb.internal.codegen.Model
import javax.lang.model.element.Element

interface  Validator {
    fun validate(element: Element, model: Model): Boolean
}
