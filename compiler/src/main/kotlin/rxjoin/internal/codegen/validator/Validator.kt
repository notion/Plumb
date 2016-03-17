package rxjoin.internal.codegen.validator

import rxjoin.internal.codegen.Model
import javax.lang.model.element.Element

interface Validator {
    fun validate(element: Element, model: Model): Boolean
}
