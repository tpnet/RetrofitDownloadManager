package com.tpnet.retrofitdownloaddemo.download.rxbus;


import com.trello.rxlifecycle.LifecycleProvider;
import com.trello.rxlifecycle.android.ActivityEvent;
import com.trello.rxlifecycle.android.FragmentEvent;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * 
 * 
 * Created by litp on 2017/3/24.
 */

public class RxBus {

    //RxBus实例
    private static RxBus rxBus;


    private final Subject<Events<?>, Events<?>> _bus = new SerializedSubject<>(PublishSubject.<Events<?>>create());

    private RxBus() {
    }

    //单例
    public static RxBus getInstance() {
        if (rxBus == null) {
            synchronized (RxBus.class) {
                if (rxBus == null) {
                    rxBus = new RxBus();
                }
            }
        }
        return rxBus;
    }

    //
    public void send(Events<?> o) {
        _bus.onNext(o);
    }


    //
    public void send(String code, Object content) {
        Events<Object> event = new Events<>();
        event.code = code;
        event.content = content;
        send(event);
    }

    public Observable<Events<?>> toObservable() {
        return _bus;
    }


    public static SubscriberBuilder with(LifecycleProvider provider) {
        return new SubscriberBuilder(provider);
    }


    public static class SubscriberBuilder {

        private LifecycleProvider mLifecycleProvider;
        private FragmentEvent mFragmentEndEvent;
        private ActivityEvent mActivityEndEvent;
        private String event;
        private Action1<? super Events<?>> onNext;
        private Action1<Throwable> onError;



        public SubscriberBuilder(LifecycleProvider provider) {
            this.mLifecycleProvider = provider;
        }

        public SubscriberBuilder setEvent(String event) {
            this.event = event;
            return this;
        }

        public SubscriberBuilder setEndEvent(FragmentEvent event) {
            this.mFragmentEndEvent = event;
            return this;
        }

        public SubscriberBuilder setEndEvent(ActivityEvent event) {
            this.mActivityEndEvent = event;
            return this;
        }

        public SubscriberBuilder onNext(Action1<? super Events<?>> action) {
            this.onNext = action;
            return this;
        }

        public SubscriberBuilder onError(Action1<Throwable> action) {
            this.onError = action;
            return this;
        }


        public void create() {
            _create();
        }

        public Subscription _create() {

            if (mLifecycleProvider != null) {
                return RxBus.getInstance().toObservable()
                        .compose(mFragmentEndEvent == null ? mLifecycleProvider.bindToLifecycle() : mLifecycleProvider.<Events<?>>bindUntilEvent(mFragmentEndEvent)) // 绑定生命周期
                        .filter(new Func1<Events<?>, Boolean>() {
                            @Override
                            public Boolean call(Events<?> events) {
                                return events.code.equals(event);
                            }
                        })   //过滤 根据code判断返回事件
                        .subscribe(onNext, onError == null ? new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                throwable.printStackTrace();
                            }
                        } : onError);
            }
            return null;
        }
    }
}
