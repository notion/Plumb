package rxjoin.example.viewmodel;

import rx.Observable;
import rx.functions.Func3;
import rx.subjects.BehaviorSubject;
import rxjoin.annotation.In;
import rxjoin.annotation.Out;

public class ColorPickerViewModel {

    @In("red") public BehaviorSubject<Integer> red = BehaviorSubject.create();
    @In("green") public BehaviorSubject<Integer> green = BehaviorSubject.create();
    @In("blue") public BehaviorSubject<Integer> blue = BehaviorSubject.create();

    @Out("color") public Observable<Integer> colorMethod() {
        return Observable.combineLatest(red, green, blue,
                new Func3<Integer, Integer, Integer, Integer>() {
                    @Override public Integer call(Integer r, Integer g, Integer b) {
                        return (0xFF << 24) | (r << 16) | (g << 8) | b;
                    }
                });
    }
}
