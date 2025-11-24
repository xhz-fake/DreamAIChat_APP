package com.example.dreamaichat_app.mvi;

import androidx.lifecycle.ViewModel;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

/**
 * MVI 架构的 ViewModel 基类
 * 
 * 提供通用的功能：
 * 1. 管理 RxJava 订阅（避免内存泄漏）
 * 2. 状态管理（使用 BehaviorSubject）
 * 3. Intent 处理
 * 
 * @param <I> Intent 类型
 * @param <S> State 类型
 */
public abstract class BaseViewModel<I extends Intent, S extends State> extends ViewModel {
    
    /**
     * 状态管理
     * BehaviorSubject 会保存最新的状态，新订阅者会立即收到最新状态
     */
    protected final BehaviorSubject<S> stateSubject = BehaviorSubject.create();
    
    /**
     * 订阅管理
     * 用于管理所有 RxJava 订阅，在 ViewModel 销毁时自动取消
     */
    protected final CompositeDisposable disposables = new CompositeDisposable();
    
    /**
     * 处理 Intent
     * 子类需要实现此方法来处理用户的操作
     * 
     * @param intent 用户意图
     */
    public abstract void processIntent(I intent);
    
    /**
     * 获取当前状态
     * 
     * @return 当前状态
     */
    public BehaviorSubject<S> getState() {
        return stateSubject;
    }
    
    /**
     * 更新状态
     * 
     * @param state 新状态
     */
    protected void updateState(S state) {
        stateSubject.onNext(state);
    }
    
    /**
     * ViewModel 销毁时调用
     * 清理所有订阅，避免内存泄漏
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}

