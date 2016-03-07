package plumb;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subjects.Subject;

public class Utils {
    public static <T> Subscription replicate(Observable<T> source, final Subject<T, T> subject) {
        return source.subscribe(new Subscriber<T>() {
            @Override
            public void onNext(T next) {
                subject.onNext(next);
            }

            @Override
            public void onError(Throwable t) {
                subject.onError(t);
            }

            @Override
            public void onCompleted() {
            }
        });
    }
}
