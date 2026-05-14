import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Card {
    public enum Suit { 
        CLUBS, DIAMONDS, HEARTS, SPADES, RUBYS
    }
    
    public enum Rank { 
        TWO(2, "2"), THREE(3, "3"), FOUR(4, "4"), FIVE(5, "5"), SIX(6, "6"), 
        SEVEN(7, "7"), EIGHT(8, "8"), NINE(9, "9"), TEN(10, "10"), 
        JACK(11, "J"), QUEEN(12, "Q"), KING(13, "K"), ACE(14, "A");
        
        private final int value;
        private final String letter;
        
        Rank(int v, String l) { 
            this.value = v; 
            this.letter = l; 
        }
        
        public int getValue() { return value; }
        public String getLetter() { return letter; }
    }

    private final Rank rank;
    private final Suit suit;
    private BufferedImage frontTexture;
    private static BufferedImage backTexture; // Static because it's shared
    private boolean isFaceUp = false;

    public Card(Rank rank, Suit suit, String path) {
        this.rank = rank;
        this.suit = suit;
        try {
            this.frontTexture = ImageIO.read(new File(path));
            if (backTexture == null) {
                backTexture = ImageIO.read(new File("assets/card_back.png"));
            }
        } catch (Exception e) {
            System.out.println("Texture missing: " + path);
        }
    }

    public void setFaceUp(boolean state) { this.isFaceUp = state; }
    public boolean isFaceUp() { return isFaceUp; }
    public BufferedImage getTexture() { return isFaceUp ? frontTexture : backTexture; }
    public Rank getRank() { return rank; }
    public Suit getSuit() { return suit; }
}