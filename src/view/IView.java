package view;

import viewmodel.IViewModel;

/**
 * A contract for the UI-facing side of an MVVM setup.
 * <p>
 * Implementations should manage the UI-related aspects of the application
 * and provide hooks for the ViewModel to update the UI.
 */
public interface IView {

    /**
     * Returns the ViewModel associated with this view.
     * 
     * @return the ViewModel
     */
    IViewModel getViewModel();

    /**
     * Associates this View with a ViewModel.
     * 
     * @param viewModel the ViewModel to set; must not be null
     */
    void setViewModel(IViewModel viewModel);

    /**
     * Starts the view.
     * <p>
     * This method should be called after the ViewModel is set and all
     * necessary UI components are initialized.
     */
    void start();
}
