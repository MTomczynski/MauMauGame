/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maumaugame;

import java.io.Serializable;
import java.rmi.*;
import java.rmi.server.*;
import java.util.*;

public class GameImpl extends UnicastRemoteObject implements Game, Serializable
{

    private Vector<Client> klienci = new Vector<Client>();
    private GameServer serwer;

    public GameImpl(GameServer serwer) throws RemoteException
    {
        this.serwer = serwer;
    }

    public synchronized void dolacz(Client n) throws RemoteException
    {

        klienci.add(n);

        serwer.wyswietlKomunikat("Do czatu dołączył/a: " + n.pobierzNicka());
        if (klienci.size() == 1)
        {
            n.changeTurn(true);
        }

        for (Iterator<Client> i = klienci.iterator(); i.hasNext();)
        {
            Client klient = i.next();
            klient.wiadomoscPowitalna(n.pobierzNicka(), klienci);
        }
    }

    public synchronized void wiadomosc(Client n, String s) throws RemoteException
    {

        for (Iterator<Client> i = klienci.iterator(); i.hasNext();)
        {
            Client klient = i.next();
            klient.wiadomosc(n.pobierzNicka(), s);
        }
    }

    public synchronized void opusc(Client n) throws RemoteException
    {

        klienci.remove(n);

        serwer.wyswietlKomunikat("Czat opuścił/a: " + n.pobierzNicka());

        for (Iterator<Client> i = klienci.iterator(); i.hasNext();)
        {
            Client klient = (Client) i.next();
            klient.wiadomoscKonczaca(n.pobierzNicka(), klienci);
        }
    }

    public synchronized void playACard(Client n, Card c) throws RemoteException
    {
        serwer.currentCard = c;
        for (Iterator<Client> i = klienci.iterator(); i.hasNext();)
        {

            Client klient = i.next();
            if (!klient.equals(n))
            {
                klient.tableRefresh();
            }

        }
    }

    public synchronized void drawACard(Client n) throws RemoteException
    {
        Card c = serwer.deck.getACard(0);
        serwer.deck.removeACard(c);
        n.drawACard(c);
    }

    public synchronized void drawACard(Client n, int quantity) throws RemoteException
    {
        for (int i = 0; i < quantity; i++)
        {
            Card c = serwer.deck.getACard(0);
            serwer.deck.removeACard(c);
            n.drawACard(c);
        }
    }

    public void changeTurn(Client n) throws RemoteException
    {
        boolean pass = false;
        for (Iterator<Client> i = klienci.iterator(); i.hasNext();)
        {
            Client klient = (Client) i.next();

            if (pass && !klient.equals(n))
            {
                klient.changeTurn(true);
                klient.tableRefresh();
                return;
            }

            if (klient.equals(n))
            {
                pass = true;
            }
        }
        klienci.elementAt(0).changeTurn(true);
        klienci.elementAt(0).tableRefresh();
    }
    public Card getCurrentCard() throws RemoteException
    {
        return serwer.currentCard;
    }

    public boolean getFunctionApplied() throws RemoteException
    {
        return serwer.functionApplied;
    }

    @Override
    public void setFunctionApplied(boolean b) throws RemoteException
    {
        serwer.functionApplied = b;
    }
}
