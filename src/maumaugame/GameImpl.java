/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maumaugame;


import java.rmi.*;
import java.rmi.server.*;
import java.util.*;

public class GameImpl extends UnicastRemoteObject implements Game {

    private Vector<Client> klienci = new Vector<Client>();
    private GameServer serwer;

    public GameImpl(GameServer serwer) throws RemoteException {
        this.serwer = serwer;
    }

    public synchronized void dolacz(Client n) throws RemoteException {

        klienci.add(n);

        serwer.wyswietlKomunikat("Do czatu dołączył/a: " + n.pobierzNicka());

        for (Iterator<Client> i = klienci.iterator(); i.hasNext();) {
            Client klient = i.next();
            klient.wiadomoscPowitalna(n.pobierzNicka(), klienci);
        }
    }

    public synchronized void wiadomosc(Client n, String s) throws RemoteException {

        for (Iterator<Client> i = klienci.iterator(); i.hasNext();) {
            Client klient = i.next();
            klient.wiadomosc(n.pobierzNicka(), s);
        }
    }

    public synchronized void opusc(Client n) throws RemoteException {

        klienci.remove(n);

        serwer.wyswietlKomunikat("Czat opuścił/a: " + n.pobierzNicka());

        for (Iterator<Client> i = klienci.iterator(); i.hasNext();) {
            Client klient = (Client) i.next();
            klient.wiadomoscKonczaca(n.pobierzNicka(), klienci);
        }
    }

    @Override
    public void playACard(Client k, Card c) throws RemoteException
    {
        serwer.currentCard = c;
    }
}
