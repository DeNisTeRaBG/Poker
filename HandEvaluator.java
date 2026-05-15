import java.util.*;

public class HandEvaluator {

    public static class HandResult {
        public String description;
        public int score;

        public HandResult(String desc, int score) {
            this.description = desc;
            this.score = score;
        }
    }

    public static HandResult evaluate(List<Card> playerHand, List<Card> communityCards) {
        if (playerHand == null || playerHand.isEmpty()) return new HandResult("None", 0);

        List<Card> cards = new ArrayList<>(playerHand);
        for (Card c : communityCards) {
            if (c.isFaceUp()) cards.add(c);
        }

        cards.sort((a, b) -> b.getRank().getValue() - a.getRank().getValue());

        Map<Integer, Integer> rankCounts = new HashMap<>();
        Map<Card.Suit, Integer> suitCounts = new HashMap<>();
        
        for (Card c : cards) {
            int val = c.getRank().getValue();
            rankCounts.put(val, rankCounts.getOrDefault(val, 0) + 1);
            suitCounts.put(c.getSuit(), suitCounts.getOrDefault(c.getSuit(), 0) + 1);
        }

        int quads = 0;
        List<Integer> trips = new ArrayList<>();
        List<Integer> pairs = new ArrayList<>();

        for (Map.Entry<Integer, Integer> entry : rankCounts.entrySet()) {
            if (entry.getValue() == 4) quads = entry.getKey();
            else if (entry.getValue() == 3) trips.add(entry.getKey());
            else if (entry.getValue() == 2) pairs.add(entry.getKey());
        }
        
        trips.sort(Collections.reverseOrder());
        pairs.sort(Collections.reverseOrder());

        Card.Suit flushSuit = null;
        for (Map.Entry<Card.Suit, Integer> entry : suitCounts.entrySet()) {
            if (entry.getValue() >= 5) flushSuit = entry.getKey();
        }
        List<Card> flushCards = new ArrayList<>();
        if (flushSuit != null) {
            for (Card c : cards) if (c.getSuit() == flushSuit) flushCards.add(c);
        }

        int straightHigh = getStraightHighCard(cards);
        int straightFlushHigh = flushCards.size() >= 5 ? getStraightHighCard(flushCards) : 0;

        if (straightFlushHigh > 0) {
            if (straightFlushHigh == 14) return new HandResult("Royal Flush!", 900000);
            return new HandResult("Straight Flush (High " + getRankName(straightFlushHigh) + ")", 800000 + straightFlushHigh);
        }

        if (quads > 0) {
            return new HandResult("Four of a Kind " + getRankName(quads) + "s", 700000 + quads);
        }

        if (!trips.isEmpty()) {
            if (trips.size() > 1) { // Two sets of 3. Highest is trips, second is pair.
                return new HandResult("Full House " + getRankName(trips.get(0)) + "s over " + getRankName(trips.get(1)) + "s", 600000 + (trips.get(0) * 100) + trips.get(1));
            } else if (!pairs.isEmpty()) {
                return new HandResult("Full House " + getRankName(trips.get(0)) + "s over " + getRankName(pairs.get(0)) + "s", 600000 + (trips.get(0) * 100) + pairs.get(0));
            }
        }

        if (flushSuit != null) {
            return new HandResult("Flush (" + flushSuit + ")", 500000 + flushCards.get(0).getRank().getValue());
        }

        if (straightHigh > 0) {
            return new HandResult("Straight (High " + getRankName(straightHigh) + ")", 400000 + straightHigh);
        }

        if (!trips.isEmpty()) {
            return new HandResult("Three of a Kind " + getRankName(trips.get(0)) + "s", 300000 + trips.get(0));
        }

        if (pairs.size() >= 2) {
            return new HandResult("Two Pair " + getRankName(pairs.get(0)) + "s & " + getRankName(pairs.get(1)) + "s", 200000 + (pairs.get(0) * 100) + pairs.get(1));
        }

        if (pairs.size() == 1) {
            return new HandResult("Pair of " + getRankName(pairs.get(0)) + "s", 100000 + pairs.get(0));
        }

        int highCardVal = cards.get(0).getRank().getValue();
        return new HandResult("High Card " + getRankName(highCardVal), highCardVal);
    }

    private static int getStraightHighCard(List<Card> cardList) {
        List<Integer> uniqueRanks = new ArrayList<>();
        for (Card c : cardList) {
            if (!uniqueRanks.contains(c.getRank().getValue())) {
                uniqueRanks.add(c.getRank().getValue());
            }
        }
        
        if (uniqueRanks.contains(14)) uniqueRanks.add(1);

        int consecutiveCount = 1;
        for (int i = 0; i < uniqueRanks.size() - 1; i++) {
            if (uniqueRanks.get(i) - 1 == uniqueRanks.get(i + 1)) {
                consecutiveCount++;
                if (consecutiveCount == 5) return uniqueRanks.get(i - 3); // Found a straight! Return the top card.
            } else {
                consecutiveCount = 1; // Streak broken, reset
            }
        }
        return 0;
    }

    private static String getRankName(int val) {
        switch (val) {
            case 11: return "JACK";
            case 12: return "QUEEN";
            case 13: return "KING";
            case 14: return "ACE";
            default: return String.valueOf(val);
        }
    }
}