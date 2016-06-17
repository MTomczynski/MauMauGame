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
import java.util.*;

public class GameClient extends JFrame {

    //GUI
    private JButton polacz, rozlacz;
    private JPanel panel;
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

    public GameClient() {
        super("Klient");

        instancjaKlienta = this;

        setSize(600, 500);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        panel = new JPanel(new FlowLayout());
        komunikaty = new JTextArea();
        komunikaty.setLineWrap(true);
        komunikaty.setEditable(false);

        wiadomosc = new JTextField();

        host = new JTextField(nazwaSerwera, 12);
        polacz = new JButton("Połącz");
        rozlacz = new JButton("Rozłącz");
        rozlacz.setEnabled(false);

        listaZalogowanych = new DefaultListModel<String>();
        zalogowani = new JList<String>(listaZalogowanych);
        zalogowani.setFixedCellWidth(120);

        ObslugaZdarzen obsluga = new ObslugaZdarzen();

        polacz.addActionListener(obsluga);
        rozlacz.addActionListener(obsluga);

        wiadomosc.addKeyListener(obsluga);

        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                rozlacz.doClick();
                setVisible(false);
                System.exit(0);
            }
        });

        panel.add(new JLabel("Serwer RMI: "));
        panel.add(host);
        panel.add(polacz);
        panel.add(rozlacz);

        add(panel, BorderLayout.NORTH);
        add(new JScrollPane(komunikaty), BorderLayout.CENTER);
        add(new JScrollPane(zalogowani), BorderLayout.EAST);
        add(wiadomosc, BorderLayout.SOUTH);

        setVisible(true);

    }

    private class ObslugaZdarzen extends KeyAdapter implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("Połącz")) {
                wyswietlKomunikat("Łączę z: " + nazwaSerwera + "...");
                polacz.setEnabled(false);
                rozlacz.setEnabled(true);
                host.setEnabled(false);
                watekKlienta = new Klient();
                watekKlienta.start();
            }
            if (e.getActionCommand().equals("Rozłącz")) {
                listaZalogowanych.clear();
                try {
                    serwer.opusc(klient);
                } catch (Exception ex) {
                    System.out.println("Błąd: " + ex);
                }
                rozlacz.setEnabled(false);
                polacz.setEnabled(true);
                host.setEnabled(true);
            }
        }

        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == 10) {
                try {
                    serwer.wiadomosc(klient, wiadomosc.getText());
                    wiadomosc.setText("");
                } catch (Exception ex) {
                    System.out.println("Błąd: " + ex);
                }
            }
        }
    }

    private class Klient extends Thread {

        public void run() {
            try {
                Registry rejestr = LocateRegistry.getRegistry(host.getText());
                serwer = (Game) rejestr.lookup("RMICzat");
                wyswietlKomunikat("Połączyłem się z serwerem.");
                String nick = JOptionPane.showInputDialog(null, "Podaj nick: ");
                klient = new ClientImpl(instancjaKlienta, nick);
                serwer.dolacz(klient);

            } catch (Exception e) {
                System.out.println("Błąd połączenia: " + e);
            }
        }
    }

    public void wyswietlKomunikat(String tekst) {
        komunikaty.append(tekst + "\n");
        komunikaty.setCaretPosition(komunikaty.getDocument().getLength());
    }

    public void odswiezListe(Vector<Client> lista) {

                listaZalogowanych.clear();
                
                for (Client n : lista) {
                    try {
                        listaZalogowanych.addElement(n.pobierzNicka());
                        System.out.println(n.pobierzNicka());
                    } catch (Exception e) {
                        System.out.println("Błąd: " + e);
                    }
                }
    }

    public static void main(String[] args) {
        new GameClient();
    }
}