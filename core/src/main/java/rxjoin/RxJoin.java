package rxjoin;

public class RxJoin {
    private static JoinerMap joinerMap = null;

    public static <T1, T2> void join(T1 t1, T2 t2) { // need better names
        ensureInitialized();
        Joiner<T1, T2> joiner = joinerMap.joinerFor(t1);
        joiner.join(t1, t2);
    }

    public static <T> void demolish(T joined) {
        ensureInitialized();
        Joiner<T, Object> joiner = joinerMap.joinerFor(joined);
        joiner.demolish();
    }

    private static void ensureInitialized() {
        if (joinerMap == null) {
            try {
                Class joinerMapClass = Class.forName(JoinerMap.IMPL_CLASS_FQCN);
                joinerMap = (JoinerMap) joinerMapClass.newInstance();
            }
            catch (Exception e) {
                throw new ExceptionInInitializerError(e);
            }
        }
    }
}
