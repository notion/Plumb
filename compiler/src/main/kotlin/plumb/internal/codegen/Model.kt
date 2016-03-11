package plumb.internal.codegen

import plumb.internal.codegen.Model.PlumberModel.InOutRegistry
import javax.annotation.processing.Filer
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

class Model(val roundEnv: RoundEnvironment, val filer: Filer) {
    val plumberEntries = mutableListOf<PlumberModel>()

    class PlumberModel(
            val enclosing: TypeElement,
            val enclosed: TypeElement,
            private val registry: MutableList<InOutRegistry> = mutableListOf()) : MutableList<InOutRegistry> by registry {

        fun getAllElements(): List<TypeElement> {
            return listOf(enclosed, enclosing)
        }

        class Entry(val enclosingElement: TypeElement, val element: Element)
        class InOutRegistry(val id: String, val outEntry: Entry, val inEntries: MutableList<Entry> = mutableListOf())
    }
}
