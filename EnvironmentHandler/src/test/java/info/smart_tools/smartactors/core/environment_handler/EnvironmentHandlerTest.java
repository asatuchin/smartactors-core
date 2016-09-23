package info.smart_tools.smartactors.core.environment_handler;


import info.smart_tools.smartactors.base.strategy.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.core.ds_object.DSObject;
import info.smart_tools.smartactors.core.field_name.FieldName;
import info.smart_tools.smartactors.core.ienvironment_handler.IEnvironmentHandler;
import info.smart_tools.smartactors.core.ienvironment_handler.exception.EnvironmentHandleException;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iqueue.IQueue;
import info.smart_tools.smartactors.scope.iscope.IScope;
import info.smart_tools.smartactors.scope.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.message_processing.IMessageProcessingSequence;
import info.smart_tools.smartactors.core.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.core.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.core.message_processing.IReceiverChain;
import info.smart_tools.smartactors.core.message_processing_sequence.MessageProcessingSequence;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.receiver_chain.ImmutableReceiverChain;
import info.smart_tools.smartactors.core.resolve_by_name_ioc_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.core.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.core.blocking_queue.*;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class EnvironmentHandlerTest {

    IMessageProcessor messageProcessor;

    @Before
    public void setUp() throws ScopeProviderException, RegistrationException, ResolutionException, InvalidArgumentException {
        ScopeProvider.subscribeOnCreationNewScope(
                scope -> {
                    try {
                        scope.setValue(IOC.getIocKey(), new StrategyContainer());
                    } catch (Exception e) {
                        throw new Error(e);
                    }
                }
        );

        Object keyOfMainScope = ScopeProvider.createScope(null);
        IScope mainScope = ScopeProvider.getScope(keyOfMainScope);
        ScopeProvider.setCurrentScope(mainScope);
        IOC.register(
                IOC.getKeyForKeyStorage(),
                new ResolveByNameIocStrategy()
        );
        IKey keyIObjectByString = Keys.getOrAdd("IObjectByString");
        IKey keyIObject = Keys.getOrAdd(IObject.class.getCanonicalName());
        IKey keyIMessageProcessingSequence = Keys.getOrAdd(IMessageProcessingSequence.class.getCanonicalName());
        IKey keyIReceiverChain = Keys.getOrAdd(IReceiverChain.class.toString());
        IKey keyIFieldName = Keys.getOrAdd(IFieldName.class.getCanonicalName());
        IKey keyFieldName = Keys.getOrAdd(FieldName.class.getCanonicalName());
        IOC.register(
                keyIObjectByString,
                new CreateNewInstanceStrategy(
                        (args) -> {
                            try {
                                return new DSObject((String) args[0]);
                            } catch (InvalidArgumentException ignored) {
                            }
                            return null;
                        }
                )
        );
        IOC.register(
                keyIMessageProcessingSequence,
                new CreateNewInstanceStrategy(
                        (args) -> {
                            try {
                                return new MessageProcessingSequence((int) args[0], (IReceiverChain) args[1]);
                            } catch (InvalidArgumentException | ResolutionException ignored) {
                            }
                            return null;
                        }
                )
        );
        IOC.register(
                keyIFieldName,
                new CreateNewInstanceStrategy(
                        (args) -> {
                            try {
                                return new FieldName((String) args[0]);
                            } catch (InvalidArgumentException ignored) {
                            }
                            return null;
                        }
                )
        );
        IOC.register(
                keyFieldName,
                new CreateNewInstanceStrategy(
                        (args) -> {
                            try {
                                return new FieldName((String) args[0]);
                            } catch (InvalidArgumentException ignored) {
                            }
                            return null;
                        }
                )
        );
    }

    @Test
    public void whenEnvironmentHandlerReceiveEnvironment_ItShouldProcessMessageProcessor()
            throws ResolutionException, InvalidArgumentException, RegistrationException, EnvironmentHandleException {
        messageProcessor = mock(IMessageProcessor.class);
        IKey keyIMessageProcessor = Keys.getOrAdd(IMessageProcessor.class.getCanonicalName());
        IOC.register(
                keyIMessageProcessor,
                new SingletonStrategy(messageProcessor)
        );
        IObject iObject = IOC.resolve(Keys.getOrAdd("IObjectByString"), "{}");
        IKey keyIObject = Keys.getOrAdd(IObject.class.getCanonicalName());
        IOC.register(
                keyIObject,
                new SingletonStrategy(iObject)
        );
        IObject environment = IOC.resolve(Keys.getOrAdd("IObjectByString"), "{\"message\": {\"hello\": \"world\"}, \"context\": null}");
        Map<Class<? extends Throwable>, IObject> exceptionalChainsAndEnv = new HashMap<>();
        exceptionalChainsAndEnv.put(InvalidArgumentException.class, null);
        IMessageReceiver messageReceivers[] = new IMessageReceiver[1];
        IObject iObjects[] = new IObject[1];
        messageReceivers[0] = null;
        iObjects[0] = null;
        IReceiverChain chain = new ImmutableReceiverChain("name", messageReceivers, iObjects, exceptionalChainsAndEnv);
        IQueue<ITask> queue = new BlockingQueue(null);

        IEnvironmentHandler handler = new EnvironmentHandler(queue, 1);
        handler.handle(environment, chain, null);
        try {
            verify(messageProcessor, times(1)).process(
                    (IObject) environment.getValue(IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "message")),
                    (IObject) environment.getValue(IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "context")));
        } catch (ReadValueException | ChangeValueException e) {
            e.printStackTrace();
        }
    }
}
