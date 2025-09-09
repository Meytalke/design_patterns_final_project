package il.ac.hit.project.main.view.ObservableProperty;

import java.util.List;

/**
 * Interface to be implemented by an observable collection.
 *
 * <p>
 * This interface defines the minimum requirements for an observable collection.
 * It extends the {@link IObservableProperty} interface for a list of items of type {@code T}.
 * It adds two methods to append and remove items from the collection.
 *
 * @param <T> type of the items in the collection
 */
public interface IObservableCollection <T> extends IObservableProperty<List<T>> {

    /**
     * Gets the list of items in the collection.
     *
     * @return the list of items in the collection
     */
    @Override
    List<T> get();

    /**
     * Sets the list of items in the collection.
     *
     * @param value the new list of items in the collection
     */
    @Override
    void setValue(List<T> value);

    /**
     * Appends an item to the collection.
     *
     * @param value the item to append to the collection
     */
    void appendValue(T value);

    /**
     * Removes an item from the collection.
     *
     * @param value the item to remove from the collection
     */
    void removeValue(T value);

}
