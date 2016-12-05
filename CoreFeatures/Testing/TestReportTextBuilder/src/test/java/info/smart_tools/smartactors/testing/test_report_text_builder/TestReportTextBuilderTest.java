package info.smart_tools.smartactors.testing.test_report_text_builder;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.field_plugins.ifield_plugin.IFieldPlugin;
import info.smart_tools.smartactors.helpers.plugins_loading_test_base.PluginsLoadingTestBase;
import info.smart_tools.smartactors.iobject.field_name.FieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin.IFieldNamePlugin;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc_plugins.ioc_keys_plugin.PluginIOCKeys;
import info.smart_tools.smartactors.scope_plugins.scope_provider_plugin.PluginScopeProvider;
import info.smart_tools.smartactors.scope_plugins.scoped_ioc_plugin.ScopedIOCPlugin;
import info.smart_tools.smartactors.testing.interfaces.itest_report_builder.ITestReportBuilder;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestReportTextBuilderTest extends PluginsLoadingTestBase {
    private ITestReportBuilder reportBuilder;

    @Before
    public void setup() throws ResolutionException {
        reportBuilder = new TestReportTextBuilder();
    }

    @Override
    protected void loadPlugins() throws Exception {
        load(ScopedIOCPlugin.class);
        load(PluginScopeProvider.class);
        load(PluginIOCKeys.class);
        load(IFieldNamePlugin.class);
        load(IFieldPlugin.class);
    }

    @Test
    public void Should_BuildText() throws Exception {
        final String report = reportBuilder.build(buildTestSuite());
        System.out.println(report);
        assertNotNull(report);
    }

    private IObject buildTestSuite() throws InvalidArgumentException, ReadValueException {
        IObject suite = mock(IObject.class);
        final List<IObject> testCases = buildTestCases();
        when(suite.getValue(new FieldName("featureName"))).thenReturn("TestReportXmlBuilderTest");
        when(suite.getValue(new FieldName("timestamp"))).thenReturn(new Date().getTime());
        when(suite.getValue(new FieldName("time"))).thenReturn(10L);
        when(suite.getValue(new FieldName("tests"))).thenReturn(2);
        when(suite.getValue(new FieldName("failures"))).thenReturn(1);
        when(suite.getValue(new FieldName("testCases"))).thenReturn(testCases);
        return suite;
    }

    private List<IObject> buildTestCases() throws InvalidArgumentException, ReadValueException {
        List<IObject> testCases = new ArrayList<>();

        testCases.add(buildSuccessfulTestCase());
        testCases.add(buildFailedTestCase());

        return testCases;
    }

    private IObject buildSuccessfulTestCase() throws InvalidArgumentException, ReadValueException {
        IObject testCase = mock(IObject.class);
        when(testCase.getValue(new FieldName("name"))).thenReturn("Should_BuildXML");
        when(testCase.getValue(new FieldName("time"))).thenReturn(4L);
        return testCase;
    }

    private IObject buildFailedTestCase() throws InvalidArgumentException, ReadValueException {
        IObject testCase = mock(IObject.class);
        when(testCase.getValue(new FieldName("name"))).thenReturn("Should_Fail");
        when(testCase.getValue(new FieldName("time"))).thenReturn(6L);
        when(testCase.getValue(new FieldName("failure"))).thenReturn(new Exception("It's a failure"));
        return testCase;
    }
}