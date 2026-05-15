import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class GameEngine extends JFrame {
    private List<Player> players = new ArrayList<>();
    private List<Card> communityCards = new ArrayList<>();
    private List<Card> deck = new ArrayList<>();
    private PokerTablePanel tablePanel = new PokerTablePanel();
    
    private int pot = 0;
    private int currentBet = 0; 
    private int humanBetThisPhase = 0;
    private int currentPhase = 0; 

    private JButton btnCall;
    private JButton btnRaise;
    private JButton btnFold;

    public GameEngine() {
        setupGame();
        initUI();
        startNewRound();
    }

    private void setupGame() {
        players.add(new Player("You", 1000, false));
        players.add(new Player("Bot 1", 1000, true));
        players.add(new Player("Bot 2", 1000, true));
        players.add(new Player("Bot 3", 1000, true));
        
        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Rank rank : Card.Rank.values()) {
                String suitLetter = suit.name().substring(0, 1).toLowerCase();
                String fileName = suitLetter + rank.getLetter() + ".png";
                deck.add(new Card(rank, suit, "assets/" + fileName));
            }
        }
    }

    private void initUI() {
        setTitle("Poker");
        setSize(850, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(tablePanel, BorderLayout.CENTER);

        JPanel controls = new JPanel();
        btnCall = new JButton("Check");
        btnRaise = new JButton("Raise");
        btnFold = new JButton("Fold");

        JButton btnTutorial = new JButton("Tutorial");

        btnTutorial.addActionListener(e -> {
            try {
                ImageIcon icon = new ImageIcon("assets/tutorial.png");
                Image scaled = icon.getImage().getScaledInstance(400, 300, Image.SCALE_SMOOTH);
                JOptionPane.showMessageDialog(this, "", "Tutorial", JOptionPane.PLAIN_MESSAGE, new ImageIcon(scaled));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Tutorial image not found! Make sure 'tutorial.png' is in the assets folder.");
            }
        });

        btnCall.addActionListener(e -> {
            SoundManager.play("check.wav");
            int toCall = currentBet - humanBetThisPhase;
            int actualBet = players.get(0).bet(toCall);
            pot += actualBet;
            humanBetThisPhase += actualBet;
            players.get(0).setActionText(toCall == 0 ? "Check" : "Call");
            startAITurns(); 
        });

        btnRaise.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, 
                "Current bet is $" + currentBet + ". Enter your raise:", 
                "Raise", JOptionPane.QUESTION_MESSAGE);
            
            if (input != null && !input.trim().isEmpty()) {
                try {
                    SoundManager.play("raise.wav");
                    int raiseAmount = Integer.parseInt(input.trim());
                    if (raiseAmount <= currentBet) {
                        JOptionPane.showMessageDialog(this, "Must raise MORE than $" + currentBet);
                        return;
                    }
                    currentBet = raiseAmount;
                    int toCall = currentBet - humanBetThisPhase;
                    int actualBet = players.get(0).bet(toCall);
                    pot += actualBet;
                    humanBetThisPhase += actualBet;
                    players.get(0).setActionText("Raise $" + currentBet);
                    startAITurns(); 
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Digits only please!");
                }
            }
        });

        btnFold.addActionListener(e -> {
            SoundManager.play("fold.wav");
            players.get(0).fold();
            players.get(0).setActionText("Fold");
            
            if (checkEarlyWin()) return;
            
            while (currentPhase < 4) {
                runAIInstantly();
                
                if (checkEarlyWin()) return; 
                
                currentBet = 0;
                currentPhase++;
                if (currentPhase == 1) for(int i=0; i<3; i++) communityCards.get(i).setFaceUp(true);
                else if (currentPhase == 2) communityCards.get(3).setFaceUp(true);
                else if (currentPhase == 3) communityCards.get(4).setFaceUp(true);
            }
            resolveWinner();
            startNewRound();
        });

        controls.add(btnTutorial); controls.add(btnCall); controls.add(btnRaise); controls.add(btnFold);
        add(controls, BorderLayout.SOUTH);
    }

private void startAITurns() {
        btnCall.setEnabled(false);
        btnRaise.setEnabled(false);
        btnFold.setEnabled(false);

        new Thread(() -> {
            try {
                Random r = new Random();
                for (Player p : players) {
                    if (p.isAI() && !p.isFolded()) {
                        
                        Thread.sleep(1000); 
                        
                        if (currentBet > 0) {
                            if (r.nextInt(10) < 4) {
                                p.fold();
                                p.setActionText("Fold");
                                SoundManager.play("fold.wav");
                            } else {    
                                pot += p.bet(currentBet);
                                p.setActionText("Call");
                                SoundManager.play("check.wav");
                            }
                        } else {
                            if (r.nextInt(10) < 7) {
                                pot += p.bet(0);
                                p.setActionText("Check");
                                SoundManager.play("check.wav");
                            } else {
                                currentBet = 50;
                                pot += p.bet(currentBet);
                                p.setActionText("Raise $50");
                                SoundManager.play("raise.wav");
                            }
                        }
                        
                        tablePanel.repaint();
                    }
                }

                Thread.sleep(1000); 

                SwingUtilities.invokeLater(() -> {
                    for (Player p : players) p.setActionText(""); 
                    
                    if (checkEarlyWin()) return;

                    if (!players.get(0).isFolded() && currentBet > humanBetThisPhase) {
                        int amountToCall = currentBet - humanBetThisPhase;
                        
                        Object[] options = {"Call $" + amountToCall, "Fold"};
                        int choice = JOptionPane.showOptionDialog(GameEngine.this,
                            "An AI raised! You need to put in $" + amountToCall + " more to stay in.",
                            "AI Raise", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
                            null, options, options[0]);

                        if (choice == 0) { 
                            int actualBet = players.get(0).bet(amountToCall);
                            pot += actualBet;
                            humanBetThisPhase += actualBet;
                            SoundManager.play("check.wav");
                        } else { 
                            players.get(0).fold();
                            SoundManager.play("fold.wav");
                            if (checkEarlyWin()) return;
                        }
                    }

                    nextPhase();
                    
                    btnCall.setEnabled(true);
                    btnRaise.setEnabled(true);
                    btnFold.setEnabled(true);
                });

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private void runAIInstantly() {
        Random r = new Random();
        for (Player p : players) {
            if (p.isAI() && !p.isFolded()) {
                if (currentBet > 0) {
                    if (r.nextInt(10) < 4) p.fold();
                    else pot += p.bet(currentBet);
                } else {
                    if (r.nextInt(10) < 7) pot += p.bet(0);
                    else {
                        currentBet = 50;
                        pot += p.bet(currentBet);
                    }
                }
            }
        }
    }

    private void nextPhase() {
        currentBet = 0;
        humanBetThisPhase = 0;
        currentPhase++;
        
        if (currentPhase == 1) { 
            for(int i=0; i<3; i++) {
                communityCards.get(i).setFaceUp(true);
                SoundManager.play("draw.wav");
            };
        } else if (currentPhase == 2) { 
            SoundManager.play("draw.wav");
            communityCards.get(3).setFaceUp(true);
        } else if (currentPhase == 3) { 
            SoundManager.play("draw.wav");
            communityCards.get(4).setFaceUp(true);
        } else if (currentPhase == 4) {
            resolveWinner();
            startNewRound();
            return; 
        }

        String myHandStrength = HandEvaluator.evaluate(players.get(0).getHand(), communityCards).description;
        tablePanel.updateData(players, communityCards, pot, myHandStrength);
    }

    private void startNewRound() {
        currentPhase = 0;
        pot = 0;
        currentBet = 0;
        humanBetThisPhase = 0;
        communityCards.clear();

        for (Player p : players) p.clearHand();
        Collections.shuffle(deck);

        int deckIndex = 0;
        for (int i = 0; i < 2; i++) {
            for (Player p : players) {
                Card c = deck.get(deckIndex++);
                if (!p.isAI()) c.setFaceUp(true); 
                else c.setFaceUp(false);
                p.addCard(c);
            }
        }

        for (int i = 0; i < 5; i++) {
            Card c = deck.get(deckIndex++);
            c.setFaceUp(false);
            communityCards.add(c);
        }

        String startingHandStrength = HandEvaluator.evaluate(players.get(0).getHand(), communityCards).description;
        tablePanel.updateData(players, communityCards, pot, startingHandStrength);
    }

    private void resolveWinner() {
        Player winner = null;
        int highestScore = -1;
        String winningHandDesc = "";

        for (Player p : players) {
            if (p.isFolded()) continue; 
            HandEvaluator.HandResult result = HandEvaluator.evaluate(p.getHand(), communityCards);
            if (result.score > highestScore) {
                highestScore = result.score;
                winner = p;
                winningHandDesc = result.description;
            }
            for (Card c : p.getHand()) c.setFaceUp(true);
        }

        tablePanel.updateData(players, communityCards, pot, "Showdown!");
        tablePanel.paintImmediately(0, 0, tablePanel.getWidth(), tablePanel.getHeight());

        if (winner != null) {
            winner.adjustChips(pot);

            if (winner == players.get(0)) {
                SoundManager.play("win.wav");
            } else if (!players.get(0).isFolded()) {
                SoundManager.play("lose.wav"); 
            }

            JOptionPane.showMessageDialog(this, winner.getName() + " wins $" + pot + " with " + winningHandDesc + "!");
        } else {
            JOptionPane.showMessageDialog(this, "Everyone folded! No winner.");
        }


    }

    private boolean checkEarlyWin() {
        int activeCount = 0;
        Player winner = null;
        
        for (Player p : players) {
            if (!p.isFolded()) {
                activeCount++;
                winner = p;
            }
        }

        if (activeCount <= 1) {
            if (activeCount == 1) {
                winner.adjustChips(pot);
                tablePanel.updateData(players, communityCards, pot, "Winner!");
                tablePanel.paintImmediately(0, 0, tablePanel.getWidth(), tablePanel.getHeight());
                JOptionPane.showMessageDialog(this, winner.getName() + " wins $" + pot + " because everyone else folded!");
            } else {
                JOptionPane.showMessageDialog(this, "Everyone folded! No winner.");
            }
            
            btnCall.setEnabled(true);
            btnRaise.setEnabled(true);
            btnFold.setEnabled(true);
            startNewRound();
            
            return true;
        }
        
        return false;
    }





    public static void main(String[] args) {
        new GameEngine().setVisible(true);
    }
}