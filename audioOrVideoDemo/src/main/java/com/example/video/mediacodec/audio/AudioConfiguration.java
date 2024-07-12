package com.example.video.mediacodec.audio;

import android.media.AudioFormat;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

/**
 * author: hongming.wei
 * data: 2024/7/5
 */
public class AudioConfiguration {
    private int minBps;
    private int maxBps;
    private int frequency;
    private int encoding;
    private CodeType codeType;
    private int channelCount;
    private int adts;
    private int aacProfile;
    private String mime;
    private boolean aec;
    private boolean mediaCodec;


    public static int DEFAULT_FREQUENCY = 44100;
    public static int DEFAULT_MAX_BPS = 64;
    public static int DEFAULT_MIN_BPS = 32;
    public static int DEFAULT_ADTS = 0;
    public static CodeType DEFAULT_CODE_TYPE = CodeType.ENCODE;
    public static String DEFAULT_MIME = MediaFormat.MIMETYPE_AUDIO_AAC;
    public static int DEFAULT_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    public static int DEFAULT_AAC_PROFILE = MediaCodecInfo.CodecProfileLevel.AACObjectLC;
    public static int DEFAULT_CHANNEL_COUNT = 1;
    public static boolean DEFAULT_AEC = false;

    public static boolean DEFAULT_MEDIA_CODEC = true;

    public static AudioConfiguration createDefault() {
        return new Builder().build();
    }

    private AudioConfiguration(Builder builder) {
        minBps = builder.minBps;
        maxBps = builder.maxBps;
        frequency = builder.frequency;
        encoding = builder.encoding;
        codeType = builder.codeType;
        channelCount = builder.channelCount;
        adts = builder.adts;
        mime = builder.mime;
        aacProfile = builder.aacProfile;
        aec = builder.aec;
        mediaCodec = builder.mediaCodec;
    }


    private static class Builder {
        public boolean mediaCodec = DEFAULT_MEDIA_CODEC;
        public int minBps = DEFAULT_MIN_BPS;
        public int maxBps = DEFAULT_MAX_BPS;
        public int frequency = DEFAULT_FREQUENCY;
        public int encoding = DEFAULT_AUDIO_ENCODING;
        public int channelCount = DEFAULT_CHANNEL_COUNT;
        public int adts = DEFAULT_ADTS;
        public String mime = DEFAULT_MIME;
        public CodeType codeType = DEFAULT_CODE_TYPE;
        public int aacProfile = DEFAULT_AAC_PROFILE;
        public boolean aec = DEFAULT_AEC;


        public Builder setBps(int minBps, int maxBps) {
            this.minBps = minBps;
            this.maxBps = maxBps;
            return this;
        }

        public Builder setFrequency(int frequency) {
            this.frequency = frequency;
            return this;
        }

        public Builder setEncoding(int encoding) {
            this.encoding = encoding;
            return this;
        }

        public Builder setChannelCount(int channelCount) {
            this.channelCount = channelCount;
            return this;
        }

        public Builder setAdts(int adts) {
            this.adts = adts;
            return this;
        }

        public Builder setAacProfile(int aacProfile) {
            this.aacProfile = aacProfile;
            return this;
        }

        public Builder setMime(String mime) {
            this.mime = mime;
            return this;
        }

        public Builder setAec(boolean aec) {
            this.aec = aec;
            return this;
        }

        public Builder setMediaCodec(boolean mediaCodec) {
            this.mediaCodec = mediaCodec;
            return this;
        }

        public Builder setCodecType(CodeType codeType) {
            this.codeType = codeType;
            return this;
        }

        public AudioConfiguration build() {
            return new AudioConfiguration(this);
        }
    }


    public enum CodeType {
        ENCODE(1),
        DECODE(2);

        CodeType(int codeType) {
        }
    }


    public int getMinBps() {
        return minBps;
    }

    public void setMinBps(int minBps) {
        this.minBps = minBps;
    }

    public int getMaxBps() {
        return maxBps;
    }

    public void setMaxBps(int maxBps) {
        this.maxBps = maxBps;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getEncoding() {
        return encoding;
    }

    public void setEncoding(int encoding) {
        this.encoding = encoding;
    }

    public CodeType getCodeType() {
        return codeType;
    }

    public void setCodeType(CodeType codeType) {
        this.codeType = codeType;
    }


    public int getChannelCount() {
        return channelCount;
    }

    public void setChannelCount(int channelCount) {
        this.channelCount = channelCount;
    }

    public int getAdts() {
        return adts;
    }

    public void setAdts(int adts) {
        this.adts = adts;
    }

    public int getAacProfile() {
        return aacProfile;
    }

    public void setAacProfile(int aacProfile) {
        this.aacProfile = aacProfile;
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public boolean getAec() {
        return aec;
    }

    public void setAec(boolean aec) {
        this.aec = aec;
    }

    public boolean getMediaCodec() {
        return mediaCodec;
    }

    public void setMediaCodec(boolean mediaCodec) {
        this.mediaCodec = mediaCodec;
    }
}
