package rxjoin;

public interface JoinerMap {
    String IMPL_CLASS_FQCN = "rxjoin.JoinerMapImpl";

    <T, R> Joiner<T, R> joinerFor(T t);
}
