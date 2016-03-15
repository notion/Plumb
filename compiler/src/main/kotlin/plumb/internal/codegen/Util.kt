package plumb.internal.codegen

import plumb.annotation.Plumbed
import javax.annotation.processing.Messager
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic.Kind

fun Plumbed.getValue(): TypeMirror {
    try {
        return value as TypeMirror
    }
    catch (e: MirroredTypeException) {
        return e.typeMirror
    }
}

fun Messager.error(message: String) {
    this.printMessage(Kind.ERROR, message)
}
