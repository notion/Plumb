package plumb.internal.codegen

import plumb.internal.codegen.Model.PlumberModel.InOutRegistry
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

class Model(val roundEnv: RoundEnvironment, val filer: Filer,
        val messager: Messager, val types: Types, val elements: Elements,
        val plumberEntries: MutableList<PlumberModel> = mutableListOf()) {

    class PlumberModel(
            val enclosing: TypeElement,
            val enclosed: Element,
            val registry: MutableList<InOutRegistry> = mutableListOf()) : MutableList<InOutRegistry> by registry {

        fun getAllElements(): List<Element> {
            return listOf(enclosed, enclosing)
        }

        class Entry(val enclosingElement: Element, val element: Element)
        class InOutRegistry(val id: String, val outEntry: Entry, val inEntries: MutableList<Entry> = mutableListOf())
    }
}
