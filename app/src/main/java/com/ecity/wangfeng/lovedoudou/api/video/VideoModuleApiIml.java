package com.ecity.wangfeng.lovedoudou.api.video;


import com.ecity.wangfeng.lovedoudou.common.ApiConstants;
import com.ecity.wangfeng.lovedoudou.common.RequestCallBack;
import com.ecity.wangfeng.lovedoudou.entity.video.VideoChannel;
import com.ecity.wangfeng.lovedoudou.util.RxJavaCustomTransform;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;

/**
 * @version  1.0
 * Created by Administrator on 2016/11/26.
 */

public class VideoModuleApiIml implements VideoModuleApi<List<VideoChannel>> {


    @Override
    public Subscription getVideoChannelList(final RequestCallBack<List<VideoChannel>> callBack) {
        return Observable.create(new Observable.OnSubscribe<List<VideoChannel>>() {

            @Override
            public void call(Subscriber<? super List<VideoChannel>> subscriber) {
                List<VideoChannel> videoChannelList = new ArrayList<>();
                videoChannelList.add(new VideoChannel("热点", ApiConstants.VIDEO_HOT_ID));
                videoChannelList.add(new VideoChannel("娱乐", ApiConstants.VIDEO_ENTERTAINMENT_ID));
                videoChannelList.add(new VideoChannel("搞笑", ApiConstants.VIDEO_FUN_ID));
//                videoChannelList.add(new VideoChannel("精品", ApiConstants.VIDEO_CHOICE_ID));
                subscriber.onNext(videoChannelList);
                subscriber.onCompleted();
            }
        })
                .compose(RxJavaCustomTransform.<List<VideoChannel>>defaultSchedulers())
                .subscribe(new Subscriber<List<VideoChannel>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        callBack.onError(e.getMessage());
                    }

                    @Override
                    public void onNext(List<VideoChannel> videoChannels) {
                        callBack.success(videoChannels);
                    }
                });
    }

}
