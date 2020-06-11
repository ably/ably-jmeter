package io.ably.jmeter;

import java.util.ArrayList;
import java.util.List;

public class SubBean {
	private int receivedMessageSize = 0;
	private int receivedCount = 0;
	private double avgElapsedTime = 0f;

	private List<byte[]> contents = new ArrayList<>();

	public int getReceivedMessageSize() {
		return receivedMessageSize;
	}
	public void setReceivedMessageSize(int receivedMessageSize) {
		this.receivedMessageSize = receivedMessageSize;
	}

	public int getReceivedCount() {
		return receivedCount;
	}
	public void setReceivedCount(int receivedCount) {
		this.receivedCount = receivedCount;
	}

	public double getAvgElapsedTime() {
		return avgElapsedTime;
	}
	public void setAvgElapsedTime(double avgElapsedTime) {
		this.avgElapsedTime = avgElapsedTime;
	}

	public List<byte[]> getContents() {
		return contents;
	}

	public byte[] mergeContents(byte[] delim) {
		if(receivedCount == 0) {
			return new byte[0];
		}

		int delimLength = delim.length;
		int finalLength = receivedMessageSize + (receivedCount - 1) * delimLength;
		byte[] dest = new byte[finalLength];
		int destPos = 0;

		for(byte[] element : contents) {
			System.arraycopy(element, 0, dest, destPos, element.length);
			destPos += element.length;
			if(destPos < finalLength) {
				System.arraycopy(delim, 0, dest, destPos, delimLength);
				destPos += delimLength;
			}
		}
		return dest;
	}
}
