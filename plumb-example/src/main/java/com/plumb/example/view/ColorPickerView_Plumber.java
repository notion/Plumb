package com.plumb.example.view;

import com.plumb.example.viewmodel.ColorPickerViewModel;

import plumb.Plumber;
import plumb.Utils;
import rx.subscriptions.CompositeSubscription;

public class ColorPickerView_Plumber implements Plumber<ColorPickerView, ColorPickerViewModel> {

    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Override
    public void plumb(ColorPickerView view, ColorPickerViewModel viewModel) {
        subscriptions.add(Utils.replicate(view.red, viewModel.red));
        subscriptions.add(Utils.replicate(view.green, viewModel.green));
        subscriptions.add(Utils.replicate(view.blue, viewModel.blue));
        subscriptions.add(Utils.replicate(viewModel.color, view.color));
    }

    @Override
    public void demolish() {
        subscriptions.unsubscribe();
    }
}
