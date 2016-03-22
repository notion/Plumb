package rxjoin.internal.codegen.validator

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import javax.lang.model.element.ElementKind.CLASS
import javax.lang.model.element.ElementKind.FIELD
import javax.lang.model.element.ElementKind.METHOD
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeKind.DECLARED
import javax.lang.model.type.TypeKind.EXECUTABLE
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
        mockWhen(mockObservableDeclaredType.typeArguments).thenReturn(mutableListOf(mockStringType))
        mockWhen(mockObservableDeclaredType.kind).thenReturn(DECLARED)
    }

    @Test
    fun test_Field_isValidOut() {
        mockWhen(mockedTypes.isAssignable(Mockito.any(), Mockito.any())).thenReturn(true)
        val field = makeMockedOutElement(FIELD, mockObservableDeclaredType, "foo")
        val model = getJoinerModelWithEntryIds(arrayOf("bar"))
        assertThat(OutValidator.validate(field, getMockedModelWithJoinerModels(model)))
                .isTrue()
    }

    @Test
    fun test_Method_isValidOut() {
        mockWhen(mockedTypes.isAssignable(Mockito.any(), Mockito.any())).thenReturn(true)
        val method = makeMockedOutElement(METHOD,
                getMockExecutableTypeWithReturnType(mockObservableDeclaredType), "foo")
        mockWhen(method.asType().kind).thenReturn(EXECUTABLE)
        val model = getJoinerModelWithEntryIds(arrayOf("bar"))
        assertThat(OutValidator.validate(method,
                getMockedModelWithJoinerModels(model)))
                .isTrue()
    }

    @Test
    fun test_Class_isInvalidOut() {
        val clz = makeMockedOutElement(CLASS, mockObservableDeclaredType, "foo")

        Assertions.assertThatThrownBy {
            OutValidator.validate(clz, getMockedModelWithJoinerModels())
        }
                .isInstanceOf(Exception::class.java)
                .hasMessage(OutValidator.elementNotFieldOrMethodError(clz))
    }

    @Test
    fun test_methodThatReturnsString_isInvalidOut() {
        val method = makeMockedOutElement(METHOD,
                getMockExecutableTypeWithReturnType(mockStringType), "foo")
        Assertions.assertThatThrownBy {
            OutValidator.validate(method, getMockedModelWithJoinerModels())
        }
                .isInstanceOf(Exception::class.java)
                .hasMessage(OutValidator.methodDoesNotReturnObservableError(method))
    }

    @Test
    fun test_fieldThatIsString_isInvalidOut() {
        val field = makeMockedOutElement(FIELD, mockStringType, "foo")
        Assertions.assertThatThrownBy {
            OutValidator.validate(field, getMockedModelWithJoinerModels())
        }
                .isInstanceOf(Exception::class.java)
                .hasMessage(OutValidator.fieldNotParameterizedObservable(field))
    }

    @Test
    fun test_addingValidOutElement_withUniqueValue_isValid() {
        mockWhen(mockedTypes.isAssignable(Mockito.any(), Mockito.any())).thenReturn(true)
        val field = makeMockedOutElement(FIELD, mockObservableDeclaredType, "foo")
        val model = getJoinerModelWithEntryIds(arrayOf("bar", "baz"))
        assertThat(OutValidator.validate(field,
                getMockedModelWithJoinerModels(model)))
                .isTrue()

    }

    @Test
    fun test_addingOutElement_withDuplicateValue_isInvalid() {
        mockWhen(mockedTypes.isAssignable(Mockito.any(), Mockito.any())).thenReturn(true)
        val field = makeMockedOutElement(FIELD, mockObservableDeclaredType, "foo")
        val model = getJoinerModelWithEntryIds(arrayOf("foo", "bar"))
        Assertions.assertThatThrownBy {
            OutValidator.validate(field, getMockedModelWithJoinerModels(model))
        }
                .isInstanceOf(Exception::class.java)
                .hasMessage(OutValidator.duplicateOutValueError("foo"))
    }

    @Test
    fun test_outElement_withNoJoinedParent_isInvalid() {
        mockWhen(mockedTypes.isAssignable(Mockito.any(), Mockito.any())).thenReturn(true)
        val field = makeMockedOutElement(FIELD, mockObservableDeclaredType, "foo")
        Assertions.assertThatThrownBy {
            OutValidator.validate(field, getMockedModelWithJoinerModels())
        }
                .isInstanceOf(Exception::class.java)
                .hasMessage(OutValidator.notEnclosedByJoinedClass(field))
    }
}
