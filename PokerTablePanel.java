import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PokerTablePanel extends JPanel {
    private List<Player> players;
    private List<Card> communityCards;
    private int currentPot = 0;
    private String handStrength = "";
    
    private final int CARD_W = 80; 
    private final int CARD_H = 112; 

    public void updateData(List<Player> players, List<Card> community, int pot, String handStrength) {
        this.players = players;
        this.communityCards = community;
        this.currentPot = pot;
        this.handStrength = handStrength;
        this.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(new Color(30, 100, 50)); 

        if (players == null) return;

        // --- NEW: Calculate the center of the screen dynamically ---
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            int x = 0, y = 0;

            // Anchor players to the edges and center
            if (i == 0) { x = centerX - 40; y = getHeight() - 150; }     // Bottom (You)
            else if (i == 1) { x = 30; y = centerY - 56; }               // Left
            else if (i == 2) { x = centerX - 40; y = 30; }               // Top
            else if (i == 3) { x = getWidth() - 130; y = centerY - 56; } // Right

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString(p.getName() + ": $" + p.getChips(), x, y - 10);
            
            g.setColor(Color.ORANGE);
            g.drawString(p.getActionText(), x, y + CARD_H + 20);

            if (!p.isFolded()) {
                for (int c = 0; c < p.getHand().size(); c++) {
                    g.drawImage(p.getHand().get(c).getTexture(), x + (c * 30), y, CARD_W, CARD_H, null);
                }
            }
        }

        int commXStart = centerX - ((communityCards.size() * 90) / 2);
        for (int i = 0; i < communityCards.size(); i++) {
            g.drawImage(communityCards.get(i).getTexture(), commXStart + (i * 90) + 10, centerY - 56, CARD_W, CARD_H, null);
        }

        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 22));
        g.drawString("POT: $" + currentPot, centerX - 40, centerY - 80); 

        // Anchor hand strength to bottom right
        g.setColor(Color.CYAN);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Hand: " + handStrength, getWidth() - 250, getHeight() - 30); 
    }
}