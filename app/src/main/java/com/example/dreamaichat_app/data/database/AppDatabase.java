package com.example.dreamaichat_app.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.dreamaichat_app.data.converter.DateConverter;
import com.example.dreamaichat_app.data.dao.ConversationDao;
import com.example.dreamaichat_app.data.dao.MessageDao;
import com.example.dreamaichat_app.data.dao.UserDao;
import com.example.dreamaichat_app.data.entity.ConversationEntity;
import com.example.dreamaichat_app.data.entity.MessageEntity;
import com.example.dreamaichat_app.data.entity.UserEntity;

/**
 * Room 数据库主类
 * 
 * 这是整个应用的数据库入口，负责：
 * 1. 定义数据库版本和表结构
 * 2. 提供 DAO 访问接口
 * 3. 管理数据库实例（单例模式）
 * 
 * 数据库结构：
 * - users: 用户表
 * - conversations: 会话表
 * - messages: 消息表
 */
@Database(
    entities = {
        UserEntity.class, // 用户表
        ConversationEntity.class, // 会话表
        MessageEntity.class // 消息表
    },
    version = 3,  // 数据库版本号，每次修改表结构时递增
    exportSchema = false  // 不导出数据库架构（开发阶段可以设为 false）
)
@TypeConverters({DateConverter.class})  // 注册类型转换器
public abstract class AppDatabase extends RoomDatabase {
    
    /**
     * 数据库名称
     */
    private static final String DATABASE_NAME = "dreamai_chat.db";
    
    /**
     * 数据库单例实例
     */
    private static AppDatabase instance;
    
    /**
     * 获取数据库实例（单例模式）
     * 
     * 单例模式的好处：
     * 1. 确保整个应用只有一个数据库实例
     * 2. 避免重复创建数据库连接
     * 3. 节省内存和资源
     * 
     * @param context 应用上下文
     * @return 数据库实例
     */
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),  // 使用 Application Context，避免内存泄漏
                    AppDatabase.class,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration()  // 开发阶段：版本升级时删除旧数据（生产环境需要 Migration）
                .allowMainThreadQueries()  // 允许在主线程查询（仅用于开发，生产环境应使用后台线程）
                .build();
        }
        return instance;
    }
    
    /**
     * 获取用户数据访问对象
     * 
     * @return UserDao 实例
     */
    public abstract UserDao userDao();
    
    /**
     * 获取会话数据访问对象
     * 
     * @return ConversationDao 实例
     */
    public abstract ConversationDao conversationDao();
    
    /**
     * 获取消息数据访问对象
     * 
     * @return MessageDao 实例
     */
    public abstract MessageDao messageDao();
    
    /**
     * 关闭数据库连接
     * 通常在应用退出时调用
     */
    public static void closeDatabase() {
        if (instance != null && instance.isOpen()) {
            instance.close();
            instance = null;
        }
    }
}

