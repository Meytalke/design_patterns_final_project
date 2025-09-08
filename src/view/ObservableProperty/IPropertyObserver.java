package view.ObservableProperty;

/**
 * A functional interface that allows you to instanciate an anonymous class object that
 * implements this interface. This enables you to override the {@link #update(Object)}
 * method with a lambda expression in order to update a specific UI component.
 *
 * @param <T> the type of the property that this observer is attached to
 */
public interface IPropertyObserver <T> {
    /**
     * This method is called whenever the value of the property that this observer is
     * attached to changes.
     *
     * @param value the new value of the property
     */
    void update(T value);
}
