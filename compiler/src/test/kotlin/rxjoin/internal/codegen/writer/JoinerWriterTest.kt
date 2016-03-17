package rxjoin.internal.codegen.writer

import com.google.common.truth.Truth.assertAbout
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourceSubjectFactory
import org.junit.Test
import rxjoin.internal.codegen.RxJoinProcessor

class JoinerWriterTest {

    private val singleClassSource = JavaFileObjects.forSourceLines("rxjoin.example.JoinedClassA",
            "package rxjoin.example;",

            "import rx.Observable;",
            "import rx.subjects.BehaviorSubject;",
            "import rxjoin.annotation.*;",

            "@Joined(JoinedClassA.JoinedViewModelA.class)",
            "public class JoinedClassA {",

            "	JoinedViewModelA viewModel = new JoinedViewModelA();",

            "	@Out(\"integer\")",
            "	public Observable<Integer> integerObservable;",

            "	@In(\"string\")",
            "	public BehaviorSubject<String> stringSubject = BehaviorSubject.create();",

            "	public class JoinedViewModelA {",
            "		@In(\"integer\")",
            "		public BehaviorSubject<Integer> integerSubject = BehaviorSubject.create();",

            "		@In(\"string\")",
            "		public BehaviorSubject<String> stringSubject = BehaviorSubject.create();",

            "		@Out(\"string\")",
            "		public Observable<String> stringObservable;",
            "   }",
            "}")

    private val joinedClassAJoiner = JavaFileObjects.forSourceLines("rxjoin.JoinedClassA_Joiner",
            "package rxjoin;",
            "",
            "import java.lang.Override;",
            "import rx.subscriptions.CompositeSubscription;",
            "import rxjoin.example.JoinedClassA;",
            "",
            "public class JoinedClassA_Joiner implements Joiner<JoinedClassA, JoinedClassA.JoinedViewModelA> {",
            "	private CompositeSubscription subscriptions;",
            "",
            "	@Override",
            "	public void join(JoinedClassA joined, JoinedClassA.JoinedViewModelA joinedTo) {",
            "		if (subscriptions != null && !subscriptions.isUnsubscribed()) {",
            "			subscriptions.unsubscribe()",
            "		}",
            "		subscriptions = new CompositeSubscription();",
            "		subscriptions.add(Utils.replicate(joined.integerObservable, joinedTo.integerSubject));",
            "		subscriptions.add(Utils.replicate(joinedTo.stringObservable, joinedTo.stringSubject));",
            "		subscriptions.add(Utils.replicate(joinedTo.stringObservable, joined.stringSubject));",
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
                .processedWith(RxJoinProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(joinedClassAJoiner)
    }
}
