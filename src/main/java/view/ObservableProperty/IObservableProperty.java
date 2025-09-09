package il.ac.hit.project.main.view.ObservableProperty;

/**
 * An observable property that can be listened to for changes.
 * <p>
 * This interface provides a basic contract for exposing a value that can be
 * observed by other objects.
 * <p>
 * The interface is designed to be used by UI components that need to
 * react to changes in a model, without having to know the details of
 * the model itself.
 *
 * @param <T> the type of the property
 */
public interface IObservableProperty<T> {

    /**
     * Get the current value of the property.
     *
     * @return the current value of the property
     */
    T get();

    /**
     * Set the value of the property.
     *
     * @param value the new value of the property
     */
    void setValue(T value);

    /**
     * Add a listener to the property.
     *
     * @param listener the listener to add
     */
    void addListener(IPropertyObserver<T> listener);

    /**
     * Remove a listener from the property.
     *
     * @param listener the listener to remove
     */
    void removeListener(IPropertyObserver<T> listener);

    /**
     * Remove all listeners from the property.
     */
    void clearListeners();

    /**
     * Notify all listeners that the property has changed.
     */
    void notifyListeners();
}
