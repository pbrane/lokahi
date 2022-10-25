package org.opennms.horizon.shared.protobuf.marshalling;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;
import com.google.protobuf.util.JsonFormat;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProtoBufJsonDeserializer<T extends Message> extends JsonDeserializer {

    private final Class<T> clazz;

    @Override
    public Class<?> handledType() {
        return clazz;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public T deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException, JacksonException {

        try {
            JsonNode jsonNode = jsonParser.getCodec().readTree(jsonParser);

            Method newBuilderMethod = clazz.getMethod("newBuilder");
            Builder builder = (Builder) newBuilderMethod.invoke(null);

            JsonFormat.parser().merge(jsonNode.toString(), builder);

            return (T) builder.build();

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("failed to deserialize protobuf message: type-name=" + clazz.getTypeName(), e);
        }
    }
}
