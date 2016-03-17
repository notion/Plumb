package rxjoin;

public interface Joiner<T1, T2> {
    void join(T1 t1, T2 t2);

    void demolish();
}
