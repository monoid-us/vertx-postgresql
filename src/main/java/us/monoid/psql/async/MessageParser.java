package us.monoid.psql.async;

import org.vertx.java.core.buffer.Buffer;

/** Parse messages (which might be longer than the buffer provided from the socket!) from postgres and pack them into a nice buffer */
class MessageParser {
	private static final byte[] BUFFER = null;
	private static final int BUFFER_SIZE = 1024;
	Buffer currentMessage = new Buffer(BUFFER_SIZE);
	int awaiting = 0;

	private Transaction trx;

	enum State {
		ReadCommand, ReadCommandSize, ReadCommandData
	}

	State state = State.ReadCommand;

	public MessageParser(Transaction transaction) {
		this.trx = transaction;
	}

	/** Receive messages and dispatch accordingly */
	protected void parseMessages(Buffer buffer, int start, int len) {
		//System.out.println("Parsing command buffer: " + len);
		if (start >= buffer.length()) {
			return;
		}

		while (start < buffer.length()) {
			switch (state) {
			case ReadCommand: {
				currentMessage.appendByte(buffer.getByte(start));
				state = State.ReadCommandSize;
				awaiting = 4;
				start++;
				len--; // ;parseMessages(buffer, start + 1, len - 1);
				break;
			}
			case ReadCommandSize: {
				int read = readRest(buffer, start, len);
				if (awaiting == 0) { // read all bytes for the command size: if not, wait for the next buffer
					state = State.ReadCommandData;
					int msgLength = currentMessage.getInt(1);
					awaiting = msgLength - 4;
				} else {
					//System.out.println("Missing command size payload:" + awaiting);
				}
				start += read;
				len -= read;// parseMessages(buffer, start + read, len - read);
				break;
			}
			case ReadCommandData: {
				int read = readRest(buffer, start, len);
				if (awaiting == 0) {
					state = State.ReadCommand;
					trx.dispatch(currentMessage);
					currentMessage = new Buffer(BUFFER_SIZE);
				} else { // could not read everything, will have to wait for the next buffer to arrive
					//System.out.println("Missing command data payload:" + awaiting);
				}
				start += read; 
				len -= read;//parseMessages(buffer, start + read, len - read);
				break;
			}
			}
		}
	}

	/** see how much data we can read from the buffer. Number of bytes to read is in awaiting */
	private int readRest(Buffer buffer, int start, int len) {
		if (awaiting == 0)
			return 0;
		int available = Math.min(len, awaiting);
		currentMessage.appendBytes(buffer.getBytes(start, start + available));
		awaiting -= available;
		return available;
	}

	/*
	 * 
	 * for (int i = 0, len = buffer.length(), msgSize = 0; i < len; i += msgSize) { msgSize = buffer.getInt(i + 1) + 1; System.out.println("Msg:" + (char)buffer.getByte(i) + "size:" + msgSize + "buf:" +
	 * buffer.length()); if (i + msgSize > buffer.length()) System.out.println("Message out of bounds by:" + (buffer.length() - i - msgSize));
	 * 
	 * else dispatch(buffer.getBuffer(i, i + msgSize)); // dispatch single message, the 1 is for the message type byte }
	 */

}
