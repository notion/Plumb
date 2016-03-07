package plumb;

public interface PlumberMap {
    String IMPL_CLASS_FQCN = "plumb.PlumberMapImpl";

    <T, R> Plumber<T, R> plumberFor(T t);
}
