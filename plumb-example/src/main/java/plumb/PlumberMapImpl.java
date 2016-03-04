package plumb;

import com.plumb.example.view.ColorPickerView;
import com.plumb.example.view.ColorPickerView_Plumber;

import java.util.HashMap;
import java.util.Map;

public class PlumberMapImpl implements PlumberMap {
    private Map<Class, Plumber> plumberMap = new HashMap<>();

    public PlumberMapImpl() {
        plumberMap.put(ColorPickerView.class, new ColorPickerView_Plumber());
    }

    @Override
    public <T, R> Plumber<T, R> plumberFor(T t) {
        return plumberMap.get(t.getClass());
    }
}
