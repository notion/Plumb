package rxjoin.internal.codegen

import rxjoin.annotation.Joined
import javax.annotation.processing.Messager
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic.Kind.ERROR

fun Joined.getValue(): TypeMirror {
    try {
        return value as TypeMirror
    }
    catch (e: MirroredTypeException) {
        return e.typeMirror
    }
}

fun Messager.error(message: String) {
    this.printMessage(ERROR, message)
}
