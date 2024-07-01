//
// Created by MOMO on 2024/6/13.
//

#ifndef MEDIAVIDEO_IDECODER_H
#define MEDIAVIDEO_IDECODER_H


class IDecoder {

public:
    virtual void start() = 0;
    virtual void stop() = 0;
    virtual void pause() = 0;
    virtual void isPlaying() = 0;
};


#endif //MEDIAVIDEO_IDECODER_H
