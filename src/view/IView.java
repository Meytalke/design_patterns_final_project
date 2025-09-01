package view;
import viewmodel.IViewModel;

public interface IView {

    public IViewModel getViewModel();
    public void setViewModel(IViewModel viewModel);
    public void start();
}


