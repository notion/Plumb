package plumb.internal.codegen

import plumb.annotation.Plumbed
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror

fun Plumbed.getValue(): TypeMirror {
    try {
        return value as TypeMirror
    }
    catch (e: MirroredTypeException) {
        return e.typeMirror
    }
}
