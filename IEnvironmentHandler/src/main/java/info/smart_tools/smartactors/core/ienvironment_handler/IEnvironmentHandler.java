package info.smart_tools.smartactors.core.ienvironment_handler;

import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.message_processing.IReceiverChain;

/**
 * Interface of environment handler, that should create MessageProcessor and process it
 */
public interface IEnvironmentHandler {
    void handle(IObject environment, IReceiverChain receiverChain);
}
