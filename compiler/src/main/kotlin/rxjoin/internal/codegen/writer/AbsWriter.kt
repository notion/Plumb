package rxjoin.internal.codegen.writer

import com.squareup.javapoet.JavaFile.Builder
import javax.annotation.processing.Filer

abstract class AbsWriter<T> {

    val PKG_NAME = "rxjoin"
    private val DO_NOT_MODIFY = "*** Automatically generated file. DO NOT MODIFY! ***"

    protected abstract fun _write(model: T): Builder

    fun write(t: T, filer: Filer) {
        val javaFile = _write(t)
                .addFileComment(DO_NOT_MODIFY)
                .build()
        javaFile.writeTo(filer)
    }
}
