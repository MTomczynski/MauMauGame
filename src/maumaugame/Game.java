/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maumaugame;

import java.rmi.*;
import java.util.ArrayList;
import java.util.List;

public interface Game extends Remote {

    public void dolacz(Client k) throws RemoteException;

    public void wiadomosc(Client k, String s) throws RemoteException;

    public void opusc(Client k) throws RemoteException;
    
    public void playACard(Client k, Card c) throws RemoteException;
    
    public void drawACard(Client k) throws RemoteException;
    
    public void drawACard(Client k, int quantity) throws RemoteException;
    
    public void changeTurn(Client k) throws RemoteException;
    
    public Card getCurrentCard() throws RemoteException;
    
    public boolean getFunctionApplied() throws RemoteException;
    
    public void setFunctionApplied(boolean b) throws RemoteException;
}
