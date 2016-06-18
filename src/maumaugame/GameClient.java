package maumaugame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.rmi.RemoteException;
import java.rmi.registry.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameClient extends JFrame
{

    //GUI
    private JButton polacz, rozlacz, endTurn;
    ArrayList<JButton> cards = new ArrayList<>();
    private JPanel panel, handPanel, currentCardPanel;
    private JTextField host, wiadomosc;
    private JTextArea komunikaty;
    private JList<String> zalogowani;
    private DefaultListModel<String> listaZalogowanych;
    //Klient
    private String nazwaSerwera = "localhost";
    private Klient watekKlienta;
    private GameClient instancjaKlienta;
    private Game serwer;
    private ClientImpl klient;
    ObslugaZdarzen obsluga;

    public Hand hand = new Hand();
    public boolean myTurn;

    public GameClient()
    {

        super("Klient");

        instancjaKlienta = this;

        setSize(900, 600);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        panel = new JPanel(new FlowLayout());
        handPanel = new JPanel(new FlowLayout());
        currentCardPanel = new JPanel(new FlowLayout());

        komunikaty = new JTextArea();
        komunikaty.setLineWrap(true);
        komunikaty.setEditable(false);

        wiadomosc = new JTextField();

        host = new JTextField(nazwaSerwera, 12);
        polacz = new JButton("Połącz");
        rozlacz = new JButton("Rozłącz");
        endTurn = new JButton("Zakoncz ture");
        rozlacz.setEnabled(false);

        listaZalogowanych = new DefaultListModel<String>();
        zalogowani = new JList<String>(listaZalogowanych);
        zalogowani.setFixedCellWidth(120);

        obsluga = new ObslugaZdarzen();

        polacz.addActionListener(obsluga);
        rozlacz.addActionListener(obsluga);
        endTurn.addActionListener(obsluga);

        wiadomosc.addKeyListener(obsluga);

        addWindowListener(new WindowAdapter()
        {

            public void windowClosing(WindowEvent e)
            {
                rozlacz.doClick();
                setVisible(false);
                System.exit(0);
            }
        });

        add(panel);
        add(handPanel);
        add(currentCardPanel);

        panel.add(new JLabel("Serwer RMI: "));
        panel.add(host);
        panel.add(polacz);
        panel.add(rozlacz);
        panel.add(endTurn);

        add(panel, BorderLayout.NORTH);

        add(new JScrollPane(komunikaty), BorderLayout.WEST);
        komunikaty.setPreferredSize(new Dimension(200, 400));
        add(new JScrollPane(zalogowani), BorderLayout.EAST);

        add(wiadomosc, BorderLayout.SOUTH);
        add(handPanel, BorderLayout.CENTER);
        panel.add(new JLabel("Aktualna karta: "));
        panel.add(currentCardPanel, BorderLayout.EAST);

        setVisible(true);
    }

    private class ObslugaZdarzen extends KeyAdapter implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            Object src = e.getSource();
            if (e.getActionCommand().equals("Połącz"))
            {
                wyswietlKomunikat("Lacze z: " + nazwaSerwera + "...");
                polacz.setEnabled(false);
                rozlacz.setEnabled(true);
                host.setEnabled(false);
                watekKlienta = new Klient();
                watekKlienta.start();
            }
            if (e.getActionCommand().equals("Rozłącz"))
            {
                listaZalogowanych.clear();
                try
                {
                    serwer.opusc(klient);
                } catch (Exception ex)
                {
                    System.out.println("Błąd: " + ex);
                }
                rozlacz.setEnabled(false);
                polacz.setEnabled(true);
                host.setEnabled(true);
            }
            if (e.getActionCommand().equals("Zakoncz ture"))
            {
                try
                {
                    applyFunction();
                    handRefresh();
                    serwer.changeTurn(klient);
                    myTurn = false;
                    currentCardRefresh();
                    handRulesRefresh();
                    serwer.listRefresh(klient);
                } catch (RemoteException ex)
                {
                    Logger.getLogger(GameClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            for (int i = 0; i < cards.size(); i++)
            {
                if (src == cards.get(i))
                {

                    try
                    {
                        if (serwer.getFunctionApplied())
                        {
                            serwer.setFunctionApplied(false);
                        }
                        serwer.playACard(klient, hand.getCard(i));
                        hand.removeCard(i);
                        handRefresh();
                        serwer.changeTurn(klient);
                        myTurn = false;
                        currentCardRefresh();
                        handRulesRefresh();
                        serwer.listRefresh(klient);
                        
                        if (hand.getCardCount() == 0)
                        {
                            wyswietlKomunikat("Wygrales!!! BRAWO");
                            serwer.opusc(klient);
                        }

                    } catch (RemoteException ex)
                    {
                        Logger.getLogger(GameClient.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

        }

        public void keyReleased(KeyEvent e)
        {
            if (e.getKeyCode() == 10)
            {
                try
                {
                    serwer.wiadomosc(klient, wiadomosc.getText());
                    wiadomosc.setText("");
                } catch (Exception ex)
                {
                    System.out.println("Błąd: " + ex);
                }
            }
        }
    }

    private class Klient extends Thread
    {

        public void run()
        {
            try
            {
                Registry rejestr = LocateRegistry.getRegistry(host.getText());
                serwer = (Game) rejestr.lookup("RMICzat");
                wyswietlKomunikat("Połączyłem się z serwerem.");
                String nick = JOptionPane.showInputDialog(null, "Podaj nick: ");
                klient = new ClientImpl(instancjaKlienta, nick);
                serwer.dolacz(klient);
                serwer.drawACard(klient, 5);
                handRefresh();
                currentCardRefresh();
                handRulesRefresh();
            } catch (Exception e)
            {
                System.out.println("Błąd połączenia: " + e);
            }
        }
    }

    private void applyFunction() throws RemoteException
    {
        if (serwer.getFunctionApplied())
        {
            serwer.drawACard(klient);
            return;
        }

        Card currCard = serwer.getCurrentCard();
        switch (currCard.getValue())
        {
            case 1:
                serwer.setFunctionApplied(true);
                break;
            case 2:
                serwer.drawACard(klient, 2);
                serwer.setFunctionApplied(true);
                break;
            case 3:
                serwer.drawACard(klient, 3);
                serwer.setFunctionApplied(true);
                break;
            case 4:
                serwer.setFunctionApplied(true);
                break;
            case 11:
                serwer.setFunctionApplied(true);
                break;
            case 13:
                serwer.drawACard(klient, 5);
                serwer.setFunctionApplied(true);
                break;
            default:
                serwer.drawACard(klient);
                break;
        }
    }

    private boolean cardRules(Card c) throws RemoteException
    {
        Card currCard = serwer.getCurrentCard();
        int playingCard = c.getValue();
        int tableCard = currCard.getValue();
        if (tableCard == playingCard)
        {
            return true;
        }

        if (serwer.getFunctionApplied())
        {
            if (playingCard == tableCard || c.getSuit() == currCard.getSuit())
            {
                return true;
            }
        }

        if (tableCard == 1 || tableCard == 2 || tableCard == 3
                || tableCard == 4 || tableCard == 11 || tableCard == 13)
        {
            if (playingCard == tableCard)
            {
                return true;
            } else
            {
                return false;
            }
        }
        if (c.getSuit() == currCard.getSuit()
                || tableCard == 12
                || playingCard == 12)
        {
            return true;
        }
        return false;
    }

    public void wyswietlKomunikat(String tekst)
    {
        komunikaty.append(tekst + "\n");
        komunikaty.setCaretPosition(komunikaty.getDocument().getLength());
    }

    public void handRefresh() throws RemoteException
    {
        handPanel.removeAll();
        cards.removeAll(cards);
        obsluga = new ObslugaZdarzen();
        for (int i = 0; i < hand.getCardCount(); i++)
        {
            ImageIcon icon = createImageIcon("CardsRes/" + hand.getCard(i).getValueAsString()
                    + "" + hand.getCard(i).getSuitAsStringShort()
                    + ".jpg");
            cards.add(new JButton(icon));
            handPanel.add(cards.get(i));
            cards.get(i).addActionListener(obsluga);
            cards.get(i).setEnabled(false);
            cards.get(i).setPreferredSize(new Dimension(80, 108));
            handPanel.revalidate();
            validate();
        }
    }

    public void handRulesRefresh() throws RemoteException
    {
        for (int i = 0; i < hand.getCardCount(); i++)
        {
            if (myTurn)
            {
                if (!cardRules(hand.getCard(i)))
                {
                    cards.get(i).setEnabled(false);
                } else
                {
                    cards.get(i).setEnabled(true);
                }
                endTurn.setEnabled(true);
            }
        }
        if (!myTurn)
        {

            endTurn.setEnabled(false);
            for (JButton j : cards)
            {
                if (j == null)
                {
                    return;
                }
                j.setEnabled(false);
            }
        }
    }

    protected static ImageIcon createImageIcon(String path)
    {
        java.net.URL imgURL = GameClient.class.getResource(path);
        return new ImageIcon(imgURL);
    }

    public void currentCardRefresh() throws RemoteException
    {
        currentCardPanel.removeAll();
        Card c;
        c = serwer.getCurrentCard();
        ImageIcon icon = createImageIcon("CardsRes/" + c.getValueAsString()
                + "" + c.getSuitAsStringShort()
                + ".jpg");
        JLabel imageLabel = new JLabel(icon);
        imageLabel.setSize(80, 108);
        currentCardPanel.add(imageLabel);
        currentCardPanel.revalidate();
        validate();
    }

    public void odswiezListe(Vector<Client> lista) throws RemoteException
    {

        listaZalogowanych.clear();
        for (Client n : lista)
        {
            try
            {
                if (n.getTurn())
                {
                    listaZalogowanych.addElement(n.pobierzNicka() + " - THINKING..");
                } else if (!n.getTurn())
                {
                    listaZalogowanych.addElement(n.pobierzNicka());
                }
                System.out.println(n.pobierzNicka());

            } catch (Exception e)
            {
                System.out.println("Błąd: " + e);
            }
        }
    }

    public static void main(String[] args)
    {
        new GameClient();
    }
}
