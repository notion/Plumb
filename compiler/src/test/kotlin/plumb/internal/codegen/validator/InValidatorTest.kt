package plumb.internal.codegen.validator

import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import javax.lang.model.element.ElementKind
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import org.mockito.Mockito.`when` as mockWhen

class InValidatorTest : ValidatorTest() {

    private val mockObservableDeclaredType = mock(DeclaredType::class.java)
    private val mockStringType = mock(TypeMirror::class.java)

    private val mockBehaviorSubjectDeclaredType = mock(DeclaredType::class.java)
    private val mockBehaviorSubjectTypeMirror = mock(TypeMirror::class.java)

    @Before
    override fun setUp() {
        super.setUp()
        mockWhen(mockBehaviorSubjectTypeMirror.kind).thenReturn(TypeKind.DECLARED)
        mockWhen(mockBehaviorSubjectTypeMirror.toString()).thenReturn("rx.subjects.BehaviorSubject")

        mockWhen(mockBehaviorSubjectDeclaredType.enclosingType).thenReturn(
                mockBehaviorSubjectTypeMirror)
        mockWhen(mockBehaviorSubjectDeclaredType.typeArguments).thenReturn(
                mutableListOf(mockStringType))
        mockWhen(mockBehaviorSubjectDeclaredType.kind).thenReturn(TypeKind.DECLARED)

        mockWhen(mockObservableDeclaredType.typeArguments).thenReturn(
                mutableListOf(mockStringType, mockStringType))
        mockWhen(mockObservableDeclaredType.kind).thenReturn(TypeKind.DECLARED)
    }

    @Test
    fun test_Field_isValid() {
        mockWhen(mockedTypes.isAssignable(any(), any())).thenReturn(true)
        val field = makeMockedInElement(ElementKind.FIELD, mockBehaviorSubjectDeclaredType, "foo")
        val model = getPlumberModelWithEntryIds(arrayOf("foo"))
        Assertions.assertThat(InValidator.validate(field, getMockedModelWithPlumberModels(model)))
                .isTrue()
    }

    @Test
    fun testMethod_isInvalid() {
        val field = makeMockedInElement(ElementKind.METHOD, mockBehaviorSubjectDeclaredType, "foo")
        val model = getPlumberModelWithEntryIds(arrayOf("foo"))
        Assertions.assertThatThrownBy {
            InValidator.validate(field, getMockedModelWithPlumberModels(model))
        }
                .isInstanceOf(Exception::class.java)
                .hasMessage(InValidator.elementNotFieldError(field))
    }

    @Test
    fun test_Observable_isInvalid() {
        val field = makeMockedInElement(ElementKind.FIELD, mockObservableDeclaredType, "foo")
        val model = getPlumberModelWithEntryIds(arrayOf("bar"))

        Assertions.assertThatThrownBy {
            InValidator.validate(field, getMockedModelWithPlumberModels(model))
        }
                .isInstanceOf(Exception::class.java)
                .hasMessage(InValidator.fieldNotSubjectError(field))

    }

    @Test
    fun test_inValue_withNoMatchingPlumber_isInvalid() {
        mockWhen(mockedTypes.isAssignable(any(), any())).thenReturn(true)
        val field = makeMockedInElement(ElementKind.FIELD, mockBehaviorSubjectDeclaredType, "foo")
        val model = getPlumberModelWithEntryIds(arrayOf("bar"))

        Assertions.assertThatThrownBy {
            InValidator.validate(field, getMockedModelWithPlumberModels(model))
        }
                .isInstanceOf(Exception::class.java)
                .hasMessage(InValidator.noCorrespondingRegistryError(field))
    }
}
