# Gerenciador de Tarefas

## Descrição

O Gerenciador de Tarefas é uma aplicação Java baseada em Swing que permite visualizar, filtrar, ordenar e finalizar processos em execução no sistema. Ele exibe informações detalhadas sobre cada processo, como PID, Nome, Usuário, Caminho, CPU (%) e Memória (GB). A aplicação permite que o usuário atualize a lista de processos, ordene por diferentes colunas e filtre processos pelo nome.

## Funcionalidades

- **Atualização da Lista de Processos**: Permite atualizar a lista de processos em execução no sistema.
- **Ordenação**: Permite ordenar os processos por diferentes colunas (PID, Nome, Usuário, Caminho, CPU (%), Memória (GB)).
- **Filtragem**: Permite filtrar os processos pelo nome usando um campo de texto.
- **Finalizar Processos**: Permite finalizar um processo selecionado na tabela.

## Dependências

- Java 8 ou superior.

## Estrutura do Código

### Classe `GerenciadorDeTarefas`

Esta é a classe principal que define a interface gráfica e a lógica do aplicativo.

#### Componentes Principais

- `DefaultTableModel tableModel`: Modelo de dados para a tabela de processos.
- `JTable table`: Tabela que exibe os processos.
- `TableRowSorter<DefaultTableModel> sorter`: Utilizado para ordenar e filtrar a tabela.
- `JComboBox<String> columnComboBox`: Combobox para selecionar a coluna de ordenação.
- `JTextField filterTextField`: Campo de texto para filtrar processos pelo nome.

#### Construtor

O construtor inicializa a interface gráfica, define os componentes e configura os ouvintes de eventos.

#### Métodos

- `refreshProcesses(ActionEvent event)`: Atualiza a lista de processos.
- `getProcessName(ProcessHandle processHandle)`: Obtém o nome do processo no Windows.
- `getUser(ProcessHandle processHandle)`: Obtém o usuário do processo.
- `getProcessCpuUsageWindows(String pid)`: Obtém o uso de CPU do processo no Windows.
- `getProcessMemoryUsageWindows(String pid)`: Obtém o uso de memória do processo no Windows.
- `sortTableByColumn(ActionEvent event)`: Ordena a tabela pela coluna selecionada.
- `getColumnIndex(String columnName)`: Retorna o índice da coluna pelo nome.
- `killProcess(ActionEvent event)`: Finaliza o processo selecionado.

## Como Executar

1. Certifique-se de ter o Java instalado na sua máquina.
2. Compile o código fonte:
    ```bash
    javac -d bin src/com/example/taskmanager/GerenciadorDeTarefas.java
    ```
3. Execute a aplicação:
    ```bash
    java -cp bin com.example.taskmanager.GerenciadorDeTarefas
    ```

## Interface Gráfica

A interface gráfica consiste em uma tabela que exibe os processos, um campo de texto para filtrar os processos pelo nome, um botão para atualizar a lista de processos, um combobox para selecionar a coluna de ordenação e um botão para finalizar o processo selecionado.

## Funcionalidades Detalhadas

### Atualização da Lista de Processos

Quando o botão "Atualizar" é clicado, a aplicação coleta informações sobre todos os processos em execução no sistema e atualiza a tabela.

### Ordenação

O usuário pode selecionar uma coluna para ordenar os processos. A ordenação é realizada usando o `TableRowSorter`.

### Filtragem

O usuário pode filtrar os processos pelo nome digitando no campo de texto. A filtragem é realizada em tempo real conforme o usuário digita.

### Finalizar Processos

O usuário pode selecionar um processo na tabela e clicar no botão "Finalizar Processo" para encerrar o processo selecionado. A aplicação executa o comando `taskkill` para finalizar o processo.

## Exceções e Tratamento de Erros

A aplicação trata várias exceções, incluindo `IOException` e `InterruptedException`, exibindo mensagens de erro apropriadas ao usuário em caso de falha ao coletar informações sobre os processos ou ao finalizar um processo.

## Observações

- A aplicação foi projetada para ser executada em sistemas Windows, pois utiliza comandos específicos do Windows para coletar informações sobre os processos (`tasklist`, `wmic`) e para finalizar processos (`taskkill`).
- Para sistemas diferentes do Windows, os métodos de coleta de informações sobre os processos e finalização de processos precisarão ser adaptados.
