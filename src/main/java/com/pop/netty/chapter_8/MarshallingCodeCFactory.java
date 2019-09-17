package com.pop.netty.chapter_8;

import io.netty.handler.codec.marshalling.*;
import org.jboss.marshalling.*;

import java.io.IOException;

/**
 * @author Pop
 * @date 2019/9/11 23:50
 */
public final class MarshallingCodeCFactory {

    public static Marshaller buildMarshalling() throws IOException {
        final MarshallerFactory marshallerFactory =
                Marshalling.getProvidedMarshallerFactory("serial");
        final MarshallingConfiguration configuration
                = new MarshallingConfiguration();
        configuration.setVersion(5);
        Marshaller marshaller = marshallerFactory.createMarshaller(configuration);
        return marshaller;
    }

    public static Unmarshaller buildUnMarshalling() throws IOException {
        final MarshallerFactory marshallerFactory =
                Marshalling.getProvidedMarshallerFactory("serial");
        final MarshallingConfiguration configuration
                = new MarshallingConfiguration();
        configuration.setVersion(5);
        final Unmarshaller unmarshaller =
                marshallerFactory.createUnmarshaller(configuration);
        return unmarshaller;
    }

    /**
     * 创建JBoss Marshalling 解码器
     * @return
     */
    public static MarshallingDecoder buildMarshllingDecoder(){

        final MarshallerFactory marshallerFactory =
                Marshalling.getProvidedMarshallerFactory("serial");
        final MarshallingConfiguration configuration =
                new MarshallingConfiguration();
        configuration.setVersion(5);
        UnmarshallerProvider provider = new DefaultUnmarshallerProvider(
                marshallerFactory,configuration
        );



        MarshallingDecoder decoder = new MarshallingDecoder(provider,1024);


        return decoder;
    }

    /**
     * 创建Jboss Marshalling 编码器
     * @return
     */
    public static MarshallingEncoder buildMarshallingEncoder(){
        final MarshallerFactory marshallerFactory = Marshalling
                .getProvidedMarshallerFactory("serial");
        final MarshallingConfiguration configuration
                = new MarshallingConfiguration();
        configuration.setVersion(5);

        MarshallerProvider provider = new DefaultMarshallerProvider(
                marshallerFactory,configuration
        );
        MarshallingEncoder encoder = new MarshallingEncoder(provider);

        return encoder;

    }

}
