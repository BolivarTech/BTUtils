package com.bolivartech.utils.handler;

/**
 * <p>
 * Copyright 2016 BolivarTech INC</p>
 *
 * <p>
 * BolivarTech Homepage: <a
 * href="http://www.bolivartech.com">http://www.bolivartech.com</a>.
 * </p>
 *
 * <p>
 * This class is the BolivarTech's interface to handle the Messages received by the Handler.</p>
 *
 * <p>
 * Esta clase que define la interface para manejar los mensajes recividos por el Handler..</p>
 *
 * @author Julian Bolivar
 * @version 1.0.0 - April 20, 2016
 * @since 2016
 *
 * <p>
 * Change Logs:</p>
 * <ul>
 * <li>v1.0.0 (2016-04-20): Version Inicial.</li>
 * </ul>
 */
public interface BTMessageHandler {
    
    /**
     * Subclasses must implement this method to process received messages.
     *
     * @param msg Message Received
     */
    public void handleMessage(BTMessage msg);
    
}
