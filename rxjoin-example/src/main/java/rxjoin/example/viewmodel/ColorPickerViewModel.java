package rxjoin.example.viewmodel;

import plumb.annotation.In;
import plumb.annotation.Out;
import rx.Observable;
import rx.functions.Func3;
import rx.subjects.BehaviorSubject;

public class ColorPickerViewModel {

    @In("red") public BehaviorSubject<Integer> red = BehaviorSubject.create();
    @In("green") public BehaviorSubject<Integer> green = BehaviorSubject.create();
    @In("blue") public BehaviorSubject<Integer> blue = BehaviorSubject.create();

    @Out("color")
    public Observable<Integer> color = Observable.combineLatest(red, green, blue,
            new Func3<Integer, Integer, Integer, Integer>() {
                @Override public Integer call(Integer r, Integer g, Integer b) {
                    return (0xFF << 24) | (r << 16) | (g << 8) | b;
                }
            });
}
