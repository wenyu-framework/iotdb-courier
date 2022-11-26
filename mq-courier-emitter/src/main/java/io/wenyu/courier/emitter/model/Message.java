package io.wenyu.courier.emitter.model;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    private int partition;
    private long offset;
    private String key;
    private String tag;
    private String value;
    private JSONObject jsonValue;
}
