package rxjoin.internal.codegen.writer

import com.google.common.truth.Truth.assertAbout
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourceSubjectFactory.javaSource
import org.junit.Test
import rxjoin.internal.codegen.RxJoinProcessor

open class JoinerMapImplWriterTest {

    // TODO - Make sources .java files in resources/

    private val singleClassSource = JavaFileObjects.forSourceLines("rxjoin.example.JoinedClassA",
            "package rxjoin.example;",

            "import rxjoin.annotation.*;",
            "import rx.Observable;",
            "import rx.subjects.BehaviorSubject;",

            "@Joined(JoinedClassA.JoinedViewModelA.class)",
            "public class JoinedClassA {",

            "   JoinedViewModelA viewModel = new JoinedViewModelA();",

            "	@Out(\"integer\")",
            "	public Observable<Integer> producer;",

            "   public class JoinedViewModelA {",
            "		@In(\"integer\")",
            "		public BehaviorSubject<Integer> integer = BehaviorSubject.create();",
            "   }",
            "}")

    private val combinedClassSource = JavaFileObjects.forSourceLines(
            "rxjoin.example.MultipleJoinedClasses",
            "package rxjoin.example;",

            "import rxjoin.annotation.Joined;",

            "public class MultipleJoinedClasses {",
            "   @Joined(JoinedClassA.JoinedViewModelA.class)",
            "   public class JoinedClassA {",
            "       JoinedViewModelA viewModel = new JoinedViewModelA();",
            "       public class JoinedViewModelA {",
            "       }",
            "   }",

            "   @Joined(JoinedClassB.JoinedViewModelB.class)",
            "   public class JoinedClassB {",
            "       JoinedViewModelB viewModel = new JoinedViewModelB();",
            "        public class JoinedViewModelB {",
            "       }",
            "   }",
            "}"
    )


    private val singleClassImplResult = JavaFileObjects.forSourceLines("rxjoin/JoinerMapImpl",
            "package rxjoin;",
            "",
            "import java.lang.Class;",
            "import java.lang.Override;",
            "import java.util.HashMap;",
            "import java.util.Map;",
            "import rxjoin.example.JoinedClassA;",
            "",
            "public class JoinerMapImpl implements JoinerMap {",
            "   private Map<Class, Joiner> joinerMap = new HashMap<Class, Joiner>();",
            "",
            "   public JoinerMapImpl() {",
            "       joinerMap.put(JoinedClassA.class, new JoinedClassA_Joiner());",
            "   }",
            "",
            "   @Override",
            "   public <T, R> Joiner<T, R> joinerFor(T t) {",
            "       return joinerMap.get(t.getClass());",
            "   }",
            "}"
    )

    private val combinedClassImplResult = JavaFileObjects.forSourceLines("rxjoin/JoinerMapImpl",
            "package rxjoin;",
            "",
            "import java.lang.Class;",
            "import java.lang.Override;",
            "import java.util.HashMap;",
            "import java.util.Map;",
            "import rxjoin.example.MultipleJoinedClasses;",
            "",
            "public class JoinerMapImpl implements JoinerMap {",
            "   private Map<Class, Joiner> joinerMap = new HashMap<Class, Joiner>();",
            "",
            "   public JoinerMapImpl() {",
            "       joinerMap.put(MultipleJoinedClasses.JoinedClassA.class, new JoinedClassA_Joiner());",
            "       joinerMap.put(MultipleJoinedClasses.JoinedClassB.class, new JoinedClassB_Joiner());",
            "   }",
            "",
            "   @Override",
            "   public <T, R> Joiner<T, R> joinerFor(T t) {",
            "       return joinerMap.get(t.getClass());",
            "   }",
            "}"
    )

    @Test
    fun test_compile_singleJoinedClass() {
        assertAbout(javaSource())
                .that(singleClassSource)
                .processedWith(RxJoinProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(singleClassImplResult)
    }

    @Test
    fun test_compile_multipleJoinedClasses() {
        assertAbout(javaSource())
                .that(combinedClassSource)
                .processedWith(RxJoinProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(combinedClassImplResult)
    }
}
