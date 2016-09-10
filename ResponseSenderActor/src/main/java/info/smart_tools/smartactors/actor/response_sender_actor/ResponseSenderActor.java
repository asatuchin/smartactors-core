package info.smart_tools.smartactors.actor.response_sender_actor;

import info.smart_tools.smartactors.actor.response_sender_actor.exceptions.ResponseSenderActorException;
import info.smart_tools.smartactors.actor.response_sender_actor.wrapper.ResponseSenderMessage;
import info.smart_tools.smartactors.core.ichannel_handler.IChannelHandler;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject_wrapper.IObjectWrapper;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iresponse.IResponse;
import info.smart_tools.smartactors.core.iresponse_content_strategy.IResponseContentStrategy;
import info.smart_tools.smartactors.core.iresponse_sender.IResponseSender;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

/**
 * Actor for sending response to client
 */
public class ResponseSenderActor {
    /**
     * Constructor for actor
     */
    public ResponseSenderActor() {
    }

    /**
     * Handler of the actor for send response
     *
     * @param message Wrapper of the actor
     * @throws ResponseSenderActorException if there are some problems on sending response
     */
    // TODO: 21.07.16 Remake with using interface
    public void sendResponse(final ResponseSenderMessage message)
            throws ResponseSenderActorException {

        IObjectWrapper messageWrapper = (IObjectWrapper) message;

        //Get response IObject
        IFieldName responseFieldName = null;
        try {
            responseFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "response");
            IObject responseIObject = messageWrapper.getEnvironmentIObject(responseFieldName);

            //Create and fill full environment
            IObject environment = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));
            IFieldName contextFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "context");
            IFieldName configFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "config");
            IFieldName messageFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "message");
            IFieldName endpointName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "endpointName");
            environment.setValue(responseFieldName, responseIObject);
            environment.setValue(configFieldName, messageWrapper.getEnvironmentIObject(configFieldName));
            environment.setValue(contextFieldName, messageWrapper.getEnvironmentIObject(contextFieldName));
            environment.setValue(messageFieldName, messageWrapper.getEnvironmentIObject(messageFieldName));

            IFieldName channelFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "channel");
            IChannelHandler channelHandler = (IChannelHandler)
                    messageWrapper.getEnvironmentIObject(contextFieldName).getValue(channelFieldName);

            IResponse response = IOC.resolve(Keys.getOrAdd(IResponse.class.getCanonicalName()));
            IResponseContentStrategy contentStrategy =
                    IOC.resolve(Keys.getOrAdd(IResponseContentStrategy.class.getCanonicalName()), environment);
            contentStrategy.setContent(responseIObject, response);

            IResponseSender sender = IOC.resolve(Keys.getOrAdd(IResponseSender.class.getCanonicalName()),
                    IOC.resolve(Keys.getOrAdd("http_request_key_for_response_sender"), environment),
                    messageWrapper.getEnvironmentIObject(contextFieldName).getValue(endpointName));
            sender.send(response, environment, channelHandler);
        } catch (Exception e) {
            throw new ResponseSenderActorException(e);
        }
    }
}