package JSocket.Utility;

/**
 * This enum is used to identify the type of frame being sent
 */
public enum OPCode {
    CONTINUATION_FRAME,
    TEXT_FRAME,
    BINARY_FRAME,
    NON_CONTROL_FRAME,
    CONNECTION_CLOSE_FRAME,
    PING,
    PONG,
    FURTHER_CONTROL_FRAME
}
