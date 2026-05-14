import java.util.ArrayList;
import java.util.List;

public class Player {
    private String name;
    private int chips;
    private String actionText = "";
    private List<Card> hand = new ArrayList<>();
    private boolean isFolded = false;
    private boolean isAI;

    public Player(String name, int chips, boolean isAI) {
        this.name = name;
        this.chips = chips;
        this.isAI = isAI;
    }

    public int bet(int amount) {
        if (chips >= amount) {
            chips -= amount;
            return amount;
        } else {
            // If they don't have enough, they go All-In
            int allIn = chips;
            chips = 0;
            return allIn;
        }
    }

    public void addCard(Card c) { hand.add(c); }
    public void clearHand() { hand.clear(); isFolded = false; actionText = ""; }
    public List<Card> getHand() { return hand; }
    public int getChips() { return chips; }
    public void adjustChips(int amount) { chips += amount; }
    public boolean isFolded() { return isFolded; }
    public void fold() { isFolded = true; }
    public boolean isAI() { return isAI; }
    public String getName() { return name; }
    public String getActionText() { return actionText; }
    public void setActionText(String actionText) { this.actionText = actionText; }
}