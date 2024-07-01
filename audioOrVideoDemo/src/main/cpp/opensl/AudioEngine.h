//
// Created by MOMO on 2024/7/1.
//

#ifndef MEDIAVIDEO_AUDIOENGINE_H
#define MEDIAVIDEO_AUDIOENGINE_H

#include <SLES/OpenSLES.h>
#include <stdio.h>
#include <SLES/OpenSLES_Android.h>
#include <assert.h>
#include <cstring>
#include <android/log.h>


class AudioEngine{
public:
    SLObjectItf engineObj;
    SLEngineItf engine;

    SLObjectItf outPutMixObj;

private:
    void createEngine() {
        // 音频的播放，就涉及到了，OpenLSES
        // TODO 第一大步：创建引擎并获取引擎接口
        // 1.1创建引擎对象：SLObjectItf engineObject

        SLresult result = slCreateEngine(&engineObj, 0, NULL, 0, NULL, NULL);
        if (SL_RESULT_SUCCESS != result) {
            return;
        }

        //1.2 初始化引擎
        result = (*engineObj)->Realize(engineObj, SL_BOOLEAN_FALSE);
        if (SL_BOOLEAN_FALSE != result) {
            return;
        }

        // 1.3 获取引擎接口 SLEngineItf engineInterface
        result = (*engineObj)->GetInterface(engineObj, SL_IID_ENGINE, &engine);
        if (SL_RESULT_SUCCESS != result) {
            return;
        }

        // TODO 第二大步 设置混音器
        // 2.1 创建混音器：SLObjectItf outputMixObject
        result = (*engine)->CreateOutputMix(engine, &outPutMixObj, 0, 0, 0);
        if (SL_RESULT_SUCCESS != result) {
            return;
        }

        // 2.2 初始化 混音器
        result = (*outPutMixObj)->Realize(outPutMixObj, SL_BOOLEAN_FALSE);
        if (SL_BOOLEAN_FALSE != result) {
            return;
        }
    }


    virtual void release() {
        if (outPutMixObj) {
            (*outPutMixObj)->Destroy(outPutMixObj);
        }

        if (engineObj) {
            (*engineObj)->Destroy(engineObj);
            engineObj = NULL;
            engine = NULL;
        }
    }

public:
    AudioEngine() : engineObj(NULL), engine(NULL), outPutMixObj(NULL) {
        createEngine();
    }

    virtual ~AudioEngine() {
        release();
    }
};



#endif //MEDIAVIDEO_AUDIOENGINE_H
