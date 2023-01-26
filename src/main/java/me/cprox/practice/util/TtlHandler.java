package me.cprox.practice.util;

public interface TtlHandler<E> {

	void onExpire(E element);

	long getTimestamp(E element);

}
