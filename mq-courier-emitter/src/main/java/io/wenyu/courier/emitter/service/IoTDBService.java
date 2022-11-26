package io.wenyu.courier.emitter.service;

import com.alibaba.fastjson.JSONObject;
import io.wenyu.courier.emitter.config.IoTDBConfig;
import io.wenyu.courier.emitter.model.Message;
import io.wenyu.courier.emitter.model.RecordSet;
import org.apache.iotdb.session.pool.SessionPool;
import org.apache.iotdb.tsfile.file.metadata.enums.TSDataType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class IoTDBService {

    @Resource
    IoTDBConfig ioTDBConfig;

    private SessionPool sessionPool;

    private SessionPool getSessionPool() {
        if (sessionPool == null) {
            sessionPool = new SessionPool.Builder()
                    .nodeUrls(ioTDBConfig.getNodeUrl())
                    .user(ioTDBConfig.getUsername())
                    .password(ioTDBConfig.getPassword())
                    .maxSize(16)
                    .build();
        }
        return sessionPool;
    }

    public boolean insert(List<Message> messages) {
        RecordSet recordSet = wrapMessages(messages);
        try {
            getSessionPool().insertAlignedRecords(recordSet.getDeviceIds(), recordSet.getTimes(),
                    recordSet.getMeasurementsList(), recordSet.getTypesList(), recordSet.getValuesList());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private RecordSet wrapMessages(List<Message> messages) {
        List<String> deviceIds = new ArrayList<>();
        List<Long> times = new ArrayList<>();
        List<List<String>> measurementsList = new ArrayList<>();
        List<List<TSDataType>> typesList = new ArrayList<>();
        List<List<Object>> valuesList = new ArrayList<>();
        for (Message msg : messages) {
            JSONObject json = parseJSONObject(msg.getValue());
            msg.setJsonValue(json);
            deviceIds.add(determineDeviceId(msg));
            times.add(parseTime(msg));
            parseTimeSeries(msg, measurementsList, typesList, valuesList);
        }
        return new RecordSet(deviceIds, times, measurementsList, typesList, valuesList);
    }

    private void parseTimeSeries(Message message, List<List<String>> measurementsList,
                                 List<List<TSDataType>> typesList, List<List<Object>> valuesList) {
        List<String> measurements = new ArrayList<>();
        List<TSDataType> types = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        for (String key : message.getJsonValue().keySet()) {
            if (key.equals(ioTDBConfig.getTimeField())) {
                continue;
            }
            measurements.add(key);
            Object obj = message.getJsonValue().get(key);
            if (obj instanceof String) {
                types.add(TSDataType.TEXT);
                values.add(obj);
            } else if (obj instanceof BigDecimal) {
                types.add(TSDataType.DOUBLE);
                values.add(((BigDecimal) obj).doubleValue());
            } else if (obj instanceof Double) {
                types.add(TSDataType.DOUBLE);
                values.add(obj);
            } else if (obj instanceof Float) {
                types.add(TSDataType.FLOAT);
                values.add(obj);
            } else if (obj instanceof Boolean) {
                types.add(TSDataType.BOOLEAN);
                values.add(obj);
            } else if (obj instanceof Integer) {
                types.add(TSDataType.INT32);
                values.add(obj);
            } else if (obj instanceof Long) {
                types.add(TSDataType.INT64);
                values.add(obj);
            } else {
                types.add(TSDataType.TEXT);
                values.add(obj);
            }
        }
        if (!measurements.isEmpty()) {
            measurementsList.add(measurements);
        }
        if (!types.isEmpty()) {
            typesList.add(types);
        }
        if (!values.isEmpty()) {
            valuesList.add(values);
        }
    }

    private long parseTime(Message message) {
        SimpleDateFormat format = new SimpleDateFormat(ioTDBConfig.getTimeParse());
        format.setTimeZone(TimeZone.getTimeZone(ioTDBConfig.getTimeZone()));
        try {
            Date time = format.parse(message.getJsonValue().getString(ioTDBConfig.getTimeField()));
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(ioTDBConfig.getTimeZone()));
            cal.setTime(time);
            if (cal.get(Calendar.YEAR) == 1970) {
                cal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
            }
            if ("ns".equals(ioTDBConfig.getTimeUnit())) {
                return cal.getTime().getTime() * (long)1e6;
            } else if ("ms".equals(ioTDBConfig.getTimeUnit())) {
                return cal.getTime().getTime();
            }
            return cal.getTime().getTime();
        } catch (ParseException e) {
            return 0L;
        }
    }

    private JSONObject parseJSONObject(String source) {
        int start = source.indexOf('{');
        if (start > 0) {
            int end = source.lastIndexOf('}');
            source = source.substring(start, end + 1);
        }
        source = source.replace("\\\"", "\"");
        return JSONObject.parseObject(source);
    }

    private String determineDeviceId(Message message) {
        String deviceId = ioTDBConfig.getBasePath();
        if(isEmpty(ioTDBConfig.getOverwriteDeviceId())) {
            if (isEmpty(message.getTag())) {
                if(!isEmpty(message.getKey())) {
                    int idx = message.getKey().indexOf(':');
                    if (idx > 0) {
                        deviceId = String.format("%s.%s", deviceId,
                                message.getKey().substring(0, idx));
                    } else {
                        deviceId = String.format("%s.%s", deviceId, message.getKey());
                    }
                }
            } else {
                deviceId = String.format("%s.%s", deviceId, message.getTag());
            }
        } else if (null != message.getJsonValue()) {
            deviceId = String.format("%s.%s", deviceId, ioTDBConfig.getOverwriteDeviceId());
            Pattern p = Pattern.compile("\\#\\{([^}]+)}");
            Matcher matcher = p.matcher(ioTDBConfig.getOverwriteDeviceId());
            while(matcher.find()) {
                deviceId = deviceId.replace(matcher.group(), message.getJsonValue().getString(matcher.group(1)));
            }
        }
        return deviceId;
    }

    private boolean isEmpty(String string) {
        return null == string || "".equals(string);
    }
}
