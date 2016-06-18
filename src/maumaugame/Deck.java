package maumaugame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;

public class Deck
{

    ArrayList<Card> deck = new ArrayList<Card>();
    ArrayList<Card> cardsUsed = new ArrayList<Card>();

    public Deck()
    {
        this(false);  // Just call the other constructor in this class.
    }

    public Deck(boolean includeJokers)
    {
        for (int suit = 0; suit <= 3; suit++)
        {
            for (int value = 1; value <= 13; value++)
            {
                deck.add(new Card(value, suit));
            }
        }
        if (includeJokers)
        {
            deck.add(new Card(1, Card.JOKER));
            deck.add(new Card(2, Card.JOKER));
        }
    }

    public void shuffleAll()
    {
        deck.removeAll(cardsUsed); //removing duplicates, just in case
        deck.addAll(cardsUsed);
        long seed = System.nanoTime();
        Collections.shuffle(deck, new Random(seed));
        cardsUsed.clear();
    }

    public void shuffleUsedCards()
    {
        deck.removeAll(cardsUsed); //removing duplicates, just in case
        long seed = System.nanoTime();
        Collections.shuffle(cardsUsed, new Random(seed));
        deck.addAll(cardsUsed);
        cardsUsed.clear();
    }

    public int cardsLeft()
    {
        return deck.size();
    }

    public void removeACard(Card card)
    {
        deck.remove(card);
    }

    public void removeACard(int position)
    {
        deck.remove(position);
    }

    public void removeAll(Collection<?> c)
    {
        deck.removeAll(c);
    }

    public boolean isEmpty()
    {
        return deck.isEmpty();
    }

    public Card getACard(int c)
    {
        return deck.get(c);
    }

//    public boolean hasJokers()
//    {
//        return (deck.length == 54);
//    }
}