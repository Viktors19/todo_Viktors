package lv.sda.todo.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static javax.swing.JOptionPane.showMessageDialog;

public class TodoUI {

    final int frameWidth = 500;

    public void generate() {

        JFrame frame = new JFrame();
        frame.setBounds(10,10,frameWidth,750);

        JPanel headPanel = new JPanel();
        headPanel.setPreferredSize(new Dimension(frameWidth,50));

        final JTextField newTaskNameTextField = new JTextField(30);
        headPanel.add(newTaskNameTextField);

        JPanel taskPanel = new JPanel();
        taskPanel.setPreferredSize(new Dimension(frameWidth,500));

        HashMap<JCheckBoxMenuItem, JPanel> checkBoxes = new HashMap<JCheckBoxMenuItem, JPanel>();
        List<String> archivedTasks = new ArrayList<String>();;


        JButton addTaskButton = new JButton("Add");
        addTaskButton.addActionListener(i->{
            createTask(taskPanel, checkBoxes, newTaskNameTextField.getText());
            newTaskNameTextField.setText(null);
        });
        headPanel.add(addTaskButton);


        Path path = Paths.get("todoTasks.txt");
        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(s -> createTask(taskPanel, checkBoxes, s));
        } catch (IOException ex) {
        }

        path = Paths.get("archivedTasks.txt");
        try (Stream<String> lines = Files.lines(path)) {
            archivedTasks = lines.collect(Collectors.toList());
        } catch (IOException ex) {
        }

        JButton deleteTasksButton = new JButton("Delete selected tasks");
        deleteTasksButton.addActionListener(i->{
            checkBoxes.forEach((k, v) -> {
                if (k.getState()){
                    v.setVisible(false);
                    taskPanel.remove(v);
                }
            });
            checkBoxes.keySet().removeIf(e -> (e.getState()));
        });


        JButton archiveTasksButton = new JButton("Archive selected tasks");
        List<String> finalArchivedTasks = archivedTasks;
        archiveTasksButton.addActionListener(i->{
            checkBoxes.forEach((k, v) -> {
                if (k.getState()){
                    String archiveTaskString = k.getText().replace("<html><strike>", "");
                    archiveTaskString = archiveTaskString.replace("</strike></html>", "");
                    finalArchivedTasks.add(archiveTaskString);
                    v.setVisible(false);
                    taskPanel.remove(v);
                }
            });
            checkBoxes.keySet().removeIf(e -> (e.getState()));
        });

        JButton showArchivedTasksButton = new JButton("Show archived tasks");
        showArchivedTasksButton.addActionListener(i->{
            showMessageDialog(null, "Archived tasks:\n"+finalArchivedTasks.toString());
        });

        frame.setLayout(new FlowLayout());
        frame.add(headPanel);
        frame.add(taskPanel);
        frame.add(deleteTasksButton);
        frame.add(archiveTasksButton);
        frame.add(showArchivedTasksButton);
        frame.setVisible(true);

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {

                try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream("todoTasks.txt"), "utf-8"))) {
                    checkBoxes.forEach((k, v) -> {
                        try {
                            writer.write(k.getText()+"\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream("archivedTasks.txt"), "utf-8"))) {
                    for(String str: finalArchivedTasks) {
                        writer.write(str+"\n");
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                System.exit(0);
            }
        });
    }

    private void createTask(JPanel taskPanel, HashMap<JCheckBoxMenuItem, JPanel> checkBoxes, String taskName) {
        JCheckBoxMenuItem cb = new JCheckBoxMenuItem(taskName);
        if (taskName.contains("<strike>")) {cb.setState(true);}
        cb.addActionListener(j->{
            if (cb.getState()) {
                cb.setText("<html><strike>"+cb.getText()+"</strike></html>");
            }else{
                cb.setText(cb.getText().replace("<html><strike>", ""));
                cb.setText(cb.getText().replace("</strike></html>", ""));
            }
        });
        JPanel separateTaskPanel = new JPanel();
        separateTaskPanel.setPreferredSize(new Dimension(frameWidth-11,22));

        JLabel deleteTaskLabel = new JLabel("<html><a href>[X]</a></html>");
        deleteTaskLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                separateTaskPanel.setVisible(false);
                taskPanel.remove(separateTaskPanel);
                checkBoxes.remove(cb);
            }
        });

        separateTaskPanel.add(cb);
        separateTaskPanel.add(deleteTaskLabel);
        checkBoxes.put(cb, separateTaskPanel);
        taskPanel.add(separateTaskPanel);
        taskPanel.validate();
    }
}