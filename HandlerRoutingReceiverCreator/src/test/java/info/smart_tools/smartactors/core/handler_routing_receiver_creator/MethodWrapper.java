package info.smart_tools.smartactors.core.handler_routing_receiver_creator;

public class MethodWrapper implements IMethodWrapper {
    public Boolean methodVisited = false;

    @Override
    public String getString() {
        this.methodVisited = true;
        return null;
    }
}