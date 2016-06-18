/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maumaugame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class GameClient extends JFrame
{

    //GUI
    private JButton polacz, rozlacz, info, endTurn, handRefreshButton;
    private JButton[] cards = new JButton[30];
    private JPanel container, panel, handPanel, currentCardPanel;
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

        setSize(700, 600);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        container = new JPanel();
//        panel = new JPanel(new FlowLayout());
//        handPanel = new JPanel(new FlowLayout());
//        currentCardPanel = new JPanel(new FlowLayout());
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
        panel = new JPanel();
        handPanel = new JPanel();
        currentCardPanel = new JPanel();

        komunikaty = new JTextArea();
        komunikaty.setLineWrap(true);
        komunikaty.setEditable(false);

        wiadomosc = new JTextField();

        host = new JTextField(nazwaSerwera, 12);
        polacz = new JButton("Połącz");
        rozlacz = new JButton("Rozłącz");
        info = new JButton("Info");
        endTurn = new JButton("End turn");
        handRefreshButton = new JButton("Hand");
        rozlacz.setEnabled(false);

        listaZalogowanych = new DefaultListModel<String>();
        zalogowani = new JList<String>(listaZalogowanych);
        zalogowani.setFixedCellWidth(120);

        obsluga = new ObslugaZdarzen();

        polacz.addActionListener(obsluga);
        rozlacz.addActionListener(obsluga);
        info.addActionListener(obsluga);
        endTurn.addActionListener(obsluga);
        handRefreshButton.addActionListener(obsluga);

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

        container.add(panel);
        container.add(handPanel);
        container.add(currentCardPanel);

        panel.add(new JLabel("Serwer RMI: "));
        panel.add(host);
        panel.add(polacz);
        panel.add(rozlacz);
        panel.add(info);
        panel.add(endTurn);
        panel.add(handRefreshButton);

        add(panel, BorderLayout.NORTH);

        add(new JScrollPane(komunikaty), BorderLayout.CENTER);
        add(new JScrollPane(zalogowani), BorderLayout.EAST);

        add(wiadomosc, BorderLayout.SOUTH);
        add(handPanel, BorderLayout.SOUTH);
        add(currentCardPanel, BorderLayout.EAST);

        setVisible(true);

    }

    private class ObslugaZdarzen extends KeyAdapter implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            Object src = e.getSource();
            if (e.getActionCommand().equals("Połącz"))
            {
                wyswietlKomunikat("Łączę z: " + nazwaSerwera + "...");
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
            if (e.getActionCommand().equals("Info"))
            {
                for (Card c : hand.hand)
                {
                    wyswietlKomunikat(c.getSuitAsString() + " "
                            + c.getValueAsString());
                }
                wyswietlKomunikat(String.valueOf(myTurn));
                repaint();
            }
            if (e.getActionCommand().equals("End turn"))
            {
                try
                {
                    serwer.changeTurn(klient);
                } catch (RemoteException ex)
                {
                    Logger.getLogger(GameClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (e.getActionCommand().equals("Hand"))
            {

                try
                {
                    handRefresh();
                } catch (RemoteException ex)
                {
                    Logger.getLogger(GameClient.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
            for (int i = 0; i < cards.length; i++)
            {
                if (src == cards[i])
                {

                    try
                    {
                        serwer.playACard(klient, hand.getCard(i));
                        hand.removeCard(i);
                        handRefresh();
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
            } catch (Exception e)
            {
                System.out.println("Błąd połączenia: " + e);
            }
        }
    }

    private boolean cardRules(Card c) throws RemoteException
    {
        Card currCard = serwer.getCurrentCard();
        if (c.getSuit() == currCard.getSuit() || c.getValue() == currCard.getValue())
        {
            return true;
        } else if (c.getValue() == 12)
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
        obsluga = new ObslugaZdarzen();
        for (int i = 0; i < hand.getCardCount(); i++)
        {
            ImageIcon icon = createImageIcon("CardsRes/" + hand.getCard(i).getValueAsString()
                    + "" + hand.getCard(i).getSuitAsStringShort()
                    + ".jpg");
            cards[i] = new JButton(icon);
            handPanel.add(cards[i]);
            cards[i].addActionListener(obsluga);

            if (myTurn)
            {
                if (!cardRules(hand.getCard(i)))
                {
                    cards[i].setEnabled(false);
                } else
                {
                    cards[i].setEnabled(true);
                }
            }

            handPanel.revalidate();
            validate();
        }

//        if (!myTurn)
//        {
//            for (JButton b : cards)
//            {
//                b.setEnabled(false);
//            }
//            endTurn.setEnabled(false);
//        } else if (myTurn)
//        {
//            for (int i = 0; i < hand.getCardCount(); i++)
//            {
//
//                if (!cardRules(hand.getCard(i)))
//                {
//                    cards[i].setEnabled(false);
//                } else
//                {
//                    cards[i].setEnabled(true);
//                }
//            }
//        }
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
        Card c;
        c = serwer.getCurrentCard();
        listaZalogowanych.addElement(c.getValueAsString() + " " + c.getSuitAsString());
        for (Client n : lista)
        {
            try
            {
                listaZalogowanych.addElement(n.pobierzNicka());
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
