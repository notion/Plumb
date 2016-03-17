package rxjoin.example.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import com.jakewharton.rxbinding.widget.RxSeekBar;
import rx.Observable;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;
import rxjoin.RxJoin;
import rxjoin.annotation.In;
import rxjoin.annotation.Joined;
import rxjoin.annotation.Out;
import rxjoin.example.R;
import rxjoin.example.viewmodel.ColorPickerViewModel;

@Joined(ColorPickerViewModel.class) public class ColorPickerView extends RelativeLayout {

    public ImageView colorView;

    public ColorPickerViewModel viewModel = ColorPickerViewModel.getInstance();

    @Out("red") public Observable<Integer> red;
    @Out("green") public Observable<Integer> green;
    @Out("blue") public Observable<Integer> blue;

    @In("color") public BehaviorSubject<Integer> color = BehaviorSubject.create();

    private CompositeSubscription subscriptions = new CompositeSubscription();

    public ColorPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override public void onAttachedToWindow() {
        super.onAttachedToWindow();
        colorView = (ImageView) findViewById(R.id.target);
        SeekBar redSeekBar = (SeekBar) findViewById(R.id.red_seek_bar);
        SeekBar greenSeekBar = (SeekBar) findViewById(R.id.green_seek_bar);
        SeekBar blueSeekBar = (SeekBar) findViewById(R.id.blue_seek_bar);

        red = RxSeekBar.changes(redSeekBar);
        green = RxSeekBar.changes(greenSeekBar);
        blue = RxSeekBar.changes(blueSeekBar);

        //(color) -> { colorView.setBackgroundColor(color) }
        subscriptions.add(color.subscribe(new Action1<Integer>() {
            @Override public void call(Integer color) {
                colorView.setBackgroundColor(color);
            }
        }));
        RxJoin.join(this, viewModel);
    }

    @Override public void onDetachedFromWindow() {
        subscriptions.unsubscribe();
        RxJoin.demolish(this);
        super.onDetachedFromWindow();
    }
}
