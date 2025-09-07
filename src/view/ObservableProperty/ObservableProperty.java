package view.ObservableProperty;


import java.util.ArrayList;
import java.util.List;

public class ObservableProperty<T> implements IObservableProperty<T> {

    private T value;
    private final List<IPropertyObserver<T>> listeners = new ArrayList<>();

    public ObservableProperty(T value) {
        /*
        * Initializing observableProperty object with the value to store */
        setValue(value);
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public void setValue(T newValue) {
        //Ensure update only on relevant value changes
        if((value == null && newValue!= null) || (value!=null && !value.equals(newValue))){
            value=newValue;
            notifyListeners();
        }
    }


    @Override
    public void addListener(IPropertyObserver<T> listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(IPropertyObserver<T> listener) {
        listeners.remove(listener);
    }

    @Override
    public void clearListeners() {
        listeners.clear();
    }

    @Override
    public void notifyListeners() {
        for(IPropertyObserver<T> listener : listeners){
            listener.update(get());
        }
    }
}
