package io.wenyu.courier.emitter.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.iotdb.tsfile.file.metadata.enums.TSDataType;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecordSet {
    private List<String> deviceIds;
    private List<Long> times;
    private List<List<String>> measurementsList;
    private List<List<TSDataType>> typesList;
    private List<List<Object>> valuesList;
}

