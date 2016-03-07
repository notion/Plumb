package plumb.internal

import com.google.common.truth.Truth.assertAbout
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourceSubjectFactory.javaSource
import org.junit.Test

open class PlumberMapImplTest {

    private val singleClassSource = JavaFileObjects.forSourceLines("plumb.example.Example",
            "package plumb.example;",

            "import plumb.annotation.Plumbed;",

            "@Plumbed(PlumbedClassA.PlumbedViewModelA.class)",
            "class PlumbedClassA {",

            "   PlumbedViewModelA viewModel = new PlumbedViewModelA();",

            "   class PlumbedViewModelA {",
            "   }",
            "}")

    private val combinedClassSource = JavaFileObjects.forSourceLines("plumb.example.Example",
            "package plumb.example;",

            "import plumb.annotation.Plumbed;",

            "@Plumbed(PlumbedClassA.PlumbedViewModelA.class)",
            "class PlumbedClassA {",

            "   PlumbedViewModelA viewModel = new PlumbedViewModelA();",

            "   class PlumbedViewModelA {",
            "   }",
            "}",
            "@Plumbed(PlumbedClassB.PlumbedViewModelB.class)",
            "class PlumbedClassB {",

            "   PlumbedViewModelB viewModel = new PlumbedViewModelB();",

            "   class PlumbedViewModelB {",
            "   }",
            "}"
    )


    private val singleClassImplResult = JavaFileObjects.forSourceLines("plumb/PlumberMapImpl",
            "package plumb;",

            "import java.util.HashMap;",
            "import java.util.Map;",

            "public class PlumberMapImpl {",
            "   private Map<Class, Plumber> plumberMap = new HashMap<>();",

            "   public PlumberMapImpl() {",
            "       plumberMap.put(PlumbedClassA.class, new PlumbedClassA_Plumber());",
            "   }",

            "   @Override",
            "   public <T, R> Plumber<T, R> plumberFor(T t) {",
            "       return plumberMap.get(t.getClass());",
            "   }",
            "}"
    )

    private val combinedClassImplResult = JavaFileObjects.forSourceLines("plumb/PlumberMapImpl",
            "package plumb;",

            "import java.util.HashMap;",
            "import java.util.Map;",

            "public class PlumberMapImpl {",
            "   private Map<Class, Plumber> plumberMap = new HashMap<>();",

            "   public PlumberMapImpl() {",
            "       plumberMap.put(PlumbedClassA.class, new PlumbedClassA_Plumber());",
            "       plumberMap.put(PlumbedClassB.class, new PlumbedClassB_Plumber());",
            "   }",

            "   @Override",
            "   public <T, R> Plumber<T, R> plumberFor(T t) {",
            "       return plumberMap.get(t.getClass());",
            "   }",
            "}"
    )

    @Test
    fun test_compile_singlePlumbedClass() {
        assertAbout(javaSource())
                .that(singleClassSource)
                //.processedWith(plumbProcessor()) TODO
                .compilesWithoutError()
                .and()
                .generatesSources(singleClassImplResult)
    }

    @Test
    fun test_compile_multiplePlumbedClasses() {
        assertAbout(javaSource())
                .that(combinedClassSource)
                //.processedWith(plumbProcessor()) TODO
                .compilesWithoutError()
                .and()
                .generatesSources(combinedClassImplResult)
    }

}
