package il.ac.hit.project.main.view.ObservableProperty;

import java.util.ArrayList;
import java.util.List;


/**
 * Implementation of {@link IObservableCollection}. This class is a container for a list that notifies observers
 * when the list changes.
 * <p>
 * The list is stored in a field, and the observers are stored in another field.
 * When the list changes, the observers are notified by calling their update method with the new
 * value of the list.
 * <p>
 * The class provides methods to add, remove, and clear observers.
 * <p>
 * The class also provides a method to notify the observers, which can be called by subclasses.
 *
 * @param <T> the type of the elements in the list
 */
public class ObservableCollection<T> implements IObservableCollection<T> {

    private List<T> list =  new ArrayList<>();
    private final List<IPropertyObserver<List<T>>> listeners = new ArrayList<>();


    /**
     * Gets the list of items in the collection.
     *
     * @return the list of items in the collection
     */
    @Override
    public List<T> get() {
        return getList();
    }

    /**
     * Sets the list of items in the collection.
     *
     * @param value the new list of items in the collection
     */
    @Override
    public void setValue(List<T> value) {
        setList(value);
        notifyListeners();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void appendValue(T value) {
        getList().add(value);
        notifyListeners();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeValue(T value) {
        getList().remove(value);
        notifyListeners();
    }

    @Override
    public void clear() {
        getList().clear();
        notifyListeners();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void addListener(IPropertyObserver<List<T>> listener) {
        listeners.add(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeListener(IPropertyObserver<List<T>> listener) {
        listeners.remove(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearListeners() {
        listeners.clear();
    }


    /**
     * Notifies all listeners that the property has changed.
     * <p>
     * This method is used to notify all listeners when the list changes.
     * <p>
     * It iterates over the listeners and calls their update method with the new
     * value of the list.
     */
    @Override
    public void notifyListeners() {
        for(IPropertyObserver<List<T>> listener : listeners){
            listener.update(getList());
        }
    }

    /**
     * Gets the list of elements.
     * @return the list of elements
     */
    public List<T> getList() {
        return list;
    }

    /**
     * Sets the list of elements.
     * @param list the new list of elements
     */
    public void setList(List<T> list) {
        this.list = list;
    }
}