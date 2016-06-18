package maumaugame;

import java.rmi.*;
import java.rmi.server.*;
import java.util.*;

public class ClientImpl extends UnicastRemoteObject implements Client
{

    private static final long serialVersionUID = 98L;

    private GameClient klient;
    private String nick;
    

    public ClientImpl(GameClient klient, String nick) throws RemoteException
    {
        this.klient = klient;
        this.nick = nick;
    }

    public void wiadomoscPowitalna(String nick, Vector<Client> lista) throws RemoteException
    {
        klient.wyswietlKomunikat("Do gry dołączył/a: " + nick);
        klient.odswiezListe(lista);
    }

    public void wiadomosc(String nick, String wiadomosc) throws RemoteException
    {
        klient.wyswietlKomunikat("<" + nick + ">" + wiadomosc);
    }

    public void wiadomoscKonczaca(String nick, Vector<Client> lista) throws RemoteException
    {
        klient.wyswietlKomunikat("Grę opuścił/a: " + nick);
        klient.odswiezListe(lista);
    }

    public String pobierzNicka() throws RemoteException
    {
        return nick;
    }

    public void ustawNicka(String nick) throws RemoteException
    {
        this.nick = nick;
    }

    public void drawACard(Card c) throws RemoteException
    {
        klient.hand.addCard(c);
    }
    
    public void drawACard(Card c, int quantity) throws RemoteException
    {
        klient.hand.addCard(c);
    }

    public void changeTurn(boolean d) throws RemoteException
    {
        klient.myTurn = d;
    }

    public boolean getTurn() throws RemoteException
    {
        return klient.myTurn;
    }

    public void tableRefresh() throws RemoteException
    {
        klient.currentCardRefresh();
        klient.handRefresh();
        klient.handRulesRefresh();
    }

    public void listRefresh(Vector<Client> lista) throws RemoteException
    {
        klient.odswiezListe(lista);
    }
}
