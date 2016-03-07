package plumb;

public class Plumbing {
    private static PlumberMap plumberMap = null;

    public static <T1, T2> void plumb(T1 t1, T2 t2) { // need better names
        ensureInitialized();
        Plumber<T1, T2> plumber = plumberMap.plumberFor(t1);
        plumber.plumb(t1, t2);
    }

    public static <T> void demolish(T plumbed) {
        ensureInitialized();
        Plumber<T, Object> plumber = plumberMap.plumberFor(plumbed);
        plumber.demolish();
    }

    private static void ensureInitialized() {
        if (plumberMap == null) {
            try {
                Class plumberMapClass = Class.forName(PlumberMap.IMPL_CLASS_FQCN);
                plumberMap = (PlumberMap) plumberMapClass.newInstance();
            } catch (Exception e) {
                throw new ExceptionInInitializerError(e);
            }
        }
    }
}
