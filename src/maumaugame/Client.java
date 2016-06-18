package maumaugame;

import java.rmi.*;
import java.util.*;

public interface Client extends Remote
{

    public void wiadomoscPowitalna(String nick, Vector<Client> lista) throws RemoteException;

    public void wiadomosc(String nick, String wiadomosc) throws RemoteException;

    public void wiadomoscKonczaca(String nick, Vector<Client> lista) throws RemoteException;

    public String pobierzNicka() throws RemoteException;

    public void ustawNicka(String nick) throws RemoteException;

    public void drawACard(Card c) throws RemoteException;

    public void changeTurn(boolean d) throws RemoteException;

    public boolean getTurn() throws RemoteException;

    public void tableRefresh() throws RemoteException;
    
    public void listRefresh(Vector<Client> lista) throws RemoteException;
    
    public void endOfTheGameToast(String nick) throws RemoteException;
}
