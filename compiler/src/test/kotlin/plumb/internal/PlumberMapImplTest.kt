package plumb.internal

import com.google.common.truth.Truth.assertAbout
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourceSubjectFactory.javaSource
import org.junit.Test
import plumb.internal.codegen.PlumbProcessor

open class PlumberMapImplTest {

	// TODO - Make sources .java files in resources/

	private val singleClassSource = JavaFileObjects.forSourceLines("plumb.example.PlumbedClassA",
			"package plumb.example;",

			"import plumb.annotation.*;",
			"import rx.Observable;",
			"import rx.subjects.BehaviorSubject;",

			"@Plumbed(PlumbedClassA.PlumbedViewModelA.class)",
			"public class PlumbedClassA {",

			"   PlumbedViewModelA viewModel = new PlumbedViewModelA();",

			"	@Out(\"integer\")",
			"	public Observable<Integer> producer;",

			"   public class PlumbedViewModelA {",
			"		@In(\"integer\")",
			"		public BehaviorSubject<Integer> integer = BehaviorSubject.create();",
			"   }",
			"}")

	private val combinedClassSource = JavaFileObjects.forSourceLines("plumb.example.MultiplePlumbedClasses",
			"package plumb.example;",

			"import plumb.annotation.Plumbed;",

			"public class MultiplePlumbedClasses {",
			"   @Plumbed(PlumbedClassA.PlumbedViewModelA.class)",
			"   public class PlumbedClassA {",
			"       PlumbedViewModelA viewModel = new PlumbedViewModelA();",
			"       public class PlumbedViewModelA {",
			"       }",
			"   }",

			"   @Plumbed(PlumbedClassB.PlumbedViewModelB.class)",
			"   public class PlumbedClassB {",
			"       PlumbedViewModelB viewModel = new PlumbedViewModelB();",
			"        public class PlumbedViewModelB {",
			"       }",
			"   }",
			"}"
	)


	private val singleClassImplResult = JavaFileObjects.forSourceLines("plumb/PlumberMapImpl",
			"package plumb;",
			"",
			"import java.lang.Class",
			"import java.lang.Override",
			"import java.util.HashMap;",
			"import java.util.Map;",
			"import plumb.example.PlumbedClassA;",
			"",
			"public class PlumberMapImpl implements PlumberMap {",
			"   private Map<Class, Plumber> plumberMap = new HashMap<Class, Plumber>();",
			"",
			"   public PlumberMapImpl() {",
			"       plumberMap.put(PlumbedClassA.class, new PlumbedClassA_Plumber());",
			"   }",
			"",
			"   @Override",
			"   public <T, R> Plumber<T, R> plumberFor(T t) {",
			"       return plumberMap.get(t.getClass());",
			"   }",
			"}"
	)

	private val combinedClassImplResult = JavaFileObjects.forSourceLines("plumb/PlumberMapImpl",
			"package plumb;",
			"",
			"import java.lang.Class",
			"import java.lang.Override",
			"import java.util.HashMap;",
			"import java.util.Map;",
			"import plumb.example.MultiplePlumbedClasses;",
			"",
			"public class PlumberMapImpl implements PlumberMap {",
			"   private Map<Class, Plumber> plumberMap = new HashMap<Class, Plumber>();",
			"",
			"   public PlumberMapImpl() {",
			"       plumberMap.put(MultiplePlumbedClasses.PlumbedClassB.class, new PlumbedClassB_Plumber());",
			"       plumberMap.put(MultiplePlumbedClasses.PlumbedClassA.class, new PlumbedClassA_Plumber());",
			"   }",
			"",
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
				.processedWith(PlumbProcessor())
				.compilesWithoutError()
				.and()
				.generatesSources(singleClassImplResult)
	}

	@Test
	fun test_compile_multiplePlumbedClasses() {
		assertAbout(javaSource())
				.that(combinedClassSource)
				.processedWith(PlumbProcessor())
				.compilesWithoutError()
				.and()
				.generatesSources(combinedClassImplResult)
	}
}
