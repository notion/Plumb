package plumb.internal.codegen

import plumb.annotation.Plumbed
import plumb.internal.codegen.writer.PlumberMapImplWriter
import plumb.internal.codegen.writer.PlumberWriter
import javax.annotation.processing.Filer
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

class ModelOperations(val model: Model, private val filer: Filer) {

	fun populatePlumbedMap(elements: Set<Element>) {
		elements.forEach { element ->
			if (element is TypeElement) {
				val plumbed = element.getAnnotation(Plumbed::class.java)
				val value = plumbed.getValue()
				element.enclosedElements.first { it.asType() == value }
					.let {
						model.plumbedMap.put(element, it as TypeElement)
					}
			}

		}
		model
	}

	fun generatePlumberMapImpl() {
		if (model.plumbedMap.size > 0) {
			PlumberMapImplWriter.write(filer, model)
		}
	}

	fun generatePlumbers() {
		model.plumbedMap.forEach {
			PlumberWriter.write(filer, it.key, it.value)
		}
	}
}
