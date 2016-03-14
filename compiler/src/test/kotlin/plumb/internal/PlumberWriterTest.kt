package plumb.internal

import com.google.common.truth.Truth.assertAbout
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourceSubjectFactory
import org.junit.Test
import plumb.internal.codegen.PlumbProcessor

class PlumberWriterTest {

    private val singleClassSource = JavaFileObjects.forSourceLines("plumb.example.PlumbedClassA",
            "package plumb.example;",

            "import plumb.annotation.*;",
            "import rx.Observable;",
            "import rx.subjects.BehaviorSubject;",

            "@Plumbed(PlumbedClassA.PlumbedViewModelA.class)",
            "public class PlumbedClassA {",

            "	PlumbedViewModelA viewModel = new PlumbedViewModelA();",

            "	@Out(\"integer\")",
            "	public Observable<Integer> integerObservable;",

            "	@In(\"string\")",
            "	public BehaviorSubject<String> stringSubject = BehaviorSubject.create();",

            "	public class PlumbedViewModelA {",
            "		@In(\"integer\")",
            "		public BehaviorSubject<Integer> integerSubject = BehaviorSubject.create();",

            "		@In(\"string\")",
            "		public BehaviorSubject<String> stringSubject = BehaviorSubject.create();",

            "		@Out(\"string\")",
            "		public Observable<String> stringObservable;",
            "   }",
            "}")

    private val plumbedClassAPlumber = JavaFileObjects.forSourceLines("plumb.PlumbedClassA_Plumber",
            "package plumb;",
            "",
            "import java.lang.Override;",
            "import plumb.example.PlumbedClassA;",
            "import rx.subscriptions.CompositeSubscription;",
            "",
            "public class PlumbedClassA_Plumber implements Plumber<PlumbedClassA, PlumbedClassA.PlumbedViewModelA> {",
            "	private CompositeSubscription subscriptions;",
            "",
            "	@Override",
            "	public void plumb(PlumbedClassA plumbed, PlumbedClassA.PlumbedViewModelA plumbedTo) {",
            "		subscriptions = new CompositeSubscription();",
            "		subscriptions.add(Utils.replicate(plumbed.integerObservable, plumbedTo.integerSubject));",
            "		subscriptions.add(Utils.replicate(plumbedTo.stringObservable, plumbedTo.stringSubject));",
            "		subscriptions.add(Utils.replicate(plumbedTo.stringObservable, plumbed.stringSubject));",
            "	}",
            "",
            "	@Override",
            "	public void demolish() {",
            "		subscriptions.unsubscribe();",
            "	}",
            "}"
    )

    @Test
    fun test_compile_simpleCase() {
        assertAbout(JavaSourceSubjectFactory.javaSource())
                .that(singleClassSource)
                .processedWith(PlumbProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(plumbedClassAPlumber)
    }
}
