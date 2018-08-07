package info.smart_tools.smartactors.testing.test_checkers;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.i_addition_dependency_strategy.IAdditionDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.i_addition_dependency_strategy.exception.AdditionDependencyStrategyException;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.configuration_manager.interfaces.iconfiguration_manager.IConfigurationManager;
import info.smart_tools.smartactors.configuration_manager_plugins.configuration_manager_plugin.PluginConfigurationManager;
import info.smart_tools.smartactors.feature_loading_system.bootstrap.Bootstrap;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_plugin.BootstrapPlugin;
import info.smart_tools.smartactors.iobject.field_name.FieldName;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.base.exception.initialization_exception.InitializationException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.iobject_extension.configuration_object.ConfigurationObject;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.iobject_extension_plugins.configuration_object_plugin.InitializeConfigurationObjectStrategies;
import info.smart_tools.smartactors.iobject_plugins.dsobject_plugin.PluginDSObject;
import info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin.IFieldNamePlugin;
import info.smart_tools.smartactors.ioc_plugins.ioc_keys_plugin.PluginIOCKeys;
import info.smart_tools.smartactors.message_processing_plugins.immutable_receiver_chain_plugin.PluginReceiverChain;
import info.smart_tools.smartactors.message_processing_plugins.map_router_plugin.PluginMapRouter;
import info.smart_tools.smartactors.message_processing_plugins.object_creation_strategies_plugin.ObjectCreationStrategiesPlugin;
import info.smart_tools.smartactors.message_processing_plugins.receiver_chains_storage_plugin.PluginReceiverChainsStorage;
import info.smart_tools.smartactors.message_processing_plugins.receiver_generator_plugin.InitializeReceiverGenerator;
import info.smart_tools.smartactors.scope_plugins.scope_provider_plugin.PluginScopeProvider;
import info.smart_tools.smartactors.scope_plugins.scoped_ioc_plugin.ScopedIOCPlugin;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.shutdown_plugins.root_up_counter_plugin.RootUpCounterPlugin;
import info.smart_tools.smartactors.task_plugins.non_blocking_queue.non_blocking_queue_plugin.PluginNonlockingQueue;
import info.smart_tools.smartactors.testing.interfaces.iassertion.IAssertion;
import info.smart_tools.smartactors.testing.interfaces.iassertion.exception.AssertionFailureException;
import info.smart_tools.smartactors.helpers.plugins_loading_test_base.PluginsLoadingTestBase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test for {@link AssertionChecker}.
 */
public class AssertionCheckerTest extends PluginsLoadingTestBase {
    private IAssertion assertion1Mock;
    private IAssertion assertion2Mock;
    private IMessageProcessor messageProcessorMock;
    private IObject environmentMock;

    @Override
    protected void loadPlugins() throws Exception {
        load(ScopedIOCPlugin.class);
        load(PluginScopeProvider.class);
        load(PluginIOCKeys.class);
        load(PluginDSObject.class);
        load(IFieldNamePlugin.class);
        load(InitializeConfigurationObjectStrategies.class);
    }

    @Override
    protected void registerMocks()
            throws Exception {
        assertion1Mock = Mockito.mock(IAssertion.class);
        assertion2Mock = Mockito.mock(IAssertion.class);
        messageProcessorMock = Mockito.mock(IMessageProcessor.class);
        environmentMock = Mockito.mock(IObject.class);

        Mockito.when(messageProcessorMock.getEnvironment()).thenReturn(environmentMock);

        IOC.register(Keys.getOrAdd("assertion of type atype1"), new SingletonStrategy(assertion1Mock));
        IOC.register(Keys.getOrAdd("assertion of type atype2"), new SingletonStrategy(assertion2Mock));
    }

    @Test(expected = InitializationException.class)
    public void Should_constructorThrowWhenCannotResolveAssertionDependency()
            throws Exception {
        IObject a1desc = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.iobject.IObject"),
                "{'type': 'nonexist', 'name': 'Nope'}".replace('\'', '"'));

        new AssertionChecker(Collections.singletonList(a1desc));
    }

    @Test(expected = InitializationException.class)
    public void Should_constructorThrowWhenCannotResolveFieldNameDependency()
            throws Exception {
        IObject a1desc = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.iobject.IObject"),
                "{'type': 'atype1', 'name': 'Nope'}".replace('\'', '"'));

        IOC.remove(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"));

        new AssertionChecker(Collections.singletonList(a1desc));
    }

    @Test(expected = InitializationException.class)
    public void Should_constructorThrowWhenCannotResolveIObjectDependency()
            throws Exception {
        IObject a1desc = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.iobject.IObject"),
                "{'type': 'atype1', 'name': 'Nope'}".replace('\'', '"'));

        IResolveDependencyStrategy strategy = new ApplyFunctionToArgumentsStrategy(
                (a) -> {
                    try {
                        String name = (String) a[0];
                        if (name.equals("type")) {
                            throw new Exception();
                        }
                        return new FieldName(name);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        );
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), "info.smart_tools.smartactors.iobject.ifield_name.IFieldName"),
                strategy
        );

        new AssertionChecker(Collections.singletonList(a1desc));
    }

    @Test
    public void Should_checkAssertions()
            throws Exception {
        IObject a1desc = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.iobject.IObject"),
                "{'type': 'atype1', 'name': 'Ass1'}".replace('\'', '"'));

        AssertionChecker checker = new AssertionChecker(Collections.singletonList(a1desc));

        checker.check(messageProcessorMock, null);
    }

    @Test(expected = AssertionFailureException.class)
    public void Should_throwWhenCannotReadValueFromEnvironment()
            throws Exception {
        IObject a1desc = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.iobject.IObject"),
                "{'type': 'atype1', 'name': 'Ass1'}".replace('\'', '"'));

        Mockito.when(environmentMock.getValue(Matchers.any())).thenThrow(ReadValueException.class);

        AssertionChecker checker = new AssertionChecker(Collections.singletonList(a1desc));

        checker.check(messageProcessorMock, null);
    }

    @Test(expected = AssertionFailureException.class)
    public void Should_throwWhenAssertionFails()
            throws Exception {
        IObject a1desc = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.iobject.IObject"),
                "{'type': 'atype1', 'name': 'Ass1'}".replace('\'', '"'));

        Mockito.doThrow(AssertionFailureException.class).when(assertion1Mock).check(Matchers.same(a1desc), Matchers.any());

        AssertionChecker checker = new AssertionChecker(Collections.singletonList(a1desc));

        checker.check(messageProcessorMock, null);
    }

    @Test(expected = AssertionFailureException.class)
    public void Should_throwWhenExceptionOccurs()
            throws Exception {
        IObject a1desc = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.iobject.IObject"),
                "{'type': 'atype1', 'name': 'Ass1'}".replace('\'', '"'));

        Mockito.doThrow(AssertionFailureException.class).when(assertion1Mock).check(Matchers.same(a1desc), Matchers.any());

        AssertionChecker checker = new AssertionChecker(Collections.singletonList(a1desc));

        checker.check(messageProcessorMock, new Exception());
    }

    @Test
    public void Should_createWrapperDescription()
            throws Exception {
        IAdditionDependencyStrategy strategy = IOC.resolve(Keys.getOrAdd("expandable_strategy#resolve key for configuration object"));
        strategy.register("in_", new ApplyFunctionToArgumentsStrategy(
                (a) -> {
                    try {
                        Object obj = a[1];
                        if (obj instanceof String) {
                            IObject innerObject = new ConfigurationObject();
                            innerObject.setValue(new FieldName("name"), "wds_getter_strategy");
                            innerObject.setValue(new FieldName("args"), new ArrayList<String>() {{ add((String) obj); }});
                            return new ArrayList<IObject>() {{ add(innerObject); }};
                        }
                        return obj;
                    } catch (Throwable e) {
                        throw new RuntimeException(
                                "Error in configuration 'wrapper' rule.", e
                        );
                    }
                })
        );

        IObject a1desc = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.iobject.IObject"),
                "{'type': 'atype1', 'name': 'Ass1', 'value': 'message/x'}".replace('\'', '"'));

        Mockito.doThrow(AssertionFailureException.class).when(assertion1Mock).check(Matchers.same(a1desc), Matchers.any());

        AssertionChecker checker = new AssertionChecker(Collections.singletonList(a1desc));

        IObject desc = checker.getSuccessfulReceiverArguments();
        IObject wrapper = (IObject) desc.getValue(new FieldName("wrapper"));
        assertNotNull(wrapper);
        List<IObject> transformationRules = (List<IObject>) wrapper.getValue(new FieldName("in_Ass1"));
        assertNotNull(transformationRules);
        assertEquals(transformationRules.size(), 1);
    }
}
