import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.swing.*;

public class SudokuGame extends JFrame {
    private static final int SIZE = 9;
    private static final int SUBGRID_SIZE = 3;
    
    private int[][] solution;
    private int[][] puzzle;
    private JTextField[][] cells;
    private JButton newGameButton, solveButton, checkButton;
    private JLabel statusLabel;
    
    public SudokuGame() {
        initializeGUI();
        generateNewGame();
    }
    
    private void initializeGUI() {
        setTitle("Sudoku - Jogo de Lógica");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Painel principal do jogo
        JPanel gamePanel = new JPanel(new GridLayout(SIZE, SIZE, 1, 1));
        gamePanel.setBackground(Color.BLACK);
        gamePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        cells = new JTextField[SIZE][SIZE];
        
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                cells[row][col] = new JTextField();
                cells[row][col].setHorizontalAlignment(JTextField.CENTER);
                cells[row][col].setFont(new Font("Arial", Font.BOLD, 18));
                
                // Colorir as subgrades alternadamente
                if (((row / SUBGRID_SIZE) + (col / SUBGRID_SIZE)) % 2 == 0) {
                    cells[row][col].setBackground(new Color(240, 240, 240));
                } else {
                    cells[row][col].setBackground(Color.WHITE);
                }
                
                // Adicionar listener para validação de entrada
                final int r = row, c = col;
                cells[row][col].addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyTyped(KeyEvent e) {
                        char ch = e.getKeyChar();
                        if (ch < '1' || ch > '9') {
                            if (ch != KeyEvent.VK_BACK_SPACE && ch != KeyEvent.VK_DELETE) {
                                e.consume();
                            }
                        }
                    }
                    
                    @Override
                    public void keyReleased(KeyEvent e) {
                        validateMove(r, c);
                    }
                });
                
                gamePanel.add(cells[row][col]);
            }
        }
        
        // Painel de controles
        JPanel controlPanel = new JPanel(new FlowLayout());
        
        newGameButton = new JButton("Novo Jogo");
        newGameButton.addActionListener(e -> generateNewGame());
        
        solveButton = new JButton("Resolver");
        solveButton.addActionListener(e -> solvePuzzle());
        
        checkButton = new JButton("Verificar");
        checkButton.addActionListener(e -> checkSolution());
        
        controlPanel.add(newGameButton);
        controlPanel.add(solveButton);
        controlPanel.add(checkButton);
        
        // Label de status
        statusLabel = new JLabel("Novo jogo carregado! Boa sorte!");
        statusLabel.setHorizontalAlignment(JLabel.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        add(gamePanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
        add(statusLabel, BorderLayout.NORTH);
        
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }
    
    private void generateNewGame() {
        solution = new int[SIZE][SIZE];
        puzzle = new int[SIZE][SIZE];
        
        // Gerar uma solução válida
        fillGrid(solution);
        
        // Criar puzzle removendo números
        for (int i = 0; i < SIZE; i++) {
            System.arraycopy(solution[i], 0, puzzle[i], 0, SIZE);
        }
        
        removeCells(puzzle, 40); // Remove 40 células para criar o puzzle
        
        updateDisplay();
        statusLabel.setText("Novo jogo carregado! Boa sorte!");
    }
    
    private boolean fillGrid(int[][] grid) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (grid[row][col] == 0) {
                    List<Integer> numbers = new ArrayList<>();
                    for (int i = 1; i <= 9; i++) {
                        numbers.add(i);
                    }
                    Collections.shuffle(numbers);
                    
                    for (int num : numbers) {
                        if (isValidMove(grid, row, col, num)) {
                            grid[row][col] = num;
                            if (fillGrid(grid)) {
                                return true;
                            }
                            grid[row][col] = 0;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }
    
    private void removeCells(int[][] grid, int cellsToRemove) {
        Random random = new Random();
        int removed = 0;
        
        while (removed < cellsToRemove) {
            int row = random.nextInt(SIZE);
            int col = random.nextInt(SIZE);
            
            if (grid[row][col] != 0) {
                int backup = grid[row][col];
                grid[row][col] = 0;
                
                // Verificar se ainda há uma solução única
                int[][] testGrid = new int[SIZE][SIZE];
                for (int i = 0; i < SIZE; i++) {
                    System.arraycopy(grid[i], 0, testGrid[i], 0, SIZE);
                }
                
                if (hasUniqueSolution(testGrid)) {
                    removed++;
                } else {
                    grid[row][col] = backup; // Restaurar se não há solução única
                }
            }
        }
    }
    
    private boolean hasUniqueSolution(int[][] grid) {
        int[][] testGrid = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            System.arraycopy(grid[i], 0, testGrid[i], 0, SIZE);
        }
        return solveSudoku(testGrid);
    }
    
    private boolean isValidMove(int[][] grid, int row, int col, int num) {
        // Verificar linha
        for (int c = 0; c < SIZE; c++) {
            if (grid[row][c] == num) return false;
        }
        
        // Verificar coluna
        for (int r = 0; r < SIZE; r++) {
            if (grid[r][col] == num) return false;
        }
        
        // Verificar subgrade 3x3
        int startRow = row - row % SUBGRID_SIZE;
        int startCol = col - col % SUBGRID_SIZE;
        
        for (int r = startRow; r < startRow + SUBGRID_SIZE; r++) {
            for (int c = startCol; c < startCol + SUBGRID_SIZE; c++) {
                if (grid[r][c] == num) return false;
            }
        }
        
        return true;
    }
    
    private boolean solveSudoku(int[][] grid) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (grid[row][col] == 0) {
                    for (int num = 1; num <= 9; num++) {
                        if (isValidMove(grid, row, col, num)) {
                            grid[row][col] = num;
                            if (solveSudoku(grid)) {
                                return true;
                            }
                            grid[row][col] = 0;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }
    
    private void updateDisplay() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (puzzle[row][col] != 0) {
                    cells[row][col].setText(String.valueOf(puzzle[row][col]));
                    cells[row][col].setEditable(false);
                    cells[row][col].setFont(new Font("Arial", Font.BOLD, 18));
                    cells[row][col].setForeground(Color.BLACK);
                } else {
                    cells[row][col].setText("");
                    cells[row][col].setEditable(true);
                    cells[row][col].setFont(new Font("Arial", Font.PLAIN, 18));
                    cells[row][col].setForeground(Color.BLUE);
                }
            }
        }
    }
    
    private void validateMove(int row, int col) {
        String text = cells[row][col].getText();
        if (!text.isEmpty()) {
            try {
                int num = Integer.parseInt(text);
                if (num >= 1 && num <= 9) {
                    int[][] currentGrid = getCurrentGrid();
                    if (isValidMove(currentGrid, row, col, num)) {
                        cells[row][col].setBackground(
                            ((row / SUBGRID_SIZE) + (col / SUBGRID_SIZE)) % 2 == 0 ?
                            new Color(240, 240, 240) : Color.WHITE
                        );
                    } else {
                        cells[row][col].setBackground(new Color(255, 200, 200)); // Vermelho claro
                    }
                }
            } catch (NumberFormatException e) {
                cells[row][col].setText("");
            }
        }
    }
    
    private int[][] getCurrentGrid() {
        int[][] current = new int[SIZE][SIZE];
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                String text = cells[row][col].getText();
                if (!text.isEmpty()) {
                    try {
                        current[row][col] = Integer.parseInt(text);
                    } catch (NumberFormatException e) {
                        current[row][col] = 0;
                    }
                } else {
                    current[row][col] = 0;
                }
            }
        }
        return current;
    }
    
    private void solvePuzzle() {
        int[][] currentGrid = getCurrentGrid();
        if (solveSudoku(currentGrid)) {
            for (int row = 0; row < SIZE; row++) {
                for (int col = 0; col < SIZE; col++) {
                    cells[row][col].setText(String.valueOf(currentGrid[row][col]));
                    if (puzzle[row][col] == 0) {
                        cells[row][col].setForeground(Color.RED);
                    }
                }
            }
            statusLabel.setText("Puzzle resolvido automaticamente!");
        } else {
            statusLabel.setText("Não foi possível resolver o puzzle!");
        }
    }
    
    private void checkSolution() {
        int[][] current = getCurrentGrid();
        boolean isComplete = true;
        boolean isValid = true;
        
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (current[row][col] == 0) {
                    isComplete = false;
                } else if (!isValidMove(copyGridExcept(current, row, col), row, col, current[row][col])) {
                    isValid = false;
                }
            }
        }
        
        if (isComplete && isValid) {
            statusLabel.setText("Parabéns! Você resolveu o Sudoku!");
            JOptionPane.showMessageDialog(this, "Parabéns! Você completou o Sudoku com sucesso!", 
                                        "Vitória!", JOptionPane.INFORMATION_MESSAGE);
        } else if (!isValid) {
            statusLabel.setText("Há erros no seu Sudoku. Verifique as células destacadas.");
        } else {
            statusLabel.setText("Sudoku incompleto, mas sem erros até agora.");
        }
    }
    
    private int[][] copyGridExcept(int[][] grid, int excludeRow, int excludeCol) {
        int[][] copy = new int[SIZE][SIZE];
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (row == excludeRow && col == excludeCol) {
                    copy[row][col] = 0;
                } else {
                    copy[row][col] = grid[row][col];
                }
            }
        }
        return copy;
    }
    
public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
        new SudokuGame().setVisible(true);
    });
}
}