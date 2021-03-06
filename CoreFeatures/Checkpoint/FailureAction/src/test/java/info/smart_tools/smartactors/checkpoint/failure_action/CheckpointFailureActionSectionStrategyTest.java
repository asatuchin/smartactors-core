package info.smart_tools.smartactors.checkpoint.failure_action;

import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.helpers.plugins_loading_test_base.PluginsLoadingTestBase;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject_plugins.dsobject_plugin.PluginDSObject;
import info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin.IFieldNamePlugin;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.ioc_plugins.ioc_keys_plugin.PluginIOCKeys;
import info.smart_tools.smartactors.scope_plugins.scope_provider_plugin.PluginScopeProvider;
import info.smart_tools.smartactors.scope_plugins.scoped_ioc_plugin.ScopedIOCPlugin;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for {@link CheckpointFailureActionSectionStrategy}.
 */
public class CheckpointFailureActionSectionStrategyTest extends PluginsLoadingTestBase {
    private IResolveDependencyStrategy actionStrategyMock;
    private IResolveDependencyStrategy defaultActionStrategyMock;
    private IAction<IObject> actionMock;

    @Override
    protected void loadPlugins() throws Exception {
        load(ScopedIOCPlugin.class);
        load(PluginScopeProvider.class);
        load(PluginIOCKeys.class);
        load(PluginDSObject.class);
        load(IFieldNamePlugin.class);
    }

    @Override
    protected void registerMocks() throws Exception {
        actionStrategyMock = mock(IResolveDependencyStrategy.class);
        IOC.register(Keys.getOrAdd("that checkpoint failure action"), actionStrategyMock);

        defaultActionStrategyMock = mock(IResolveDependencyStrategy.class);
        IOC.register(Keys.getOrAdd("default configurable checkpoint failure action"), defaultActionStrategyMock);

        actionMock = mock(IAction.class);
    }

    @Test
    public void Should_returnSectionName()
            throws Exception {
        assertEquals(
                IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "checkpoint_failure_action"),
                new CheckpointFailureActionSectionStrategy().getSectionName()
        );
    }

    @Test
    public void Should_resolveAndRegisterCustomAction()
            throws Exception {
        IObject config = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.iobject.IObject"),
                "{'checkpoint_failure_action':{'action':'that checkpoint failure action'}}".replace('\'','"'));
        when(actionStrategyMock.resolve(
                same(config.getValue(IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "checkpoint_failure_action")))
        )).thenReturn(actionMock);

        new CheckpointFailureActionSectionStrategy().onLoadConfig(config);

        assertSame(actionMock, IOC.resolve(Keys.getOrAdd("checkpoint failure action")));
    }

    @Test
    public void Should_resolveAndRegisterDefaultActionIfActionDependencyNameWasNotGiven()
            throws Exception {
        IObject config = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.iobject.IObject"),
                "{'checkpoint_failure_action':{}}".replace('\'','"'));
        when(defaultActionStrategyMock.resolve(
                same(config.getValue(IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "checkpoint_failure_action")))
        )).thenReturn(actionMock);

        new CheckpointFailureActionSectionStrategy().onLoadConfig(config);

        assertSame(actionMock, IOC.resolve(Keys.getOrAdd("checkpoint failure action")));
    }
}
