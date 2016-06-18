/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
}
