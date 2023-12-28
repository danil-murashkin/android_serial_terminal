// UartLib.cpp
//
// @author BevisWang
//
// URL https://blog.csdn.net/PD_Wang/article/details/81449768

#include <jni.h>
#include <string>

#include "DevUart.h"

DevUart devUartPort;



extern "C"
JNIEXPORT jint JNICALL
Java_com_danil_1murashkin_serial_1terminal_UARTTTYMT2Operator_openPort( JNIEnv *env, jobject thiz, jstring path, jint baud_rate, jint data_bits, jint stop_bits, jchar parity)
{
    try {
        UartConfig config;
        config = UartConfig();
        config.baudrate = baud_rate;
        config.databits = data_bits;
        config.stopbits = stop_bits;
        config.parity = parity;
        devUartPort = DevUart(env->GetStringUTFChars(path, 0));
        devUartPort.openUart(config);
    } catch (char *exception) {
        LOGE("Open device is error! Message:%s", exception);
        return FALSE;
    }
    return TRUE;
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_danil_1murashkin_serial_1terminal_UARTTTYMT2Operator_readPort(JNIEnv *env, jobject thiz, jint max_size)
{
    BYTE buf[max_size];
    int len;
    len = devUartPort.readData(buf, max_size);
    if(len < 1) return NULL;
    jbyteArray byteArray;
    jbyte *bytes = reinterpret_cast<jbyte *>(buf);
    byteArray = env->NewByteArray(len);
    env->SetByteArrayRegion(byteArray, 0, len, bytes);
    return byteArray;
}

extern "C" JNIEXPORT void JNICALL
Java_com_danil_1murashkin_serial_1terminal_UARTTTYMT2Operator_writePort(JNIEnv *env, jobject thiz, jbyteArray data, jint data_len)
{
    jbyte *array = env->GetByteArrayElements(data, 0);
    BYTE *bytes = reinterpret_cast<BYTE *>(array);
    devUartPort.writeData(bytes, data_len);
}

extern "C" JNIEXPORT void JNICALL
Java_com_danil_1murashkin_serial_1terminal_UARTTTYMT2Operator_closePort(JNIEnv *env, jobject thiz)
{
    devUartPort.closeUart();
    devUartPort = NULL;
}