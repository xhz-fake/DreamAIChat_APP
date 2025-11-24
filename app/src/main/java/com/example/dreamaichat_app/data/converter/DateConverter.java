package com.example.dreamaichat_app.data.converter;

import androidx.room.TypeConverter;

import java.util.Date;

/**
 * 日期类型转换器
 * 
 * Room 数据库不支持直接存储 Date 类型
 * 需要将 Date 转换为 Long（时间戳）存储
 * 这个类负责 Date 和 Long 之间的转换
 * 
 * 使用场景：
 * - 如果 Entity 中有 Date 类型的字段，可以使用这个转换器
 * - 当前项目使用 Long（时间戳）存储时间，所以这个转换器暂时不需要
 * - 但保留它以便将来扩展使用
 */
public class DateConverter {
    
    /**
     * 将 Date 转换为 Long（时间戳）
     * 
     * @param date Date 对象
     * @return 时间戳（毫秒）
     */
    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        if (date == null) {
            return null;
        }
        return date.getTime();
    }
    
    /**
     * 将 Long（时间戳）转换为 Date
     * 
     * @param timestamp 时间戳（毫秒）
     * @return Date 对象
     */
    @TypeConverter
    public static Date timestampToDate(Long timestamp) {
        if (timestamp == null) {
            return null;
        }
        return new Date(timestamp);
    }
}

