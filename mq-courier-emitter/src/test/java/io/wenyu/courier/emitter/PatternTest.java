package io.wenyu.courier.emitter;

import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternTest {
    @Test
    public void TestPropPickerPattern() {
        String origin = "#{hostid}##{filename}";
        Pattern p = Pattern.compile("#\\{([^}]+)}");
        Matcher m = p.matcher(origin);
        while(m.find()) {
            System.out.println("m.group() = " + m.group());
            System.out.println("m.group(1) = " + m.group(1));
            System.out.println("m.groupCount() = " + m.groupCount());
        }
    }
}
