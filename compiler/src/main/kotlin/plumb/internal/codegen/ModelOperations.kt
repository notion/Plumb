package plumb.internal.codegen

import plumb.annotation.Plumbed
import plumb.internal.codegen.writer.PlumberMapImplWriter
import plumb.internal.codegen.writer.PlumberWriter
import javax.annotation.processing.Filer
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

class ModelOperations(val model: Model, private val filer: Filer) {

	val populatePlumbedMap = { elements: Set<Element> ->
		elements.forEach { element ->
			val plumbed = element.getAnnotation(Plumbed::class.java)
			model.plumbedMap.put(element as TypeElement, plumbed.getValue())
		}
		model
	}

	val createPlumberMapImpl = {
		if (model.plumbedMap.size > 0) {
			PlumberMapImplWriter.write(filer, model)
		}
		model
	}

	val createPlumbers = {
		model.plumbedMap.forEach {
			PlumberWriter.write(filer, it.key, it.value)
		}
	}
}
