/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maumaugame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.rmi.registry.*;

public class GameServer extends JFrame {

    //GUI
    private JButton uruchom, zatrzymaj;
    private JPanel panel;
    private JTextField port;
    private JTextArea komunikaty;
    //Serwer
    private int numerPortu = 1099;
    GameServer instancjaSerwera;
    
    //Variables for the game
    static Deck deck = new Deck();
    public Card currentCard;
    static int multiFactor;

    public GameServer() {
        super("Serwer");

        instancjaSerwera = this;

        setSize(450, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        panel = new JPanel(new FlowLayout());
        komunikaty = new JTextArea();
        komunikaty.setLineWrap(true);
        komunikaty.setEditable(false);

        port = new JTextField((new Integer(numerPortu)).toString(), 8);
        uruchom = new JButton("Uruchom");
        zatrzymaj = new JButton("Zatrzymaj");
        zatrzymaj.setEnabled(false);

        ObslugaZdarzen obsluga = new ObslugaZdarzen();

        uruchom.addActionListener(obsluga);
        zatrzymaj.addActionListener(obsluga);

        panel.add(new JLabel("Port RMI: "));
        panel.add(port);
        panel.add(uruchom);
        panel.add(zatrzymaj);

        add(panel, BorderLayout.NORTH);
        add(new JScrollPane(komunikaty), BorderLayout.CENTER);

        setVisible(true);
    }

    private class ObslugaZdarzen implements ActionListener {

        private Serwer srw;

        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("Uruchom")) {
                srw = new Serwer();
                srw.start();
                uruchom.setEnabled(false);
                zatrzymaj.setEnabled(true);
                port.setEnabled(false);
                repaint();
            }
            if (e.getActionCommand().equals("Zatrzymaj")) {
                srw.kill();
                zatrzymaj.setEnabled(false);
                uruchom.setEnabled(true);
                port.setEnabled(true);
                repaint();
            }
        }
    }

    private class Serwer extends Thread {

        Registry rejestr;

        public void kill() {
            try {
                rejestr.unbind("RMICzat");
                wyswietlKomunikat("Serwer został‚ wyrejestrowany.");
            } catch (Exception e) {
                wyswietlKomunikat("Nie udało się wyrejestrować serwera.");
            }
        }

        public void run() {

            try {
                rejestr = LocateRegistry.createRegistry(new Integer(port.getText()));
                wyswietlKomunikat("Utworzyłem nowy rejestr na porcie: " + port.getText());
            } catch (Exception e) {
                wyswietlKomunikat("Nie powiodło sie utworzenie rejestru...\nPróba skorzystania z istniejącego...");
            }
            if (rejestr == null) {
                try {
                    rejestr = LocateRegistry.getRegistry();
                } catch (Exception e) {
                    wyswietlKomunikat("Brak uruchomionego rejestru.");
                }
            }
            try {
                GameImpl serwer = new GameImpl(instancjaSerwera);
                rejestr.rebind("RMICzat", serwer);
                wyswietlKomunikat("Serwer został‚ poprawnie zarejestrowany i uruchomiony.");
            } catch (Exception e) {
                wyswietlKomunikat("Nie udało się zarejestrować i uruchomić serwera.");
            }
        }
    }

    public void wyswietlKomunikat(String tekst) {
        komunikaty.append(tekst + "\n");
        komunikaty.setCaretPosition(komunikaty.getDocument().getLength());
    }

    public static void main(String[] args) {
        new GameServer();
    }
}