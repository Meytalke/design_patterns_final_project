package view;

import model.task.*;
import viewmodel.IViewModel;
import viewmodel.TasksViewModel;
import viewmodel.strategy.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.List;

/**
 * Swing-based view for managing tasks in an MVVM setup.
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
 *   <li>Observes task updates via {@link TasksObserver} and refreshes the list accordingly.</li>
 *   <li>Forwards user intents (add/update/delete/filter/sort/export) to the bound {@link IViewModel}.</li>
 * </ul>
 * @see IView
 * @see TasksObserver
 */
public class TaskManagerView extends JPanel implements TasksObserver, IView {

    private final JFrame window;
    private final JPanel contentPane;
    //Task title and description input field and textArea
    private final JTextField taskTitleInputF;
    private final JTextArea descriptionInputTA;
    //Control buttons - addTask, updateTask, deleteTask, deleteAllTasks, report, up, down ,deselect
    private final JButton addButton;
    private final JButton updateButton;
    private final JButton deleteButton;
    private final JButton deleteAllButton;
    private final JButton reportButton;
    private final JButton upButton,downButton;
    private final JButton deselectButton;
    //Task state in string value, convert to IState to control more accurate filtering
    private final JComboBox<String> stateFilterComboBox;
    private final JComboBox<SortingOption> sortComboBox;
    private final JComboBox<String> exportFormatComboBox;
    // Task State in string value, convert to IState to control action behavior on each task
    // Or just make a visual difference depending on the state (color).
    private final JComboBox<TaskState> taskStateComboBox;
    private final TaskState selectedTaskState = new ToDoState();
    private final JComboBox<TaskPriority> taskPriorityComboBox;
    private final JLabel creationDateLabel;
    private ITask selectedTask = null;

    //Task list in memory and a listModel list to store the tasks visually
    private JList<ITask> taskList;
    private DefaultListModel<ITask> listModel;

    //Search specific regex fields
    private final JTextField searchTitleInput;
    private final JTextField searchDescriptionInput;
    private final JTextField searchIdInput;

    //Sorting strategies to shuffle through
    private final ISortingStrategy sortByCreationStrat =  new SortByCreationDateStrategy();
    private final ISortingStrategy sortByPriorityStrat =  new SortByPriorityStrategy();
    private final ISortingStrategy sortByTitleStrat =  new SortByTitleStrategy();

    //Interface over class
    private IViewModel viewModel;

    /**
     * Constructs the task manager view and initializes the UI hierarchy.
     * <p>
     * This constructor creates and lays out all Swing components, configures renderers for
     * {@link TaskState} and {@link TaskPriority} selectors, initializes the sorting and filtering
     * controls, and prepares the list model for displaying tasks.
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
         * Task priority selector.
         *
         * Populates from the available priority values.
         * The custom renderer uses getDisplayName() to show a human-friendly label.
         */
        taskPriorityComboBox = new JComboBox<>(TaskPriority.values());
        taskPriorityComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof TaskPriority priority) {
                    setText(priority.getDisplayName());
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

        creationDateLabel = new JLabel("Creation Date: N/A");

        exportFormatComboBox = new JComboBox<>(new String[]{"Terminal","PDF", "CSV" , "JSON"});

        //Setting up the visual listModel object and the taskList JList visual object.
        listModel = new DefaultListModel<>();
        taskList = new JList<>(listModel);
        taskList.setCellRenderer(new TaskCellRenderer());

        window = new JFrame("Tasks Manager");
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

        // Row 4: Task Priority
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        addTaskPanel.add(new JLabel("Priority:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        addTaskPanel.add(taskPriorityComboBox, gbc);

        // Row 5: Creation Date
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.NONE;
        addTaskPanel.add(creationDateLabel, gbc);

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
            TaskPriority priority = (TaskPriority) taskPriorityComboBox.getSelectedItem();
            viewModel.addButtonPressed(title, description, priority);

            resetForm();
        });

        // Update the currently selected task with edited fields; if no task is selected, do nothing.
        updateButton.addActionListener(e -> {
            if (viewModel.getSelectedTask().get() != null) {
                String title = taskTitleInputF.getText();
                String description = descriptionInputTA.getText();
                TaskState state = (TaskState) taskStateComboBox.getSelectedItem();
                TaskPriority priority = (TaskPriority) taskPriorityComboBox.getSelectedItem();
                viewModel.updateButtonPressed(viewModel.getSelectedTask().get().getId(), title, description, state, priority);

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

        // Change the sorting strategy based on the selected option in the sort combo box.
        sortComboBox.addActionListener(e -> {
            SortingOption selectedOption = (SortingOption) sortComboBox.getSelectedItem();

            switch (selectedOption) {
                case PRIORITY:
                    viewModel.setSortingStrategy(getSortByPriorityStrat());
                    break;
                case CREATION_DATE:
                    viewModel.setSortingStrategy(getSortByCreationStrat());
                    break;
                case TITLE:
                    viewModel.setSortingStrategy(getSortByTitleStrat());
                    break;
                case null:
                    break;
                default:
                    viewModel.setSortingStrategy(getSortByTitleStrat());
                    break;
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

        // Initial data load: fetch tasks into the ViewModel (and indirectly refresh the view via observers/bindings).
        viewModel.loadTasks();
    }

    private void applyAllFilters() {
        String selectedState = (String) stateFilterComboBox.getSelectedItem();
        String titleTerm = searchTitleInput.getText();
        String descriptionTerm = searchDescriptionInput.getText();
        String idTerm = searchIdInput.getText();
        ((TasksViewModel) getViewModel()).filterTasks(selectedState, titleTerm, descriptionTerm, idTerm);
    }

    public void resetForm() {
        taskTitleInputF.setText("");
        descriptionInputTA.setText("");
        taskStateComboBox.setSelectedIndex(0);
        taskPriorityComboBox.setSelectedIndex(0);
        taskStateComboBox.setEnabled(false);
        addButton.setEnabled(true);
        updateButton.setEnabled(false);
        downButton.setEnabled(false);
        upButton.setEnabled(false);
        deselectButton.setEnabled(false);
        taskList.clearSelection();
    }

    /**
     * Called by the ViewModel when the list of tasks has been updated.
     * Schedules an update of the JList to show the new tasks.
     * @param tasks The new, updated list of tasks.
     */
    @Override
    public void onTasksChanged(List<ITask> tasks) {

        SwingUtilities.invokeLater(() -> {
            listModel.clear();
            for (ITask task : tasks) {
                listModel.addElement(task);
            }
        });
    }

    @Override
    public IViewModel getViewModel() {
        return viewModel;
    }

    @Override
    public void setViewModel(IViewModel viewModel) {
        this.viewModel = viewModel;
    }

    public JTextField getTaskTitleInputF() {
        return taskTitleInputF;
    }


    public JTextArea getDescriptionInputTA() {
        return descriptionInputTA;
    }

    public JButton getAddButton() {
        return addButton;
    }

    public JButton getUpdateButton() {
        return updateButton;
    }

    public ITask getSelectedTask() {
        return selectedTask;
    }

    public void setSelectedTask(ITask selectedTask) {
        this.selectedTask = selectedTask;
    }

    public JList<ITask> getTaskList() {
        return taskList;
    }

    public void setTaskList(JList<ITask> taskList) {
        this.taskList = taskList;
    }

    public DefaultListModel<ITask> getListModel() {
        return listModel;
    }

    public void setListModel(DefaultListModel<ITask> listModel) {
        this.listModel = listModel;
    }

    public JTextField getSearchTitleInput() {
        return searchTitleInput;
    }

    public JTextField getSearchDescriptionInput() {
        return searchDescriptionInput;
    }

    public JTextField getSearchIdInput() {
        return searchIdInput;
    }

    public JComboBox<TaskState> getTaskStateComboBox() {
        return taskStateComboBox;
    }

    public JButton getDeleteButton() {
        return deleteButton;
    }

    public JButton getUpButton() {
        return upButton;
    }

    public JButton getDownButton() {
        return downButton;
    }

    public JButton getDeselectButton() {
        return deselectButton;
    }

    public JComboBox<TaskPriority> getTaskPriorityComboBox() {
        return taskPriorityComboBox;
    }

    public JLabel getCreationDateLabel() {
        return creationDateLabel;
    }

    public JButton getDeleteAllButton() {
        return deleteAllButton;
    }

    public ISortingStrategy getSortByCreationStrat() {
        return sortByCreationStrat;
    }

    public ISortingStrategy getSortByPriorityStrat() {
        return sortByPriorityStrat;
    }

    public ISortingStrategy getSortByTitleStrat() {
        return sortByTitleStrat;
    }

    public TaskState getSelectedTaskState() {return selectedTaskState;}
}