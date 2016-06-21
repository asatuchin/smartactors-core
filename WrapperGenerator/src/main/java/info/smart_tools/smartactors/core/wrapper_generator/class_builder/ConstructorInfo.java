package info.smart_tools.smartactors.core.wrapper_generator.class_builder;

import java.util.ArrayList;
import java.util.List;

/**
 * Constructor summary info
 */
public class ConstructorInfo {

    private ClassBuilder builder;
    private Modifiers modifier;
    private String name;
    private List<ConstructorParameterInfo> parameters = new ArrayList<>();
    private List<String> exceptions = new ArrayList<>();
    private String body;

    public ConstructorInfo(final ClassBuilder builder) {
        this.builder = builder;
    }

    public Modifiers getModifier() {
        return modifier;
    }

    public ConstructorInfo setModifier(final Modifiers modifier) {
        this.modifier = modifier;

        return this;
    }

    public String getName() {
        return name;
    }

    public ConstructorInfo setName(final String name) {
        this.name = name;

        return this;
    }

    public List<ConstructorParameterInfo> getParameters() {
        return parameters;
    }

    public ConstructorParameterInfo setParameters() {
        ConstructorParameterInfo parameterInfo = new ConstructorParameterInfo(this);
        this.parameters.add(parameterInfo);

        return parameterInfo;
    }

    public List<String> getExceptions() {
        return exceptions;
    }

    public ConstructorInfo setExceptions(final String exception) {
        this.exceptions.add(exception);

        return this;
    }

    public String getBody() {
        return body;
    }

    public ConstructorInfo setBody(final String body) {
        this.body = body;

        return this;
    }

    public ClassBuilder next() {
        return this.builder;
    }
}
