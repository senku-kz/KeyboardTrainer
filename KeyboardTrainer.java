import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;
import javax.swing.text.*;

public class KeyboardTrainer extends JFrame {
    private JTextPane textToType;
    private JTextField inputField;
    private JLabel statsLabel;
    private JButton startButton;
    private JComboBox<String> languageSelector;
    private JSpinner letterCountSpinner;
    private JCheckBox includeUpperCase;
    private JCheckBox includeNumbers;
    private JCheckBox includeSpecialChars;
    
    private long startTime;
    private int correctChars;
    private int totalChars;
    private boolean gameActive;
    private String currentLanguage = "EN";
    
    private static final String LOWERCASE_RU = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя";
    private static final String LOWERCASE_EN = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMBERS = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$%^&*()_+-=,./<>?;':\"[]{}\\|";
    
    private static final Map<String, Map<String, String>> TRANSLATIONS = new HashMap<>();
    
    private static final Color ERROR_COLOR = new Color(255, 102, 102);
    private static final Color CORRECT_COLOR = new Color(102, 255, 102);
    private static final Color DEFAULT_COLOR = Color.BLACK;
    
    static {
        Map<String, String> englishUI = new HashMap<>();
        englishUI.put("title", "Keyboard Trainer");
        englishUI.put("start", "Start");
        englishUI.put("restart", "Restart");
        englishUI.put("speed", "Speed: %d chars/min | Accuracy: %d%%");
        englishUI.put("congrats", "Congratulations! You've completed the text!");
        englishUI.put("letterCount", "Length: ");
        englishUI.put("upperCase", "Upper case");
        englishUI.put("numbers", "Numbers");
        englishUI.put("specialChars", "Special chars");
        TRANSLATIONS.put("EN", englishUI);
        
        Map<String, String> russianUI = new HashMap<>();
        russianUI.put("title", "Клавиатурный тренажер");
        russianUI.put("start", "Начать");
        russianUI.put("restart", "Заново");
        russianUI.put("speed", "Скорость: %d зн/мин | Точность: %d%%");
        russianUI.put("congrats", "Поздравляем! Вы закончили текст!");
        russianUI.put("letterCount", "Длина: ");
        russianUI.put("upperCase", "Прописные буквы");
        russianUI.put("numbers", "Цифры");
        russianUI.put("specialChars", "Спец. символы");
        TRANSLATIONS.put("RU", russianUI);
    }

    public KeyboardTrainer() {
        setTitle(getTranslation("title"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // Initialize components
        textToType = new JTextPane();
        textToType.setFont(new Font("Arial", Font.PLAIN, 24));
        textToType.setEditable(false);
        textToType.setBackground(new Color(245, 245, 245));
        textToType.setMargin(new Insets(5, 5, 5, 5));
        
        inputField = new JTextField();
        inputField.setFont(new Font("Arial", Font.PLAIN, 24));
        inputField.setEnabled(false);
        
        statsLabel = new JLabel(String.format(getTranslation("speed"), 0, 0));
        startButton = new JButton(getTranslation("start"));
        
        // Language selector
        languageSelector = new JComboBox<>(new String[]{"EN", "RU"});
        
        // Generator controls
        letterCountSpinner = new JSpinner(new SpinnerNumberModel(30, 5, 100, 5));
        includeUpperCase = new JCheckBox(getTranslation("upperCase"));
        includeNumbers = new JCheckBox(getTranslation("numbers"));
        includeSpecialChars = new JCheckBox(getTranslation("specialChars"));
        
        // Layout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Text display panel
        JPanel textPanel = new JPanel(new BorderLayout(5, 5));
        textPanel.add(textToType, BorderLayout.NORTH);
        textPanel.add(inputField, BorderLayout.CENTER);
        mainPanel.add(textPanel, BorderLayout.NORTH);
        
        // Generator panel
        JPanel generatorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        generatorPanel.add(new JLabel(getTranslation("letterCount")));
        generatorPanel.add(letterCountSpinner);
        generatorPanel.add(includeUpperCase);
        generatorPanel.add(includeNumbers);
        generatorPanel.add(includeSpecialChars);
        mainPanel.add(generatorPanel, BorderLayout.CENTER);
        
        // Bottom panel with stats and controls
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(statsLabel, BorderLayout.WEST);
        
        JPanel rightPanel = new JPanel(new FlowLayout());
        rightPanel.add(languageSelector);
        rightPanel.add(startButton);
        bottomPanel.add(rightPanel, BorderLayout.EAST);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Add listeners
        startButton.addActionListener(e -> startGame());
        languageSelector.addActionListener(e -> switchLanguage());
        
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (gameActive) {
                    checkInput();
                }
            }
        });
        
        setSize(800, 250);
        setLocationRelativeTo(null);
    }
    
    private String generateRandomText() {
        StringBuilder availableChars = new StringBuilder();
        
        if (currentLanguage.equals("EN")) {
            availableChars.append(LOWERCASE_EN);
        } else {
            availableChars.append(LOWERCASE_RU);
        }
        
        if (includeUpperCase.isSelected()) {
            availableChars.append(availableChars.toString().toUpperCase());
        }
        if (includeNumbers.isSelected()) {
            availableChars.append(NUMBERS);
        }
        if (includeSpecialChars.isSelected()) {
            availableChars.append(SPECIAL_CHARS);
        }
        
        int length = (Integer) letterCountSpinner.getValue();
        StringBuilder randomText = new StringBuilder();
        Random random = new Random();
        
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(availableChars.length());
            randomText.append(availableChars.charAt(index));
            
            if ((i + 1) % 5 == 0 && i < length - 1) {
                randomText.append(" ");
            }
        }
        
        return randomText.toString();
    }
    
    private void startGame() {
        gameActive = true;
        correctChars = 0;
        totalChars = 0;
        startTime = System.currentTimeMillis();
        
        String newText = generateRandomText();
        StyledDocument doc = textToType.getStyledDocument();
        Style style = textToType.addStyle("ColorStyle", null);
        StyleConstants.setForeground(style, DEFAULT_COLOR);
        
        try {
            doc.remove(0, doc.getLength());
            doc.insertString(0, newText, style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        
        inputField.setText("");
        inputField.setEnabled(true);
        inputField.requestFocus();
        startButton.setText(getTranslation("restart"));
    }
    
    private void switchLanguage() {
        currentLanguage = (String) languageSelector.getSelectedItem();
        setTitle(getTranslation("title"));
        startButton.setText(gameActive ? getTranslation("restart") : getTranslation("start"));
        updateStats();
        
        if (gameActive) {
            startGame();
        }
    }
    
    private String getTranslation(String key) {
        return TRANSLATIONS.get(currentLanguage).get(key);
    }
    
    private void checkInput() {
        String targetText = textToType.getText();
        String currentInput = inputField.getText();
        
        totalChars = currentInput.length();
        correctChars = 0;
        
        // Создаем форматированный текст
        StyledDocument doc = textToType.getStyledDocument();
        Style style = textToType.addStyle("ColorStyle", null);
        
        try {
            // Сначала очищаем все форматирование
            doc.remove(0, doc.getLength());
            doc.insertString(0, targetText, style);
            
            // Применяем цвета для каждого символа
            for (int i = 0; i < targetText.length(); i++) {
                if (i < currentInput.length()) {
                    if (currentInput.charAt(i) == targetText.charAt(i)) {
                        StyleConstants.setForeground(style, CORRECT_COLOR);
                        correctChars++;
                    } else {
                        StyleConstants.setForeground(style, ERROR_COLOR);
                    }
                    doc.setCharacterAttributes(i, 1, style, true);
                } else {
                    StyleConstants.setForeground(style, DEFAULT_COLOR);
                    doc.setCharacterAttributes(i, 1, style, true);
                }
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        
        updateStats();
        
        if (currentInput.equals(targetText)) {
            // Сохраняем текущую статистику
            long currentTime = System.currentTimeMillis();
            double timeInMinutes = (currentTime - startTime) / 60000.0;
            int finalSpeed = timeInMinutes > 0 ? (int)(totalChars / timeInMinutes) : 0;
            int finalAccuracy = totalChars > 0 ? (correctChars * 100) / totalChars : 0;
            
            // Показываем небольшое уведомление с результатами
            String message = String.format("%s\n%s: %d\n%s: %d%%", 
                getTranslation("congrats"),
                getTranslation("speed").split("\\|")[0].trim(), finalSpeed,
                getTranslation("speed").split("\\|")[1].trim(), finalAccuracy
            );
            
            // Создаем и показываем уведомление в отдельном потоке
            SwingUtilities.invokeLater(() -> {
                JOptionPane optionPane = new JOptionPane(
                    message,
                    JOptionPane.INFORMATION_MESSAGE
                );
                JDialog dialog = optionPane.createDialog(this, getTranslation("title"));
                
                // Автоматически закрываем диалог через 2 секунды
                new Timer(2000, e -> dialog.dispose()).start();
                dialog.setVisible(true);
            });
            
            // Генерируем новый текст и очищаем поле ввода
            textToType.setText(generateRandomText());
            inputField.setText("");
            
            // Обновляем время начала для новой сессии
            startTime = System.currentTimeMillis();
            correctChars = 0;
            totalChars = 0;
        }
    }
    
    private void updateStats() {
        long currentTime = System.currentTimeMillis();
        double timeInMinutes = (currentTime - startTime) / 60000.0;
        int speed = timeInMinutes > 0 ? (int)(totalChars / timeInMinutes) : 0;
        int accuracy = totalChars > 0 ? (correctChars * 100) / totalChars : 0;
        
        statsLabel.setText(String.format(getTranslation("speed"), speed, accuracy));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new KeyboardTrainer().setVisible(true);
        });
    }
}
