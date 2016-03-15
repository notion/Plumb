package plumb.internal.codegen.validator

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import org.mockito.Mockito.`when` as mockWhen

class OutValidatorTest : ValidatorTest() {

    private val mockObservableTypeElement = mock(TypeElement::class.java)
    private val mockObservableDeclaredType = mock(DeclaredType::class.java)
    private val mockStringType = mock(TypeMirror::class.java)

    override fun setUp() {
        super.setUp()
        mockWhen(mockStringType.toString()).thenReturn("java.lang.String")
        mockWhen(mockElements.getTypeElement("rx.Observable")).thenReturn(mockObservableTypeElement)
        mockWhen(mockedTypes.isAssignable(Mockito.any(), Mockito.any())).thenReturn(true)
        mockWhen(mockObservableDeclaredType.typeArguments).thenReturn(mutableListOf(mockStringType))
        mockWhen(mockObservableDeclaredType.kind).thenReturn(TypeKind.DECLARED)
    }

    @Test
    fun test_Field_isValidOut() {
        val field = makeMockedElement(ElementKind.FIELD, mockObservableDeclaredType, "foo")
        val model = getPlumberModelWithEntryIds(arrayOf("bar"))
        assertThat(OutValidator.validate(field, getMockedModelWithPlumberModels(model)))
                .isTrue()
    }

    @Test
    fun test_Method_isValidOut() {
        val method = makeMockedElement(ElementKind.METHOD,
                getMockExecutableTypeWithReturnType(mockObservableDeclaredType), "foo")
        mockWhen(method.asType().kind).thenReturn(TypeKind.DECLARED)
        val model = getPlumberModelWithEntryIds(arrayOf("bar"))
        assertThat(OutValidator.validate(method,
                getMockedModelWithPlumberModels(model)))
                .isTrue()
    }

    @Test
    fun test_Class_isInvalidOut() {
        val clz = makeMockedElement(ElementKind.CLASS, mockObservableDeclaredType, "foo")

        Assertions.assertThatThrownBy {
            OutValidator.validate(clz, getMockedModelWithPlumberModels())
        }
                .isInstanceOf(Exception::class.java)
                .hasMessage(OutValidator.elementNotFieldOrMethodError(clz))
    }

    @Test
    fun test_methodThatReturnsString_isInvalidOut() {
        val method = makeMockedElement(ElementKind.METHOD,
                getMockExecutableTypeWithReturnType(mockStringType), "foo")
        Assertions.assertThatThrownBy {
            OutValidator.validate(method, getMockedModelWithPlumberModels())
        }
                .isInstanceOf(Exception::class.java)
                .hasMessage(OutValidator.methodDoesNotReturnObservableError(method))
    }

    @Test
    fun test_fieldThatIsString_isInvalidOut() {
        val field = makeMockedElement(ElementKind.FIELD, mockStringType, "foo")
        Assertions.assertThatThrownBy {
            OutValidator.validate(field, getMockedModelWithPlumberModels())
        }
                .isInstanceOf(Exception::class.java)
                .hasMessage(OutValidator.fieldNotParameterizedObservable(field))
    }

    @Test
    fun test_addingValidOutElement_withUniqueValue_isValid() {
        val field = makeMockedElement(ElementKind.FIELD, mockObservableDeclaredType, "foo")
        val model = getPlumberModelWithEntryIds(arrayOf("bar", "baz"))
        assertThat(OutValidator.validate(field,
                getMockedModelWithPlumberModels(model)))
                .isTrue()

    }

    @Test
    fun test_addingOutElement_withDuplicateValue_isInvalid() {
        val field = makeMockedElement(ElementKind.FIELD, mockObservableDeclaredType, "foo")
        val model = getPlumberModelWithEntryIds(arrayOf("foo", "bar"))
        Assertions.assertThatThrownBy {
            OutValidator.validate(field, getMockedModelWithPlumberModels(model))
        }
                .isInstanceOf(Exception::class.java)
                .hasMessage(OutValidator.duplicateOutValueError("foo"))
    }

    @Test
    fun test_outElement_withNoPlumbedParent_isInvalid() {
        val field = makeMockedElement(ElementKind.FIELD, mockObservableDeclaredType, "foo")
        Assertions.assertThatThrownBy {
            OutValidator.validate(field, getMockedModelWithPlumberModels())
        }
                .isInstanceOf(Exception::class.java)
                .hasMessage(OutValidator.notEnclosedByPlumbedClass(field))
    }
}
