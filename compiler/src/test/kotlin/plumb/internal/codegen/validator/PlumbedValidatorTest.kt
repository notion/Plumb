package plumb.internal.codegen.validator

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.anyString
import org.mockito.Mockito.eq
import org.mockito.Mockito.mock
import javax.annotation.processing.Messager
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.tools.Diagnostic.Kind
import org.mockito.Mockito.`when` as mockWhen

class PlumbedValidatorTest {

    private val mockedMessager = mock(Messager::class.java)

    @Before
    fun setUp() {
        mockWhen(mockedMessager.printMessage(eq(Kind.ERROR), anyString()))
                .then {
                    val errorMessage = it.arguments[1] as String
                    throw Exception(errorMessage)
                }
    }

    @Test
    fun test_publicClass_isValid() {
        val validClass = mock(Element::class.java)
        mockWhen(validClass.kind).thenReturn(ElementKind.CLASS)
        mockWhen(validClass.modifiers).thenReturn(mutableSetOf(Modifier.PUBLIC))

        assertThat(PlumbedValidator.validate(validClass, mockedMessager)).isEqualTo(true)
    }

    @Test
    fun test_privateClass_throwsException() {
        val privateClass = mock(Element::class.java)
        mockWhen(privateClass.kind).thenReturn(ElementKind.CLASS)
        mockWhen(privateClass.modifiers).thenReturn(mutableSetOf(Modifier.PRIVATE))
        assertThatThrownBy { PlumbedValidator.validate(privateClass, mockedMessager) }
                .isInstanceOf(Exception::class.java)
                .hasMessage(PlumbedValidator.publicErrorMessage(privateClass))
    }

    @Test
    fun test_publicField_throwsException() {
        val publicField = mock(Element::class.java)
        mockWhen(publicField.kind).thenReturn(ElementKind.FIELD)
        mockWhen(publicField.modifiers).thenReturn(mutableSetOf(Modifier.PUBLIC))
        assertThatThrownBy { PlumbedValidator.validate(publicField, mockedMessager) }
                .isInstanceOf(Exception::class.java)
                .hasMessage(PlumbedValidator.classErrorMessage(publicField))
    }
}
