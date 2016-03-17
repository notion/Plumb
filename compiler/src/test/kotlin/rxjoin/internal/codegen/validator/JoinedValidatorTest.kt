package rxjoin.internal.codegen.validator

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.anyString
import org.mockito.Mockito.eq
import org.mockito.Mockito.mock
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind.CLASS
import javax.lang.model.element.ElementKind.FIELD
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.Modifier.PUBLIC
import javax.tools.Diagnostic.Kind.ERROR
import org.mockito.Mockito.`when` as mockWhen

class JoinedValidatorTest : ValidatorTest() {

    @Before
    override fun setUp() {
        super.setUp()
        mockWhen(mockedMessager.printMessage(eq(ERROR), anyString()))
                .then {
                    val errorMessage = it.arguments[1] as String
                    throw Exception(errorMessage)
                }
    }

    @Test
    fun test_publicClass_isValid() {
        val validClass = mock(Element::class.java)
        mockWhen(validClass.kind).thenReturn(CLASS)
        mockWhen(validClass.modifiers).thenReturn(mutableSetOf(PUBLIC))

        assertThat(
                JoinedValidator.validate(validClass, getMockedModelWithJoinerModels())).isEqualTo(
                true)
    }

    @Test
    fun test_privateClass_throwsException() {
        val privateClass = mock(Element::class.java)
        mockWhen(privateClass.kind).thenReturn(CLASS)
        mockWhen(privateClass.modifiers).thenReturn(mutableSetOf(PRIVATE))
        assertThatThrownBy {
            JoinedValidator.validate(privateClass, getMockedModelWithJoinerModels())
        }
                .isInstanceOf(Exception::class.java)
                .hasMessage(JoinedValidator.publicErrorMessage(privateClass))
    }

    @Test
    fun test_publicField_throwsException() {
        val publicField = mock(Element::class.java)
        mockWhen(publicField.kind).thenReturn(FIELD)
        mockWhen(publicField.modifiers).thenReturn(mutableSetOf(PUBLIC))
        assertThatThrownBy {
            JoinedValidator.validate(publicField, getMockedModelWithJoinerModels())
        }
                .isInstanceOf(Exception::class.java)
                .hasMessage(JoinedValidator.classErrorMessage(publicField))
    }
}
