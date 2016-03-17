package rxjoin.example.viewmodel;

import rx.Observable;
import rx.functions.Func3;
import rx.subjects.BehaviorSubject;
import rxjoin.annotation.In;
import rxjoin.annotation.Out;

public class ColorPickerViewModel {

    @In("red") public BehaviorSubject<Integer> red = BehaviorSubject.create(20);
    @In("green") public BehaviorSubject<Integer> green = BehaviorSubject.create(0);
    @In("blue") public BehaviorSubject<Integer> blue = BehaviorSubject.create(0);

    @Out("color") public Observable<Integer> color = Observable.combineLatest(red, green, blue,
            new Func3<Integer, Integer, Integer, Integer>() {
                @Override public Integer call(Integer r, Integer g, Integer b) {
                    return (0xFF << 24) | (r << 16) | (g << 8) | b;
                }
            });

    private static ColorPickerViewModel instance = new ColorPickerViewModel();

    public static ColorPickerViewModel getInstance() {
        return instance;
    }

    private ColorPickerViewModel() {
    }
}
