package plumb.internal.codegen

import java.util.*
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

class Model {
    val plumbedMap = HashMap<TypeElement, TypeElement>()
    val outsAndInsForPlumbedClass = HashMap<TypeElement, HashMap<Element, Element>>()
}
