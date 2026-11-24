package com.swak.test.license.io;

import com.swak.license.api.io.Codec;
import com.swak.license.api.io.Source;
import com.swak.license.api.io.Store;
import com.swak.license.api.io.bios.BIOS;
import com.swak.license.api.io.json.Jackson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 编解码器（serialization / XML / JSON）与流复制回归。
 */
@DisplayName("BIOS 编解码与复制")
class BiosCodecTest {

    private static final byte[] PAYLOAD = "swak-license-payload".getBytes(StandardCharsets.UTF_8);

    @Test
    @DisplayName("序列化编解码：对象往返一致，Codec.clone 得到深拷贝")
    void serializationCodecRoundTrip() throws Exception {
        Codec codec = BIOS.serialization();
        Store store = BIOS.memory();
        SampleBean bean = new SampleBean("swak", 3);

        codec.connect(store).encode(bean);
        SampleBean decoded = codec.connect(store).decode(SampleBean.class);

        assertThat(decoded).isEqualTo(bean);
        assertThat(codec.connect(store).clone(bean)).isEqualTo(bean);
    }

    @Test
    @DisplayName("BIOS.clone：默认与指定缓冲区大小都返回等值副本")
    void cloneObjects() throws Exception {
        SampleBean bean = new SampleBean("clone", 7);

        assertThat(BIOS.clone(bean)).isEqualTo(bean);
        assertThat(BIOS.clone(bean, 64)).isEqualTo(bean);
        assertThat(BIOS.clone("plain-string")).isEqualTo("plain-string");
    }

    @Test
    @DisplayName("XML 编解码：JavaBean 往返一致")
    void xmlCodecRoundTrip() throws Exception {
        Codec codec = BIOS.xml();
        Store store = BIOS.memory();
        SampleBean bean = new SampleBean("xml", 11);

        codec.connect(store).encode(bean);
        SampleBean decoded = codec.connect(store).decode(SampleBean.class);

        assertThat(decoded.getName()).isEqualTo("xml");
        assertThat(decoded.getCount()).isEqualTo(11);
        assertThat(BIOS.xml(XMLEncoder::new, XMLDecoder::new)).isNotNull();
    }

    @Test
    @DisplayName("JSON 编解码：Map 往返一致，clone 使用临时存储")
    void jsonCodecRoundTrip() throws Exception {
        Codec codec = Jackson.json();
        Store store = BIOS.memory();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("name", "swak");
        payload.put("count", 5);

        codec.connect(store).encode(payload);

        assertThat(new String(store.content(), StandardCharsets.UTF_8))
                .contains("\"name\":\"swak\"")
                .contains("\"count\":5");

        @SuppressWarnings("unchecked")
        Map<String, Object> decoded = (Map<String, Object>) codec.connect(store).decode(Map.class);
        assertThat(decoded).containsEntry("name", "swak").containsEntry("count", 5);

        assertThat(codec.clone(payload, BIOS::memory)).isEqualTo(payload);
    }

    @Test
    @DisplayName("Store.connect(Codec)：与 Codec.connect(Store) 等价")
    void storeConnectsToCodec() throws Exception {
        Store store = BIOS.memory();

        store.connect(BIOS.serialization()).encode("via-store");

        String decoded = store.connect(BIOS.serialization()).decode(String.class);
        assertThat(decoded).isEqualTo("via-store");
        assertThat(store.connect(BIOS.serialization()).codec()).isNotNull();
        assertThat(store.connect(BIOS.serialization()).store()).isSameAs(store);
    }

    @Test
    @DisplayName("复制：Source 到 Sink、Socket 到 Socket 都逐字节一致")
    void copiesStreams() throws Exception {
        Store target = BIOS.memory();
        Source source = BIOS.stream(new ByteArrayInputStream(PAYLOAD));

        BIOS.copy(source, target);
        assertThat(target.content()).isEqualTo(PAYLOAD);

        Store socketTarget = BIOS.memory();
        BIOS.copy(() -> new ByteArrayInputStream(PAYLOAD), socketTarget.output());
        assertThat(socketTarget.content()).isEqualTo(PAYLOAD);
    }

    @Test
    @DisplayName("复制：超过单个缓冲区的大数据走多轮 FIFO 仍然完整")
    void copiesLargePayload() throws Exception {
        byte[] large = new byte[100_000];
        new Random(7).nextBytes(large);

        Store target = BIOS.memory();
        BIOS.copy(BIOS.stream(new ByteArrayInputStream(large)), target);

        assertThat(target.content()).isEqualTo(large);
    }

    /** XML 编解码要求公共无参构造器与 setter。 */
    public static class SampleBean implements Serializable {

        private static final long serialVersionUID = 1L;

        private String name;
        private int count;

        public SampleBean() {
        }

        SampleBean(String name, int count) {
            this.name = name;
            this.count = count;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof SampleBean)) {
                return false;
            }
            SampleBean that = (SampleBean) o;
            return count == that.count && Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, count);
        }
    }
}
