package info.smart_tools.smartactors.message_processing.constant_chain_choice_strategy;

import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.message_processing.chain_call_receiver.IChainChoiceStrategy;
import info.smart_tools.smartactors.message_processing.chain_call_receiver.exceptions.ChainChoiceException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;

/**
 * {@link IChainChoiceStrategy Chain choice strategy} that always returns the same chain id for the same step.
 */
public class ConstantChainChoiceStrategy implements IChainChoiceStrategy {
    private final IFieldName chainIdFieldName;

    public ConstantChainChoiceStrategy()
            throws ResolutionException {
        chainIdFieldName = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "chain");
    }

    @Override
    public Object chooseChain(IMessageProcessor messageProcessor) throws ChainChoiceException {
        try {
            Object name = messageProcessor.getSequence().getCurrentReceiverArguments().getValue(chainIdFieldName);
            return IOC.resolve(Keys.getOrAdd("chain_id_from_map_name"), name);
        } catch (Exception e) {
            throw new ChainChoiceException("Exception occurred reading chain id for current step.", e);
        }
    }
}
