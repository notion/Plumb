package plumb.internal.codegen

import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

class Model(val roundEnv: RoundEnvironment) {
    val plumberEntries = mutableListOf<PlumberModel>()

    class PlumberModel(val enclosing: TypeElement, val enclosed: TypeElement) {
        fun getAllElements(): List<TypeElement> {
            return mutableListOf(enclosed, enclosing)
        }

        val registry = mutableListOf<InOutRegistry>()

        class Entry(val enclosingElement: TypeElement, val element: Element)
        class InOutRegistry(val id: String, val outEntry: Entry, val inEntries: MutableList<Entry> = mutableListOf())
    }
}
