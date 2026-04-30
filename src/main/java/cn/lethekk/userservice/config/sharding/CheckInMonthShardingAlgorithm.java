package cn.lethekk.userservice.config.sharding;

import com.google.common.collect.Range;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * @Author Lethekk
 * @Date 2026/4/30 20:44
 */
public class CheckInMonthShardingAlgorithm implements StandardShardingAlgorithm<LocalDate> {

    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<LocalDate> shardingValue) {
        LocalDate date = shardingValue.getValue();
        String suf = date.format(DateTimeFormatter.ofPattern("yyyyMM"));
        String target = "check_in_log_" + suf;
        if (availableTargetNames.contains(target)) {
            return target;
        }
        throw new IllegalArgumentException("表不存在: " + target);
    }

    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, RangeShardingValue<LocalDate> shardingValue) {
        // 跨月查询走这里，返回范围内所有月表
        Range<LocalDate> range = shardingValue.getValueRange();
        LocalDate start = range.lowerEndpoint();
        LocalDate end = range.upperEndpoint();

        List<String> result = new ArrayList<>();
        YearMonth cur = YearMonth.from(start);
        YearMonth endYM = YearMonth.from(end);
        while (!cur.isAfter(endYM)) {
            String target = "check_in_log_" + cur.format(DateTimeFormatter.ofPattern("yyyyMM"));
            if (availableTargetNames.contains(target)) {
                result.add(target);
            }
            cur = cur.plusMonths(1);
        }
        return result;
    }

    //    @Override
    public Properties getProps() {
        return new Properties();
    }

    @Override
    public void init(Properties props) {
    }
}
