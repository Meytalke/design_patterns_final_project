package view.ObservableProperty;

import java.util.function.Consumer;

public interface IObservableProperty<T> {


    T get();
    void setValue(T value);
    void addListener(Consumer<T> listener);


}
