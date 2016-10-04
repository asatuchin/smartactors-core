package info.smart_tools.smartactors.core.chain_storage;

import info.smart_tools.smartactors.core.ichain_storage.IChainStorage;
import info.smart_tools.smartactors.core.ichain_storage.exceptions.ChainCreationException;
import info.smart_tools.smartactors.core.ichain_storage.exceptions.ChainNotFoundException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.core.irouter.IRouter;
import info.smart_tools.smartactors.core.message_processing.IReceiverChain;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link info.smart_tools.smartactors.core.ichain_storage.IChainStorage}.
 */
public class ChainStorage implements IChainStorage {
    private final Map<Object, IReceiverChain> chainsMap;
    private final IRouter router;

    /**
     * The constructor.
     *
     * @param chainsMap    {@link Map} to store chains in
     * @param router       {@link IRouter} to use to resolve receivers
     * @throws InvalidArgumentException if chainsMap is {@code null}
     * @throws InvalidArgumentException if router is {@code null}
     */
    public ChainStorage(final Map<Object, IReceiverChain> chainsMap, final IRouter router)
            throws InvalidArgumentException {
        if (null == chainsMap) {
            throw new InvalidArgumentException("Chains map should not be null.");
        }

        if (null == router) {
            throw new InvalidArgumentException("Router should not be null.");
        }

        this.chainsMap = chainsMap;
        this.router = router;
    }

    @Override
    public void register(final Object chainId, final IObject description)
            throws ChainCreationException {
        try {
            IReceiverChain newChain = IOC.resolve(
                    IOC.resolve(IOC.getKeyForKeyStorage(), IReceiverChain.class.getCanonicalName()),
                    chainId, description, this, router);

            IReceiverChain oldChain = chainsMap.put(chainId, newChain);

            if (null != oldChain) {
                System.out.println(MessageFormat.format("Warning: replacing chain ({0}) registered as ''{1}'' by {2}",
                        oldChain.toString(), chainId.toString(), newChain.toString()));
            }
        } catch (ResolutionException  e) {
            throw new ChainCreationException("Could not create a chain", e);
        }
    }

    @Override
    public IReceiverChain resolve(final Object chainId)
            throws ChainNotFoundException {
        IReceiverChain chain = chainsMap.get(chainId);

        if (null == chain) {
            throw new ChainNotFoundException(chainId);
        }

        return chain;
    }

    @Override
    public List<Object> enumerate() {
        return new ArrayList<>(chainsMap.keySet());
    }
}