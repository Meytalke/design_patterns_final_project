package il.ac.hit.project.main.view;

import il.ac.hit.project.main.model.task.*;
import il.ac.hit.project.main.viewmodel.IViewModel;
import il.ac.hit.project.main.viewmodel.TasksViewModel;
import il.ac.hit.project.main.viewmodel.strategy.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Swing-based il.ac.hit.project.main.view for managing tasks in an MVVM setup.
 * <p>
 * This component composes the task management UI, including:
 * <ul>
 *   <li>Inputs for creating and editing tasks (title, description, state, priority).</li>
 *   <li>Search fields (by id, title, description) and a state filter.</li>
 *   <li>Sorting controls to reorder the visible task list.</li>
 *   <li>Action buttons for adding, updating, deleting, bulk-deleting, navigating, and reporting/exporting.</li>
 *   <li>A list displaying the current tasks with the ability to select one for editing.</li>
 * </ul>
 *
 * <h3>Responsibilities</h3>
 * <ul>
 *   <li>Implements the View side of MVVM via {@link IView}.</li>
 *   <li>Forwards user intents (add/update/delete/filter/sort/export) to the bound {@link IViewModel}.</li>
 * </ul>
 * @see IView
 */
public class TaskManagerView extends JPanel implements IView {

    /**
     * The main application window.
     */
    private final JFrame window;
    /**
     * The main content pane for the window.
     */
    private final JPanel contentPane;
    /**
     * Text field for entering the task title.
     */
    private final JTextField taskTitleInputF;
    /**
     * Text area for entering the task description.
     */
    private final JTextArea descriptionInputTA;
    /**
     * Button to add a new task.
     */
    private final JButton addButton;
    /**
     * Button to update an existing task.
     */
    private final JButton updateButton;
    /**
     * Button to delete the selected task.
     */
    private final JButton deleteButton;
    /**
     * Button to delete all tasks.
     */
    private final JButton deleteAllButton;
    /**
     * Button to generate a report.
     */
    private final JButton reportButton;
    /**
     * Button to move a task up in the list.
     */
    private final JButton upButton;
    /**
     * Button to move a task down in the list.
     */
    private final JButton downButton;
    /**
     * Button to deselect the current task.
     */
    private final JButton deselectButton;
    /**
     * Combo box to filter tasks by state.
     */
    private final JComboBox<String> stateFilterComboBox;
    /**
     * Combo box to select a sorting option.
     */
    private final JComboBox<SortingOption> sortComboBox;
    /**
     * Combo box to select the report export format.
     */
    private final JComboBox<String> exportFormatComboBox;
    /**
     * Combo box to select the task state.
     */
    private final JComboBox<TaskState> taskStateComboBox;
    /**
     * The initial "To Do" state for a new task.
     */
    private final TaskState selectedTaskState = new ToDoState();
    /**
     * The visual list component displaying tasks.
     */
    private final JList<ITask> taskList;
    /**
     * The model for the task list.
     */
    private DefaultListModel<ITask> listModel;
    /**
     * Text field for searching by title.
     */
    private final JTextField searchTitleInput;
    /**
     * Text field for searching by description.
     */
    private final JTextField searchDescriptionInput;
    /**
     * Text field for searching by ID.
     */
    private final JTextField searchIdInput;
    /**
     * Sorting strategy for sorting by ID.
     */
    private final ISortingStrategy sortById =  new SortByIDStrategy();
    /**
     * Sorting strategy for sorting by state.
     */
    private final ISortingStrategy sortByState =  new SortByStateStrategy();
    /**
     * Sorting strategy for sorting by title.
     */
    private final ISortingStrategy sortByTitleStrat =  new SortByTitleStrategy();

    /**
     * The associated ViewModel.
     */
    private IViewModel viewModel;

    /**
     * A map to store different sorting strategies, keyed by a sorting option enum.
     * This allows for easy retrieval of the correct strategy at runtime.
     */
    private final Map<SortingOption, ISortingStrategy> strategies = new HashMap<>();

    /**
     * Constructs the task manager il.ac.hit.project.main.view and initializes the UI hierarchy.
     * <p>
     * This constructor creates and lays out all Swing components, configures renderers for
     * {@link TaskState} selector, initializes the sorting and filtering
     * controls, and prepares the list il.ac.hit.project.main.model for displaying tasks.
     * <p>
     */
    public TaskManagerView() {

        //Create UI base components
        contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        //Setting up add task area
        taskTitleInputF = new JTextField(20);
        descriptionInputTA = new JTextArea(3, 20);

        //Setting up a search area
        searchTitleInput = new JTextField(15);
        searchDescriptionInput = new JTextField(15);
        searchIdInput = new JTextField(5);

        //Setting up control buttons
        addButton = new JButton("Add Task");
        updateButton = new JButton("Update Task");
        deleteButton = new JButton("Delete Selected");
        deleteAllButton = new JButton("Delete All");
        reportButton = new JButton("Generate Report");
        deselectButton = new JButton("Deselect Task");
        upButton = new JButton("↑");
        downButton = new JButton("↓");

        // Filter by human-readable workflow state labels.
        stateFilterComboBox = new JComboBox<>(new String[]{"All", "To Do", "In Progress", "Completed"});

        /*
         * Task state editor.
         *
         * This combo box is populated with concrete TaskState instances rather than enum values.
         * It assumes a linear "next()" progression across the workflow:
         *   ToDoState -> InProgressState -> CompletedState
         *
         * The initial state is obtained from getSelectedTaskState(), and subsequent states are
         * derived via next() calls to present a canonical ordered list for selection.
         */
        taskStateComboBox = new JComboBox<TaskState>(new TaskState[]{
                getSelectedTaskState(),//ToDoState
                getSelectedTaskState().next(), //InProgressState
                getSelectedTaskState().next().next() //CompletedState
        });

        /*
         * Custom renderer for taskStateComboBox:
         * Displays a user-friendly name for each TaskState via getDisplayName().
         * Falls back to the default behavior if the value is not a TaskState.
         */
        taskStateComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof TaskState state) {
                    setText(state.getDisplayName());
                }
                return this;
            }
        });

        /*
         * Sorting option selector.
         *
         * Populates from the available sorting options (e.g., by date, priority, title).
         * The renderer shows a user-facing display name for each option.
         */
        sortComboBox = new JComboBox<>(SortingOption.values());
        sortComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof SortingOption option) {
                    setText(option.getDisplayName());
                }
                return this;
            }
        });

        exportFormatComboBox = new JComboBox<>(new String[]{"Terminal","PDF", "CSV" , "JSON"});

        //Setting up the visual listModel object and the taskList JList visual object.
        listModel = new DefaultListModel<>();
        taskList = new JList<>(listModel);
        taskList.setCellRenderer(new TaskCellRenderer());

        window = new JFrame("Tasks Manager");

        // Initializes the strategy map with concrete implementations of the ISortingStrategy interface.
        // This sets up the available sorting algorithms
        strategies.put(SortingOption.STATE, new SortByStateStrategy());
        strategies.put(SortingOption.ID, new SortByIDStrategy());
        strategies.put(SortingOption.TITLE, new SortByTitleStrategy());
    }

    /**
     * Selects in the combo box the first TaskState whose runtime class matches {@code stateToSelect}.
     * <p>Comparison is by class identity (state.getClass() == stateToSelect.getClass()).
     * Does nothing if no match is found.
     *
     * @param stateToSelect the non-null state whose class should be selected
     */
    public void selectTaskStateInComboBox(TaskState stateToSelect) {
        for (int i = 0; i < taskStateComboBox.getItemCount(); i++) {
            TaskState state = taskStateComboBox.getItemAt(i);
            if (state.getClass() == stateToSelect.getClass()) {
                taskStateComboBox.setSelectedIndex(i);
                return;
            }
        }
    }

    /**
     * Sets up the initial UI components and layout.
     * <p>
     * This includes:
     * <ul>
     *     <li>A panel for adding/updating tasks with input fields for title, description, state, and priority.</li>
     *     <li>A panel for searching/filtering tasks with input fields for search title, description, and ID, as well as a combo box for selecting a task state to filter by.</li>
     *     <li>A panel for displaying the current task list with a JList component.</li>
     *     <li>A panel for controlling the task list with action buttons (add, update, delete, delete all, report, export) and a combo box for selecting the export format.</li>
     * </ul>
     * <p>
     * Also sets up event listeners for the buttons and combo boxes and initializes the ViewModel with the initial data.
     */
    public void start() {
        JPanel addTaskPanel = new JPanel(new GridBagLayout());
        addTaskPanel.setBorder(BorderFactory.createTitledBorder("Add/Update Task"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Row 1: Task Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        addTaskPanel.add(new JLabel("Task Title:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        addTaskPanel.add(taskTitleInputF, gbc);

        // Row 2: Task Description
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        addTaskPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        addTaskPanel.add(new JScrollPane(descriptionInputTA), gbc);

        // Row 3: Task State
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        addTaskPanel.add(new JLabel("Task State:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        addTaskPanel.add(taskStateComboBox, gbc);


        // Row 4: Action Buttons
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.EAST;
        JPanel actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        actionButtonsPanel.add(addButton);
        actionButtonsPanel.add(updateButton);
        actionButtonsPanel.add(upButton);
        actionButtonsPanel.add(downButton);
        actionButtonsPanel.add(deselectButton);
        addTaskPanel.add(actionButtonsPanel, gbc);

        // Set the initial state of buttons
        updateButton.setEnabled(false);
        taskStateComboBox.setEnabled(false);
        upButton.setEnabled(false);
        downButton.setEnabled(false);
        deselectButton.setEnabled(false);

        // Panel for search and filter controls
        // Changed to GridBagLayout
        JPanel searchAndFilterPanel = new JPanel(new GridBagLayout());
        searchAndFilterPanel.setBorder(BorderFactory.createTitledBorder("Search and Filter"));

        // New GBC for this panel
        GridBagConstraints searchGBC = new GridBagConstraints();
        // Padding
        searchGBC.insets = new Insets(5, 5, 5, 5);

        // --- Row 1: Search Title ---
        searchGBC.gridx = 0; searchGBC.gridy = 0; searchGBC.anchor = GridBagConstraints.WEST;
        searchAndFilterPanel.add(new JLabel("Search Title:"), searchGBC);
        searchGBC.gridx = 1; searchGBC.gridy = 0; searchGBC.fill = GridBagConstraints.HORIZONTAL; searchGBC.weightx = 1.0;
        searchAndFilterPanel.add(searchTitleInput, searchGBC);

        // --- Row 2: Search Description ---
        searchGBC.gridx = 0; searchGBC.gridy = 1; searchGBC.anchor = GridBagConstraints.WEST;
        // Reset fill/weightx
        searchGBC.fill = GridBagConstraints.NONE; searchGBC.weightx = 0;
        searchAndFilterPanel.add(new JLabel("Search Description:"), searchGBC);
        searchGBC.gridx = 1; searchGBC.gridy = 1; searchGBC.fill = GridBagConstraints.HORIZONTAL; searchGBC.weightx = 1.0;
        searchAndFilterPanel.add(searchDescriptionInput, searchGBC);

        // --- Row 3: Search ID ---
        searchGBC.gridx = 0; searchGBC.gridy = 2; searchGBC.anchor = GridBagConstraints.WEST;
        searchGBC.fill = GridBagConstraints.NONE; searchGBC.weightx = 0;
        searchAndFilterPanel.add(new JLabel("Search ID:"), searchGBC);
        searchGBC.gridx = 1; searchGBC.gridy = 2; searchGBC.fill = GridBagConstraints.HORIZONTAL; searchGBC.weightx = 1.0;
        searchAndFilterPanel.add(searchIdInput, searchGBC);

        // --- Row 4: Filter by State ---
        searchGBC.gridx = 0; searchGBC.gridy = 3; searchGBC.anchor = GridBagConstraints.WEST;
        searchGBC.fill = GridBagConstraints.NONE; searchGBC.weightx = 0;
        searchAndFilterPanel.add(new JLabel("Filter by State:"), searchGBC);
        searchGBC.gridx = 1; searchGBC.gridy = 3; searchGBC.fill = GridBagConstraints.HORIZONTAL; searchGBC.weightx = 1.0;
        searchAndFilterPanel.add(stateFilterComboBox, searchGBC);

        // --- Row 5: Sort by ---
        searchGBC.gridx = 0; searchGBC.gridy = 4; searchGBC.anchor = GridBagConstraints.WEST;
        searchGBC.fill = GridBagConstraints.NONE; searchGBC.weightx = 0;
        searchAndFilterPanel.add(new JLabel("Sort by:"), searchGBC);
        searchGBC.gridx = 1; searchGBC.gridy = 4; searchGBC.fill = GridBagConstraints.HORIZONTAL; searchGBC.weightx = 1.0;
        searchAndFilterPanel.add(sortComboBox, searchGBC);


        // Top panel containing both the add/update and search panels
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.add(addTaskPanel, BorderLayout.NORTH);
        topPanel.add(searchAndFilterPanel, BorderLayout.CENTER);

        // Center panel for the task list
        JPanel taskListPanel = new JPanel(new BorderLayout());
        taskListPanel.setBorder(BorderFactory.createTitledBorder("Task List"));
        taskListPanel.add(new JScrollPane(taskList), BorderLayout.CENTER);

        // Bottom panel for action buttons
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        actionsPanel.add(deleteButton);
        actionsPanel.add(deleteAllButton);
        actionsPanel.add(reportButton);
        actionsPanel.add(exportFormatComboBox);

        // Assemble the main content pane
        contentPane.add(topPanel, BorderLayout.NORTH);
        contentPane.add(taskListPanel, BorderLayout.CENTER);
        contentPane.add(addTaskPanel, BorderLayout.WEST);
        contentPane.add(actionsPanel, BorderLayout.SOUTH);

        window.setContentPane(contentPane);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);

        // --- Event Listeners ---
        // Create a new task from input fields, delegate to the ViewModel, then clear the form.
        TasksViewModel viewModel = (TasksViewModel) getViewModel();
        addButton.addActionListener(e -> {
            String title = taskTitleInputF.getText();
            String description = descriptionInputTA.getText();
            viewModel.addButtonPressed(title, description);

            resetForm();
        });

        // Update the currently selected task with edited fields; if no task is selected, do nothing.
        updateButton.addActionListener(e -> {
            if (viewModel.getSelectedTask().get() != null) {
                String title = taskTitleInputF.getText();
                String description = descriptionInputTA.getText();
                TaskState state = (TaskState) taskStateComboBox.getSelectedItem();
                viewModel.updateButtonPressed(viewModel.getSelectedTask().get().getId(), title, description, state);

                resetForm();
            }
        });

        // Request the ViewModel to move the selected task "up" in the workflow, then clear the form.
        upButton.addActionListener(e -> {
            if (viewModel.getSelectedTask().get() != null) {
                viewModel.upButtonPressed();
                resetForm();
            }
        });

        // Request the ViewModel to move the selected task "down" in the workflow, then clear the form.
        downButton.addActionListener(e -> {
            if (viewModel.getSelectedTask().get() != null) {
                viewModel.downButtonPressed();
                resetForm();
            }
        });

        /*
         * An action listener for the sort combo box.
         * When a user selects a sorting option, this listener retrieves the
         * corresponding sorting strategy from the map and applies it to the
         * ViewModel. This demonstrates a flexible, polymorphic approach
         * to changing the sorting behavior without modifying the core logic.
         *
         * @param e The action event is triggered by the user's selection.
         */
        sortComboBox.addActionListener(e -> {
            SortingOption selectedOption = (SortingOption) sortComboBox.getSelectedItem();

            ISortingStrategy selectedStrategy = strategies.get(selectedOption);
            if (selectedStrategy != null) {
                viewModel.setSortingStrategy(selectedStrategy);
            }
        });

        // Delete the selected task; if none is selected, show a warning dialog. Always clear the form afterward.
        deleteButton.addActionListener(e -> {
            if (viewModel.getSelectedTask().get() != null) {
                viewModel.deleteButtonPressed();
                resetForm();
            } else {
                JOptionPane.showMessageDialog(window, "Please select a task to delete.", "No Task Selected", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Confirm and delete all tasks. Clears the form after a successful deletion.
        deleteAllButton.addActionListener(e -> {
            int response = JOptionPane.showConfirmDialog(window, "Are you sure you want to delete all tasks?", "Confirm Delete All", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                viewModel.deleteAllTasks();
                resetForm();
            }
        });

        // Deselect a task from the viewport
        deselectButton.addActionListener(e->{
            viewModel.getSelectedTask().setValue(null);
        });

        // Generate a report in the selected export format.
        reportButton.addActionListener(e -> {
            String selectedFormat = (String) exportFormatComboBox.getSelectedItem();
            viewModel.generateReport(selectedFormat);
        });

        // When the task list selection settles, populate the form with the task data and enable editing controls.
        // If the selection is cleared, reset the form to its initial state.
        taskList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                //While value has changed but is not being actively tweaked
                if (!e.getValueIsAdjusting()) {
                    //Use the observer viewModel function that goes through the observer the update the UI.
                    viewModel.getSelectedTask().setValue(taskList.getSelectedValue());
                }
            }
        });

        // Re-apply filters whenever any of the filter controls trigger an action (state or search fields).
        stateFilterComboBox.addActionListener(e -> applyAllFilters());
        searchTitleInput.addActionListener(e -> applyAllFilters());
        searchDescriptionInput.addActionListener(e -> applyAllFilters());
        searchIdInput.addActionListener(e -> applyAllFilters());

        // Initial data load: fetch tasks into the ViewModel (and indirectly refresh the il.ac.hit.project.main.view via observers/bindings).
        viewModel.loadTasks();
    }

    /**
     * Updates the task list displayed in the UI with the given collection of tasks.
     * <p>
     * This method clears the current list model and repopulates it with the provided tasks.
     * The update is scheduled to run on the Event Dispatch Thread (EDT) using
     * {@link SwingUtilities#invokeLater(Runnable)} to ensure thread safety when modifying
     * Swing components.
     *
     * @param tasks the list of {@link ITask} objects to display in the task list;
     *              if empty, the list will simply be cleared
     */
    @Override
    public void setTasks(java.util.List<ITask> tasks) {
        SwingUtilities.invokeLater(() -> {
            getListModel().clear();
            for (ITask task : tasks) {
                getListModel().addElement(task);
            }
        });
    }

    /**
     * Displays a message dialog to the user with a title and icon based on the specified message type.
     * <p>
     * The dialog is shown using {@link JOptionPane} and adapts its appearance as follows:
     * <ul>
     *   <li>{@link MessageType#SUCCESS} → Title: "Success", Information icon</li>
     *   <li>{@link MessageType#ERROR} → Title: "Error", Error icon</li>
     *   <li>{@link MessageType#WARNING} → Title: "Warning", Warning icon</li>
     *   <li>{@link MessageType#INFO} or unspecified → Title: "Information", Information icon</li>
     * </ul>
     *
     * @param message the text message to display in the dialog
     * @param type    the {@link MessageType} that determines the dialog's title and icon
     */
    @Override
    public void showMessage(String message, MessageType type) {
        String title;
        int messageType;

        switch (type) {
            case SUCCESS:
                title = "Success";
                messageType = JOptionPane.INFORMATION_MESSAGE;
                break;
            case ERROR:
                title = "Error";
                messageType = JOptionPane.ERROR_MESSAGE;
                break;
            case WARNING:
                title = "Warning";
                messageType = JOptionPane.WARNING_MESSAGE;
                break;
            case INFO:
            default:
                title = "Information";
                messageType = JOptionPane.INFORMATION_MESSAGE;
                break;
        }

        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    /**
     * Applies all current filter settings to the ViewModel, causing the visible task list to
     * be refreshed accordingly.
     * <p>
     * This method is called in response to user actions on the filter controls.
     */
    private void applyAllFilters() {
        String selectedState = (String) stateFilterComboBox.getSelectedItem();
        String titleTerm = searchTitleInput.getText();
        String descriptionTerm = searchDescriptionInput.getText();
        String idTerm = searchIdInput.getText();
        ((TasksViewModel) getViewModel()).filterTasks(selectedState, titleTerm, descriptionTerm, idTerm);
    }

    /**
     * Populates the form fields and updates control states based on the provided task.
     * <p>
     * If a valid {@link ITask} is provided, this method:
     * <ul>
     *   <li>Sets the task title and description input fields with the task's values.</li>
     *   <li>Selects the corresponding task state in the state combo box.</li>
     *   <li>Enables or disables action buttons appropriately:
     *       <ul>
     *         <li>Enables: state combo box, update, delete, up, down, and deselect buttons.</li>
     *         <li>Disables: add button.</li>
     *       </ul>
     *   </li>
     * </ul>
     * If the task is {@code null}, the form is reset to its default state.
     *
     * @param task the {@link ITask} whose data should populate the form,
     *             or {@code null} to reset the form.
     */
    @Override
    public void setFormData(ITask task) {
        if (task != null) {
            getTaskTitleInputF().setText(task.getTitle());
            getDescriptionInputTA().setText(task.getDescription());
            selectTaskStateInComboBox(task.getState());
            // Set button states
            getTaskStateComboBox().setEnabled(true);
            getAddButton().setEnabled(false);
            getUpdateButton().setEnabled(true);
            getDeleteButton().setEnabled(true);
            getUpButton().setEnabled(true);
            getDownButton().setEnabled(true);
            getDeselectButton().setEnabled(true);
        } else {
            resetForm();
        }
    }

    /**
     * Resets the form to its initial state: clears input fields, selects the first
     * option in the state and priority combo boxes, disables the state combo box,
     * and enables/disables the control buttons as appropriate. Also clears the
     * selection in the task list.
     */
    @Override
    public void resetForm() {
        taskTitleInputF.setText("");
        descriptionInputTA.setText("");
        taskStateComboBox.setSelectedIndex(0);
        taskStateComboBox.setEnabled(false);
        addButton.setEnabled(true);
        updateButton.setEnabled(false);
        downButton.setEnabled(false);
        upButton.setEnabled(false);
        deselectButton.setEnabled(false);
        stateFilterComboBox.setSelectedIndex(0);
        searchTitleInput.setText("");
        searchDescriptionInput.setText("");
        searchIdInput.setText("");
        taskList.clearSelection();
    }

    /**
     * Returns the ViewModel associated with this il.ac.hit.project.main.view.
     *
     * @return the ViewModel
     */
    @Override
    public IViewModel getViewModel() {
        return viewModel;
    }

    /**
     * Associates this View with a ViewModel.
     *
     * @param viewModel the ViewModel to set; must not be null
     */
    @Override
    public void setViewModel(IViewModel viewModel) {
        this.viewModel = viewModel;
    }

    /**
     * Returns the input field for the task title.
     *
     * @return the text field for task title input
     */
    public JTextField getTaskTitleInputF() {
        return taskTitleInputF;
    }


    /**
     * Returns the text area for the task description input.
     *
     * @return the text area for task description input
     */
    public JTextArea getDescriptionInputTA() {
        return descriptionInputTA;
    }

    /**
     * Returns the button that triggers adding a new task to the ViewModel.
     *
     * @return the add task button
     */
    public JButton getAddButton() {
        return addButton;
    }

    /**
     * Returns the button that triggers updating the selected task in the ViewModel.
     *
     * @return the update task button
     */
    public JButton getUpdateButton() {
        return updateButton;
    }



    /**
     * Returns the list il.ac.hit.project.main.model for the task list component.
     * <p>
     * The list il.ac.hit.project.main.model contains the tasks that are currently visible in the
     * task list. The ViewModel uses this il.ac.hit.project.main.model to update the task list
     * component when the visible tasks change.
     *
     * @return the list il.ac.hit.project.main.model for the task list
     */
    public DefaultListModel<ITask> getListModel() {
        return listModel;
    }

    /**
     * Sets the list il.ac.hit.project.main.model for the task list component.
     * <p>
     * This method is intended for use by the ViewModel to update the task list
     * when the visible tasks change. The new list il.ac.hit.project.main.model will be used to update
     * the task list component.
     *
     * @param listModel the list il.ac.hit.project.main.model to set; must not be null
     */
    public void setListModel(DefaultListModel<ITask> listModel) {
        this.listModel = listModel;
    }

    /**
     * Returns the text field for searching tasks by title.
     *
     * @return the text field for searching tasks by title
     */
    public JTextField getSearchTitleInput() {
        return searchTitleInput;
    }

    /**
     * Getter for the text field
     *
     * @return the text field for searching tasks by description
     */
    public JTextField getSearchDescriptionInput() {
        return searchDescriptionInput;
    }


    /**
     * Allows users to enter an ID to search for a task by its ID.
     *
     * @return the text field for searching tasks by ID
     */
    public JTextField getSearchIdInput() {
        return searchIdInput;
    }

    /**
     * Returns the combo box that allows users to select a task state
     * to filter the task list by.
     *
     * @return the combo box for selecting a task state to filter by
     */
    public JComboBox<TaskState> getTaskStateComboBox() {
        return taskStateComboBox;
    }

    /**
     * Returns the button for deleting the selected task.
     *
     * @return the delete task button
     */
    public JButton getDeleteButton() {
        return deleteButton;
    }

    /**
     * Returns the button for moving the selected task up in the task list.
     *
     * @return the button for moving the selected task up
     */
    public JButton getUpButton() {
        return upButton;
    }

    /**
     * Returns the button for moving the selected task down in the task list.
     *
     * @return the button for moving the selected task down
     */
    public JButton getDownButton() {
        return downButton;
    }

    /**
     * Returns the button for deselecting the currently selected task.
     *
     * @return the button for deselecting the task
     */
    public JButton getDeselectButton() {
        return deselectButton;
    }

    /**
     * Returns the button for deleting all tasks in the task list.
     *
     * @return the button for deleting all tasks
     */
    public JButton getDeleteAllButton() {
        return deleteAllButton;
    }

    /**
     * Returns the sorting strategy for sorting tasks by ID (highest to lowest).
     *
     * @return the sorting strategy for sorting tasks by ID
     */
    public ISortingStrategy getSortByIdStrat() {
        return sortById;
    }

    /**
     * Returns the sorting strategy for sorting tasks by their state.
     * The sorting order is: "To Do", followed by "In Progress", and then "Complete".
     *
     * @return the sorting strategy for sorting tasks by their state
     */
    public ISortingStrategy getSortByStateStrat() {
        return sortByState;
    }

    /**
     * Returns the sorting strategy for sorting tasks by title.
     * The tasks are sorted alphabetically by their title (A-Z).
     *
     * @return the sorting strategy for sorting tasks by title
     */
    public ISortingStrategy getSortByTitleStrat() {
        return sortByTitleStrat;
    }


    /**
     * Returns the currently selected {@link TaskState} in the UI state filter combo box.
     *
     * @return the selected task state
     */

    public TaskState getSelectedTaskState() {return selectedTaskState;}
    /**
     * Returns the visual list component that displays tasks.
     * <p>
     * This method is primarily used by tests and the view's internal logic
     * to access and manipulate the JList component directly.
     *
     * @return the JList component displaying the tasks.
     */
    public JList<ITask> getTaskList() {
        return taskList;
    }

    /**
     * Returns the combo box used to filter tasks by their state.
     * <p>
     * This method is used by the view's event listeners and tests to
     * retrieve the currently selected filter state.
     *
     * @return the JComboBox component for state filtering.
     */
    public JComboBox<String> getStateFilterComboBox() {
        return stateFilterComboBox;
    }
}