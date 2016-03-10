package plumb.internal.codegen

import plumb.annotation.In
import plumb.annotation.Out
import plumb.annotation.Plumbed
import plumb.internal.codegen.writer.PlumberMapImplWriter
import plumb.internal.codegen.writer.PlumberWriter
import java.util.*
import javax.annotation.processing.Filer
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement

class ModelOperations(val model: Model, private val roundEnv: RoundEnvironment, private val filer: Filer) {

	fun populatePlumbedMap() {
		val elements = roundEnv.getElementsAnnotatedWith(Plumbed::class.java)
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
	}

	fun populateOutsAndIns() {
		model.plumbedMap.forEach { plumbedEntry ->
			val outList = mutableListOf<Element>()
			val inList = mutableListOf<Element>()

			fillOutAndInLists(plumbedEntry.key, outList, inList)
			fillOutAndInLists(plumbedEntry.value, outList, inList)

			val map = HashMap<Element, Element>()
			outList.forEach { outElement ->
				val outAnnotation = outElement.getAnnotation(Out::class.java)
				val inElement: Element = inList.first { it.getAnnotation(In::class.java).value == outAnnotation.value }
				map[outElement] = inElement
			}
			model.outsAndInsForPlumbedClass[plumbedEntry.key] = map
		}
	}

	private fun fillOutAndInLists(element: Element, outList: MutableList<Element>, inList: MutableList<Element>) {
		element.enclosedElements
				.filter { it.kind == ElementKind.FIELD || it.kind == ElementKind.METHOD }
				.forEach { element: Element ->
					if (element.getAnnotation(Out::class.java) != null) {
						outList.add(element)
					}
					else if (element.getAnnotation(In::class.java) != null) {
						inList.add(element)
					}
				}
	}

	fun generatePlumbers() {
		model.plumbedMap.forEach {
			val outsAndIns = model.outsAndInsForPlumbedClass[it.key]!!
			PlumberWriter.write(filer, it.key, it.value, outsAndIns)
		}
	}

	fun generatePlumberMapImpl() {
		if (model.plumbedMap.size > 0) {
			PlumberMapImplWriter.write(filer, model)
		}
	}
}
