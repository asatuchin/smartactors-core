package info.smart_tools.smartactors.testing_plugins.test_reporter_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.testing.interfaces.itest_reporter.ITestReporter;
import info.smart_tools.smartactors.testing.test_report_console_printer.TestReportConsolePrinter;
import info.smart_tools.smartactors.testing.test_report_text_builder.TestReportTextBuilder;
import info.smart_tools.smartactors.testing.test_reporter.TestReporter;

public class PluginTestReporter implements IPlugin {

    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    public PluginTestReporter(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            IBootstrapItem<String> reporterItem = new BootstrapItem("test_reporter");
            reporterItem.process(() -> {
                try {
                    IOC.register(Keys.getOrAdd(ITestReporter.class.getCanonicalName()),
                            new ApplyFunctionToArgumentsStrategy(args -> {
                                try {
                                    return new TestReporter((String) args[0], new TestReportConsolePrinter(), new TestReportTextBuilder());
                                } catch (ResolutionException e) {
                                    throw new RuntimeException(e);
                                }
                            }));
                } catch (ResolutionException | InvalidArgumentException | RegistrationException e) {
                    throw new ActionExecuteException(e);
                }
            });
            this.bootstrap.add(reporterItem);
        } catch (InvalidArgumentException e) {
            throw new PluginException(e);
        }
    }
}