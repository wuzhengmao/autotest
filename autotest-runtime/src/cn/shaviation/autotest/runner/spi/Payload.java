package cn.shaviation.autotest.runner.spi;

public class Payload<T> {

	private T data;

	public T get() {
		return data;
	}

	public void set(T data) {
		this.data = data;
	}
}
