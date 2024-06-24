package com.example.taskmanager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

public class GerenciadorDeTarefas extends JFrame {

    // Componentes principais da interface gráfica
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JComboBox<String> columnComboBox;
    private final JTextField filterTextField;

    // Construtor da classe TaskManager
    public GerenciadorDeTarefas() {
        setTitle("Gerenciador de Tarefas");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Inicializa o modelo da tabela e a tabela
        tableModel = new DefaultTableModel(new String[]{"PID", "Nome", "Usuário", "Caminho", "CPU (%)", "Memória (GB)"}, 0);
        table = new JTable(tableModel);

        // Cria um TableRowSorter para a tabela
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(table);

        // Botão para atualizar a lista de processos
        JButton refreshButton = new JButton("Atualizar");
        refreshButton.addActionListener(this::refreshProcesses);

        // Combobox para selecionar a coluna para ordenação
        columnComboBox = new JComboBox<>(new String[]{"PID", "Nome", "Usuário", "Caminho", "CPU (%)", "Memória (GB)"});
        columnComboBox.addActionListener(this::sortTableByColumn);

        // Botão para finalizar um processo selecionado
        JButton killButton = new JButton("Finalizar Processo");
        killButton.addActionListener(this::killProcess);

        // Campo de texto para filtrar processos pelo nome
        filterTextField = new JTextField(20);
        filterTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String text = filterTextField.getText();
                if (text.trim().length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 1));
                }
            }
        });

        // Painel para os componentes de controle
        JPanel panel = new JPanel();
        panel.add(new JLabel("Procurar por Nome:"));
        panel.add(filterTextField);
        panel.add(refreshButton);
        panel.add(new JLabel("Ordenar por:"));
        panel.add(columnComboBox);
        panel.add(killButton); // Adiciona o botão de finalizar processo

        // Adiciona o painel e a tabela à janela principal
        add(scrollPane, BorderLayout.CENTER);
        add(panel, BorderLayout.NORTH);

        // Define a visibilidade da janela após configurar os componentes e carregar os processos
        refreshProcesses(null);
        setVisible(true);
    }

    // Método para atualizar a lista de processos
    private void refreshProcesses(ActionEvent event) {
        SwingWorker<Object[][], Void> worker = new SwingWorker<>() {
            @Override
            protected Object[][] doInBackground() {
                List<ProcessHandle> processes = ProcessHandle.allProcesses().collect(Collectors.toList());
                Object[][] rowData = new Object[processes.size()][6]; // Preallocate array for rows

                // Parallellize the processing of process information
                processes.parallelStream().forEach(process -> {
                    int index = processes.indexOf(process); // Get the index for the current process
                    String pid = String.valueOf(process.pid());
                    String name = getProcessName(process);
                    String user = getUser(process);
                    String path = process.info().command().orElse("");
                    String cpuUsage = getProcessCpuUsageWindows(pid);
                    String memoryUsage = getProcessMemoryUsageWindows(pid);

                    // Debugging output
                    System.out.println("Processo PID: " + pid + ", Nome: " + name + ", Usuário: " + user + ", Caminho: " + path + ", CPU: " + cpuUsage + ", Memória: " + memoryUsage);

                    rowData[index] = new Object[]{pid, name, user, path, cpuUsage, memoryUsage};
                });

                return rowData;
            }

            @Override
            protected void done() {
                try {
                    Object[][] rowData = get();
                    tableModel.setDataVector(rowData, new String[]{"PID", "Nome", "Usuário", "Caminho", "CPU (%)", "Memória (GB)"});
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    // Método para obter o nome do processo no Windows
    private String getProcessName(ProcessHandle processHandle) {
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Windows")) {
            try {
                Process process = Runtime.getRuntime().exec("tasklist /FI \"PID eq " + processHandle.pid() + "\" /FO CSV");
                int bufferSize = 64 * 1024; // 64 KB buffer size
                char[] buffer = new char[bufferSize];
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()), bufferSize)) {
                    int charsRead;
                    StringBuilder output = new StringBuilder();
                    while ((charsRead = reader.read(buffer, 0, buffer.length)) != -1) {
                        output.append(buffer, 0, charsRead);
                    }
                    // Processar a saída completa
                    String[] lines = output.toString().split("\n");
                    for (String line : lines) {
                        if (line.contains(String.valueOf(processHandle.pid()))) {
                            String[] parts = line.split(",");
                            if (parts.length >= 2) {
                                return parts[0].replaceAll("\"", ""); // Get executable name without path
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // Default: returna processo ou string vazia
        return processHandle.info().command().orElse("");
    }

    // Método para obter o usuário do processo
    private String getUser(ProcessHandle processHandle) {
        return processHandle.info().user().orElse(System.getProperty("user.name"));
    }

    // Método para obter o uso de CPU do processo no Windows
    private String getProcessCpuUsageWindows(String pid) {
        String cpuUsage = "N/A";
        try {
                Process process = Runtime.getRuntime().exec("wmic path Win32_PerfFormattedData_PerfProc_Process where \"IDProcess='" + pid + "'\" get PercentProcessorTime /format:csv");
            int bufferSize = 64 * 1024; // 64 KB buffer size
            char[] buffer = new char[bufferSize];
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()), bufferSize)) {
                int charsRead;
                StringBuilder output = new StringBuilder();
                while ((charsRead = reader.read(buffer, 0, buffer.length)) != -1) {
                    output.append(buffer, 0, charsRead);
                }
                // Processar a saída completa
                String[] lines = output.toString().split("\n");
                for (String line : lines) {
                    if (!line.trim().isEmpty() && !line.contains("PercentProcessorTime")) {
                        String[] parts = line.split(",");
                        if (parts.length >= 2) {
                            cpuUsage = parts[1].trim();
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cpuUsage;
    }

    // Método para obter o uso de memória do processo no Windows
    private String getProcessMemoryUsageWindows(String pid) {
        String memoryUsage = "N/A";
        try {
            Process process = Runtime.getRuntime().exec("tasklist /FI \"PID eq " + pid + "\" /FO CSV /NH");
            int bufferSize = 64 * 1024; // 64 KB buffer size
            char[] buffer = new char[bufferSize];
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()), bufferSize)) {
                int charsRead;
                StringBuilder output = new StringBuilder();
                while ((charsRead = reader.read(buffer, 0, buffer.length)) != -1) {
                    output.append(buffer, 0, charsRead);
                }
                // Processar a saída completa
                String[] lines = output.toString().split("\n");
                for (String line : lines) {
                    if (line.contains(pid)) {
                        String[] parts = line.split(",");
                        if (parts.length >= 5) {
                            memoryUsage = parts[4].replaceAll("\"", "").replace(" K", "").trim();
                            // Remove commas and handle multiple points issue
                            memoryUsage = memoryUsage.replace(",", "").replaceAll("\\.", "");
                            try {
                                double memoryInKb = Double.parseDouble(memoryUsage);
                                memoryUsage = String.format("%.2f", memoryInKb / 1024.0 / 1024.0); // Convert KB to GB
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                                memoryUsage = "N/A";
                            }
                        }
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return memoryUsage;
    }

    // Método para ordenar a tabela pela coluna selecionada
    private void sortTableByColumn(ActionEvent event) {
        String columnName = (String) columnComboBox.getSelectedItem();
        if (columnName != null) {
            int columnIndex = getColumnIndex(columnName);
            if (columnIndex != -1) {
                sorter.toggleSortOrder(columnIndex);
            }
        }
    }

    // Método auxiliar para obter o índice da coluna pelo nome
    private int getColumnIndex(String columnName) {
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            if (tableModel.getColumnName(i).equals(columnName)) {
                return i;
            }
        }

        return -1;
    }

    private void killProcess(ActionEvent event) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            String pid = (String) tableModel.getValueAt(selectedRow, 0);
            try {
                Process process = Runtime.getRuntime().exec("taskkill /F /PID " + pid);
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    JOptionPane.showMessageDialog(this, "Processo " + pid + " finalizado com sucesso.");
                    refreshProcesses(null);
                } else {
                    JOptionPane.showMessageDialog(this, "Falha ao finalizar o processo " + pid + ".");
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erro ao finalizar o processo " + pid + ".");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um processo para finalizar.");
        }
    }

    public static void main(String[] args) {
        // Criar instância do gerenciador de tarefas no Event Dispatch Thread
        SwingUtilities.invokeLater(GerenciadorDeTarefas::new);
    }
}