/*
 * Copyright (C), 2018-2018, 深圳点积科技有限公司
 * FileName: IdGenerator
 * Author:   yugo
 * Date:   2018/4/27 20:04
 * Since: 1.0.0
 */
package com.rookie.opcua.utils;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Enumeration;


/**
 * id生成器
 *
 * @author yugo
 * @since 1.0.0
 * 2018/4/27
 */
@Slf4j
@SuppressWarnings("all")
public enum IdGenerator {
    INSTANCE;
    /**
     * 开始时间截 (2017-01-01)
     */
    private final long twepoch = 1483200000000L;

    private static final int LOW_ORDER_THREE_BYTES = 0x00ffffff;

    /**
     * 机器ID所占的位数
     */
    private final long workerIdBits = 3L;

    /**
     * 进程标识ID所占的位数
     */
    private final long processIdBits = 5L;

    /**
     * 序列在ID中占的位数
     */
    private final long sequenceBits = 5L;

    /**
     * 机器ID向左移11位 (5+5)
     */
    private final long workerIdShift = sequenceBits + processIdBits;

    /**
     * 进程标识ID向左移5位
     */
    private final long processIdShift = sequenceBits;

    /**
     * 时间截向左移14位(3+5+5)
     */
    private final long timestampLeftShift = sequenceBits + workerIdBits + processIdBits;

    /**
     * 生成序列的掩码，这里为32 (0b111111111111=0xfff=32)
     */
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);

    /**
     * 工作机器ID(0~7)
     */
    private static final long WORKER_ID;

    /**
     * 线程ID(0~31)
     */
    private static final long PROCESS_ID;

    /**
     * 毫秒内序列(0~255)
     */
    private long sequence = 0L;

    /**
     * 上次生成ID的时间截
     */
    private long lastTimestamp = -1L;

    static {
        try {
            /*
             * 这种计算方式是粗糙的，最好是能根据idc编号和机器编号 代替目前的IP和进程号方案取模运算，但是设定idc编号和机器号需要运维支持
             * 目前为了完全不依赖运维，所以采用网卡和进程的方案，还有一点，此方案强依赖系统时间，所以系统时间需一致
             */
            WORKER_ID = createIpIdentifier() % 8;
            PROCESS_ID = createProcessIdentifier() % 32;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static IdGenerator getInstance() {
        return INSTANCE;
    }

    /**
     * 计算线程号
     *
     * @return long
     * @author yugo
     * 2018/4/28 09:55
     * @since 1.0.0
     */
    private static long createProcessIdentifier() {
        long processId;
        try {
            String processName = java.lang.management.ManagementFactory.getRuntimeMXBean()
                    .getName();
            if (processName.contains("@")) {
                processId = Long.parseLong(processName.substring(0, processName.indexOf('@')));
            } else {
                processId = java.lang.management.ManagementFactory.getRuntimeMXBean().getName()
                        .hashCode();
            }

        } catch (Throwable t) {
            processId = new SecureRandom().nextLong();
            log.warn("Failed to get process identifier from JMX, using random number instead", t);
        }

        return processId;
    }

    /**
     * 计算机网卡
     *
     * @return long
     * @author yugo
     * 2018/4/28 09:56
     * @since 1.0.0
     */
    @Deprecated
    private static long createMachineIdentifier() {
        long machinePiece;
        try {
            StringBuilder sb = new StringBuilder();
            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface ni = e.nextElement();
                sb.append(ni.toString());
                byte[] mac = ni.getHardwareAddress();
                if (mac != null) {
                    ByteBuffer bb = ByteBuffer.wrap(mac);
                    try {
                        sb.append(bb.getChar());
                        sb.append(bb.getChar());
                        sb.append(bb.getChar());
                    } catch (BufferUnderflowException shortHardwareAddressException) { // NOPMD
                        // mac with less than 6 bytes. continue
                    }
                }
            }

            machinePiece = sb.toString().hashCode();
        } catch (Throwable t) {
            // exception sometimes happens with IBM JVM, use random
            machinePiece = (new SecureRandom().nextLong());
            log.warn(
                    "Failed to get machine identifier from network interface,using random number instead ",
                    t);
        }
        machinePiece = machinePiece & LOW_ORDER_THREE_BYTES;
        return machinePiece;
    }

    /**
     * 计算机器ip
     *
     * @return long
     * @author yugo
     * 2018/4/28 09:56
     * @since 1.0.0
     */
    private static long createIpIdentifier() {
        long result = 0;
        try {
            InetAddress address = InetAddress.getLocalHost();
            String hostAddress = address.getHostAddress();

            String[] ipAddressInArray = hostAddress.split("\\.");

            for (int i = 3; i >= 0; i--) {
                long ip = Long.parseLong(ipAddressInArray[3 - i]);
                result |= ip << (i * 8);
            }
        } catch (Throwable t) {
            result = (new SecureRandom().nextLong());
            log.warn(
                    "Failed to get machine identifier from network interface, using random number instead",
                    t);

        }

        return result;
    }

    /**
     * 获得下一个ID
     *
     * @return long
     * @author yugo
     * 2018/4/28 09:56
     * @since 1.0.0
     */
    public synchronized Long nextId() {
        long timestamp = timeGen();

        // 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format(
                    "Clock moved backwards.  Refusing to generate id for %d milliseconds",
                    lastTimestamp - timestamp));
        }

        // 如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            // 毫秒内序列溢出
            if (sequence == 0) {
                // 阻塞到下一个毫秒,获得新的时间戳
                timestamp = tilNextMillis(lastTimestamp);
            }
        }
        // 时间戳改变，毫秒内序列重置
        else {
            sequence = 0L;
        }

        // 上次生成ID的时间截
        lastTimestamp = timestamp;

        // 移位并通过或运算拼到一起组成64位的ID
        Long id = ((timestamp - twepoch) << timestampLeftShift)
                | (WORKER_ID << workerIdShift)
                | (PROCESS_ID << processIdShift)
                | sequence;
        if (id < 0) {
            throw new RuntimeException(
                    String.format("Id generate error for %d milliseconds", lastTimestamp - timestamp));
        }
        return id;

    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     *
     * @param lastTimestamp 上次的时间戳
     * @return long
     * @author yugo
     * 2018/4/28 09:57
     * @since 1.0.0
     */
    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 返回以毫秒为单位的当前时间
     *
     * @return long
     * @author yugo
     * 2018/4/28 09:57
     * @since 1.0.0
     */
    protected long timeGen() {
        return System.currentTimeMillis();
    }
}
