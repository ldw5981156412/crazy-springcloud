package com.crazymaker.redission.demo.codec;

import org.redisson.client.codec.BaseCodec;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;

/**
 * @author liudawei
 * @date 2023/9/30
 **/
public class FastjsonCodec extends BaseCodec {
    @Override
    public Decoder<Object> getValueDecoder() {
        return null;
    }

    @Override
    public Encoder getValueEncoder() {
        return null;
    }
}
