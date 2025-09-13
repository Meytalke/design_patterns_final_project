package view;

import model.task.ITask;
import model.task.TaskState;
import viewmodel.TasksViewModel;
import viewmodel.IViewModel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.List;

public class TaskManagerView extends JPanel implements TasksObserver, IView {

    private JFrame frame;
    private JPanel contentPane;
    private JTextField taskInput;
    private JTextArea descriptionInput;
    private JButton addButton, updateButton, deleteButton, deleteAllButton, reportButton;
    private JComboBox<String> stateFilterComboBox;
    private JComboBox<String> exportFormatComboBox;

    private JComboBox<TaskState> taskStateComboBox;
    private ITask selectedTask = null;

    private JList<ITask> taskList;
    private DefaultListModel<ITask> listModel;

    private JTextField searchTitleInput;
    private JTextField searchDescriptionInput;
    private JTextField searchIdInput;

    //Interface over class
    private IViewModel viewModel;

    public TaskManagerView(IViewModel viewModel) {
        
        //Set viewmodel
        setViewModel(viewModel);

        //Create UI base components
        contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


        taskInput = new JTextField(20);
        descriptionInput = new JTextArea(3, 20);

        searchTitleInput = new JTextField(15);
        searchDescriptionInput = new JTextField(15);
        searchIdInput = new JTextField(5);

        addButton = new JButton("Add Task");
        updateButton = new JButton("Update Task");
        deleteButton = new JButton("Delete Selected");
        deleteAllButton = new JButton("Delete All");
        reportButton = new JButton("Generate Report");

        stateFilterComboBox = new JComboBox<>(new String[]{"All", "To Do", "In Progress", "Completed"});
        taskStateComboBox = new JComboBox<>(TaskState.values());
        exportFormatComboBox = new JComboBox<>(new String[]{"Terminal","PDF", "CSV" , "JSON"});

        listModel = new DefaultListModel<>();
        taskList = new JList<>(listModel);
        taskList.setCellRenderer(new TaskCellRenderer());

        frame = new JFrame("Tasks Manager");
    }

    @Override
    public void start() {
        JPanel addTaskPanel = new JPanel(new GridBagLayout());
        addTaskPanel.setBorder(BorderFactory.createTitledBorder("Add/Update Task"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Row 1: Task Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        addTaskPanel.add(new JLabel("Task Title:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        addTaskPanel.add(taskInput, gbc);

        // Row 2: Task Description
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        addTaskPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        addTaskPanel.add(new JScrollPane(descriptionInput), gbc);

        // Row 3: Task State
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        addTaskPanel.add(new JLabel("Task State:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        addTaskPanel.add(taskStateComboBox, gbc);

        // Row 4: Action Buttons
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.EAST;
        JPanel actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        actionButtonsPanel.add(addButton);
        actionButtonsPanel.add(updateButton);
        addTaskPanel.add(actionButtonsPanel, gbc);

        // Set initial state of buttons
        updateButton.setEnabled(false);
        taskStateComboBox.setEnabled(false);

        // Panel for search and filter controls
        JPanel searchAndFilterPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        searchAndFilterPanel.setBorder(BorderFactory.createTitledBorder("Search and Filter"));
        searchAndFilterPanel.add(new JLabel("Search Title:"));
        searchAndFilterPanel.add(searchTitleInput);
        searchAndFilterPanel.add(new JLabel("Search Description:"));
        searchAndFilterPanel.add(searchDescriptionInput);
        searchAndFilterPanel.add(new JLabel("Search ID:"));
        searchAndFilterPanel.add(searchIdInput);
        searchAndFilterPanel.add(new JLabel("Filter by State:"));
        searchAndFilterPanel.add(stateFilterComboBox);

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

        frame.setContentPane(contentPane);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // --- Event Listeners ---
        addButton.addActionListener(e -> {
            String title = taskInput.getText();
            String description = descriptionInput.getText();
            if (!title.isEmpty()) {
                viewModel.addTask(title, description);
                resetForm();
            }
        });

        // Update task...
        updateButton.addActionListener(e -> {
            if (selectedTask != null) {
                String title = taskInput.getText();
                String description = descriptionInput.getText();
                TaskState state = (TaskState) taskStateComboBox.getSelectedItem();
                viewModel.updateTask(selectedTask.getId(), title, description, state);
                resetForm();
            }
        });
        // Delete task...
        deleteButton.addActionListener(e -> {
            ITask taskToDelete = taskList.getSelectedValue();
            if (taskToDelete != null) {
                viewModel.deleteTask(taskToDelete.getId());
                resetForm();
            } else {
                JOptionPane.showMessageDialog(frame, "Please select a task to delete.", "No Task Selected", JOptionPane.WARNING_MESSAGE);
            }
        });
        // Delete all tasks...
        deleteAllButton.addActionListener(e -> {
            int response = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete all tasks?", "Confirm Delete All", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                viewModel.deleteAllTasks();
                resetForm();
            }
        });
        // Generate report...
        reportButton.addActionListener(e -> {
            String selectedFormat = (String) exportFormatComboBox.getSelectedItem();
            viewModel.generateReport(selectedFormat);
        });
        //List selection listener...
        taskList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    selectedTask = taskList.getSelectedValue();
                    if (selectedTask != null) {
                        taskInput.setText(selectedTask.getTitle());
                        descriptionInput.setText(selectedTask.getDescription());
                        taskStateComboBox.setSelectedItem(selectedTask.getState());
                        taskStateComboBox.setEnabled(true);
                        addButton.setEnabled(false);
                        updateButton.setEnabled(true);
                    } else {
                        resetForm();
                    }
                }
            }
        });

        // Filter controls
        stateFilterComboBox.addActionListener(e -> applyAllFilters());
        searchTitleInput.addActionListener(e -> applyAllFilters());
        searchDescriptionInput.addActionListener(e -> applyAllFilters());
        searchIdInput.addActionListener(e -> applyAllFilters());

        viewModel.loadTasks();
    }

/**
 * Applies the current filter settings to the ViewModel, causing the visible task list to
 * be refreshed accordingly.
 * <p>
 * This method is called in response to user actions on the filter controls.
 *
 * @param selectedState   the desired state name or "All"
 * @param titleTerm        a search term for titles; may be null/empty
 * @param descriptionTerm  a search term for descriptions; may be null/empty
 * @param idTerm           an integer id as string; may be null/empty
 */
    private void applyAllFilters() {
        String selectedState = (String) stateFilterComboBox.getSelectedItem();
        String titleTerm = searchTitleInput.getText();
        String descriptionTerm = searchDescriptionInput.getText();
        String idTerm = searchIdInput.getText();
        ((TasksViewModel) getViewModel()).filterTasks(selectedState, titleTerm, descriptionTerm, idTerm);
    }

  

/**
 * Resets the form to its initial state: clears input fields, selects the first
 * option in the state and priority combo boxes, disables the state combo box,
 * and enables/disables the control buttons as appropriate. Also clears the
 * selection in the task list.
 */
    private void resetForm() {
        taskInput.setText("");
        descriptionInput.setText("");
        taskStateComboBox.setSelectedIndex(0);
        taskStateComboBox.setEnabled(false);
        addButton.setEnabled(true);
        updateButton.setEnabled(false);
        selectedTask = null;
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

        /**
         * Sets the ViewModel for this View.
         * <p>
         * After setting the ViewModel, the View registers itself as an observer of the
         * ViewModel to receive updates when the ViewModel's state changes.
         *
         * @param viewModel the ViewModel to set; must not be null
         */
    @Override
    public void setViewModel(IViewModel viewModel) {
        this.viewModel = viewModel;
        viewModel.addObserver(this);
    }


/**
 * Returns the currently associated ViewModel.
 * <p>
 * Implementations may return {@code null} if a ViewModel has not yet been assigned.
 *
 * @return the current {@link IViewModel}, or {@code null} if none is set
 */
    @Override
    public IViewModel getViewModel() {
        return viewModel;
    }
}