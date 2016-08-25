package info.smart_tools.smartactors.plugin.in_memory_database;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.in_memory_database.InMemoryDatabaseIOCInitializer;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;

/**
 * Plugin for in memory database
 */
public class PluginInMemoryDatabase implements IPlugin {

    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor
     *
     * @param bootstrap bootstrap element
     */
    public PluginInMemoryDatabase(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            BootstrapItem item = new BootstrapItem("InMemoryDatabase");
            item
                    .after("IOC")
                    .after("IFieldNamePlugin")
                    .process(() -> {
                                try {
                                    InMemoryDatabaseIOCInitializer.init();
                                } catch (Exception e) {
                                    throw new ActionExecuteException("Failed to load plugin \"NestedFieldName\"", e);
                                }
                            }
                    );
            bootstrap.add(item);
        } catch (InvalidArgumentException e) {
            throw new PluginException(e);
        }
    }

}