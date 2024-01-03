// DevUart.h
// Based on BevisWang from 2018/7/17
// URL https://blog.csdn.net/PD_Wang/article/details/81449768

#ifndef __BLOGPROJECT_DEV_UART_H
#define __BLOGPROJECT_DEV_UART_H

#include <sys/ioctl.h>

#include "android/log.h"

typedef unsigned char BYTE;

static const char *DEV_UART_TAG = "UartLib";
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, DEV_UART_TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, DEV_UART_TAG, fmt, ##args)

/** define states. */
#define FALSE  0
#define TRUE   1

/** define device path. */
#define UART_DEVICE_4     "/dev/ttyS4"
#define UART_DEVICE_6     "/dev/ttyS6"

/** Serial port config param. */
struct UartConfig {
    int baudrate;   // read speed
    int databits;   // one of 7,8
    int stopbits;   // one of 1,2
    int parity;     // one of N,E,O,S
};

/** Serial port device UART class. */
class DevUart {
private:
    const char *path;  // device path
    int fd;             // device
    bool isClose;       // is close fd

    /**
     * @brief  Set serial port speed (baudrate).
     * @param  fd     type int device file
     * @param  speed  type int serial port baurate
     * @return  is success
     */
    int setSpeed(int fd, int speed);

    /**
     * @brief   Set data bits,stop bits and parity.
     * @param  fd       type int device file
     * @param  databits type int data bits,one of 7,8
     * @param  stopbits type int stop bits,one of 1,2
     * @param  parity   type int parity,one of N,E,O,S
    */
    int setParity(int fd, int databits, int stopbits, int parity);

public:
    DevUart();

    /**
     * @brief Construction method.
     * @param path serial port device path
     */
    DevUart(const char *path);

    /**
     * @brief Open serial port device.
     * @param config serial port device config param
     * @return is success
     */
    int openUart(UartConfig config);

    /**
     * @brief Read device data.
     * @param data read data
     * @param timeval read time
     * @return data length
     */
    int readData(BYTE *data,int size);

    /**
     * @brief Write serial port data.
     * @param data serial port data
     * @return is success
     */
    int writeData(BYTE *data, int len);

    /** @brief Close serial port device. */
    void closeUart();

    /**
     * @brief Get baudrate by int.
     * @param baudrate int
     * @return baudrate
     */
    speed_t getBaudrate(int baudrate);
};

#endif //__BLOGPROJECT_DEV_UART_H
