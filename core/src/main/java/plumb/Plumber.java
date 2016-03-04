package plumb;

public interface Plumber<T1, T2> {
    void plumb(T1 t1, T2 t2);

    void demolish();
}
