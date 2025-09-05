package view.ObservableProperty;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ObservableProperty<T> implements IObservableProperty<T> {

    private T value;
    private final List<Consumer<T>> listeners = new ArrayList<>();

    public ObservableProperty(T value) {
        /*
        * Initializing observableProperty object with the value to store, and the UI component to */
        setValue(value);
    }


    @Override
    public T get() {
        return value;
    }

    @Override
    public void setValue(T newValue) {
        if((value == null && newValue!= null) || (value!=null && !value.equals(newValue))){
            value=newValue;
            for( Consumer<T> listener : listeners){
                listener.accept(value);
            }
        }
    }

    @Override
    public void addListener(Consumer<T> listener) {
        listeners.add(listener);
    }
}
