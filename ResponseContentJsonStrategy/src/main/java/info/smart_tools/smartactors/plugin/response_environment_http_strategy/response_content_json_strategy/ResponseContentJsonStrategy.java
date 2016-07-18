package info.smart_tools.smartactors.plugin.response_environment_http_strategy.response_content_json_strategy;

import com.google.common.base.Charsets;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.SerializeException;
import info.smart_tools.smartactors.core.iresponse.IResponse;
import info.smart_tools.smartactors.core.iresponse_content_strategy.IResponseContentStrategy;

import java.nio.charset.Charset;

public class ResponseContentJsonStrategy implements IResponseContentStrategy {
    @Override
    public void setContent(final IObject responseObject, IResponse response) throws SerializeException {
        response.setContent(serializeIObjectToJson(responseObject));
    }

    private byte[] serializeIObjectToJson(IObject responseObject) throws SerializeException {
        return ((String)responseObject.serialize()).getBytes(Charset.forName("UTF-8"));
    }
}