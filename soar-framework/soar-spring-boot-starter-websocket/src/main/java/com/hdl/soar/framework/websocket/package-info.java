/**
 * WebSocket support for Soar, with pluggable multi-node message fan-out.
 *
 * <p>A single node uses the {@code local} sender. Multiple nodes use a broker-backed
 * sender (Redis provided; Rabbit/Kafka may be added) so a message produced on any
 * node reaches whichever node holds the target session.
 */
package com.hdl.soar.framework.websocket;