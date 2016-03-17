package rxjoin.internal.codegen.validator

import org.junit.Before
import org.mockito.Mockito
import org.mockito.Mockito.anyString
import org.mockito.Mockito.eq
import org.mockito.Mockito.mock
import rxjoin.annotation.In
import rxjoin.annotation.Out
import rxjoin.internal.codegen.Model
import rxjoin.internal.codegen.Model.JoinerModel
import rxjoin.internal.codegen.Model.JoinerModel.Entry
import rxjoin.internal.codegen.Model.JoinerModel.InOutRegistry
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.ExecutableType
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic.Kind.ERROR
import org.mockito.Mockito.`when` as mockWhen

abstract class ValidatorTest {

    protected val mockedRoundEnv = mock(RoundEnvironment::class.java)
    protected val mockedFiler = mock(Filer::class.java)
    protected val mockedMessager = mock(Messager::class.java)
    protected val mockedTypes = mock(Types::class.java)
    protected val mockElements = mock(Elements::class.java)

    @Before
    open fun setUp() {
        mockWhen(mockedMessager.printMessage(eq(ERROR), anyString()))
                .then {
                    val errorMessage = it.arguments[1] as String
                    throw Exception(errorMessage)
                }
    }

    private fun getListOfInOutRegistriesWithIds(ids: Array<String>): MutableList<InOutRegistry> {
        val mockedField = Mockito.mock(Element::class.java)
        val list = mutableListOf<InOutRegistry>()
        ids.forEach {
            list.add(InOutRegistry(it, Entry(mockedField, mockedField)))
        }
        return list
    }

    protected fun getJoinerModelWithEntryIds(
            vararg entryIds: Array<String>): MutableList<JoinerModel> {
        val mockedElement = mock(TypeElement::class.java)
        val list: MutableList<JoinerModel> = mutableListOf()
        entryIds.forEach { ids ->
            val model = JoinerModel(mockedElement, mockedElement,
                    getListOfInOutRegistriesWithIds(ids))
            list.add(model)
        }
        return list
    }

    protected fun getMockedModelWithJoinerModels(
            joinerEntries: MutableList<JoinerModel> = mutableListOf()): Model {
        return Model(
                mockedRoundEnv,
                mockedFiler,
                mockedMessager,
                mockedTypes,
                mockElements,
                joinerEntries)
    }

    protected fun getMockExecutableTypeWithReturnType(type: TypeMirror): ExecutableType {
        val executableType = mock(ExecutableType::class.java)
        mockWhen(executableType.returnType).thenReturn(type)
        return executableType
    }

    private fun makeMockedElement(kind: ElementKind, type: TypeMirror): Element {
        val field = Mockito.mock(Element::class.java)
        mockWhen(field.kind).thenReturn(kind)
        mockWhen(field.asType()).thenReturn(type)
        mockWhen(field.enclosingElement).thenReturn(mock(Element::class.java))
        return field
    }

    protected fun makeMockedOutElement(kind: ElementKind, type: TypeMirror,
            outAnnotationValue: String): Element {
        val field = makeMockedElement(kind, type)
        mockOutAnnotationValue(field, outAnnotationValue)
        return field
    }

    protected fun makeMockedInElement(kind: ElementKind, type: TypeMirror,
            inAnnotationValue: String): Element {
        val field = makeMockedElement(kind, type)
        mockInAnnotationValue(field, inAnnotationValue)
        return field
    }

    protected fun mockOutAnnotationValue(element: Element, annotationValue: String) {
        val outAnnotation = mock(Out::class.java)
        mockWhen(outAnnotation.value).thenReturn(annotationValue)
        mockWhen(element.getAnnotation(Out::class.java)).thenReturn(outAnnotation)
    }

    protected fun mockInAnnotationValue(element: Element, annotationValue: String) {
        val inAnnotation = mock(In::class.java)
        mockWhen(inAnnotation.value).thenReturn(annotationValue)
        mockWhen(element.getAnnotation(In::class.java)).thenReturn(inAnnotation)
    }

}
