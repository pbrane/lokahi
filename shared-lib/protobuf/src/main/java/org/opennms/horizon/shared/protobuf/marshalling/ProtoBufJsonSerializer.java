package org.opennms.horizon.shared.protobuf.marshalling;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import java.io.IOException;

public class ProtoBufJsonSerializer<T extends Message> extends JsonSerializer<T> {
    Class clazz;

    public ProtoBufJsonSerializer(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Class<T> handledType() {
        return clazz;
    }

    @Override
    public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeRaw(JsonFormat.printer().print(value));
    }
}
