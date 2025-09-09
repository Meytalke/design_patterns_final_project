package il.ac.hit.project.main.view.ObservableProperty;


import java.util.ArrayList;
import java.util.List;

/**
 * A generic implementation of the {@link IObservableProperty} interface.
 *
 * <p>
 * This class is a generic implementation of the {@link IObservableProperty} interface.
 * It provides a basic implementation for a property that can be observed by other objects.
 *
 * @param <T> the type of the property
 */
public class ObservableProperty<T> implements IObservableProperty<T> {

    private T value;
    private final List<IPropertyObserver<T>> listeners = new ArrayList<>();


    /**
     * Initializes an observable property with the given value.
     *
     * @param value the initial value of the property
     */
    public ObservableProperty(T value) {
        /*
        * Initializing observableProperty object with the value to store */
        setValue(value);
    }

    @Override
    /**
     * Gets the current value of the property.
     *
     * @return the current value of the property
     */
    public T get() {
        return value;
    }

    @Override
    /**
     * Sets the value of the property.
     *
     * @param newValue the new value of the property
     */
    public void setValue(T newValue) {
        //Ensure update only on relevant value changes
        if((value == null && newValue!= null) || (value!=null && !value.equals(newValue))){
            value=newValue;
            notifyListeners();
        }
    }


    @Override
    /**
     * Adds a listener to the property.
     *
     * @param listener the listener to add
     */
    public void addListener(IPropertyObserver<T> listener) {
        listeners.add(listener);
    }

    @Override
    /**
     * Removes a listener from the property.
     *
     * @param listener the listener to remove
     */
    public void removeListener(IPropertyObserver<T> listener) {
        listeners.remove(listener);
    }

    @Override
    /**
     * Removes all listeners from the property.
     */
    public void clearListeners() {
        listeners.clear();
    }

    @Override
    /**
     * Notifies all listeners that the property has changed.
     */
    public void notifyListeners() {
        for(IPropertyObserver<T> listener : listeners){
            listener.update(get());
        }
    }
}
