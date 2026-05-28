package com.geek.common.transport.event;

import java.io.Serializable;

import com.geek.common.transport.TransportMessage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用传输入站事件。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportInboundEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private TransportMessage message;
}
