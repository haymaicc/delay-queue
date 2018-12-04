package com.yangwenjie.delayqueue.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 延迟消息队列
 * 
 * @author Yang WenJie
 * @date 2018/1/27 上午11:28
 */
public class DelayQueue {

	private static final Logger logger = LoggerFactory.getLogger(DelayQueue.class);
	public static final String DELAY_BUCKET_KEY_PREFIX = "delayBucket";
	public static final long DELAY_BUCKET_NUM = 10L;

	/**
	 * 获取delayBucket key 分开多个，有利于提高效率
	 * 
	 * @param delayQueueJodId
	 * @return
	 */
	private static String getDelayBucketKey(long delayQueueJodId) {
		return DELAY_BUCKET_KEY_PREFIX + Math.floorMod(delayQueueJodId, DELAY_BUCKET_NUM);
	}

	/**
	 * 添加延迟任务到延迟队列
	 * 
	 * @param delayQueueJob
	 */
	public static void push(DelayQueueJob delayQueueJob) {
		DelayQueueJobPool.addDelayQueueJod(delayQueueJob);
		ScoredSortedItem item = new ScoredSortedItem(delayQueueJob.getId(), delayQueueJob.getDelayTime());
		DelayBucket.addToBucket(getDelayBucketKey(delayQueueJob.getId()), item);
	}

	/**
	 * 获取准备好的延迟任务
	 * 
	 * @param topic
	 * @return
	 */
	public static DelayQueueJob pop(String topic) {
		Long delayQueueJodId = ReadyQueue.pollFormReadyQueue(topic);
		if (delayQueueJodId == null) {
			return null;
		} else {
			DelayQueueJob delayQueueJod = DelayQueueJobPool.getDelayQueueJod(delayQueueJodId);
			if (delayQueueJod == null) {
				return null;
			} else {
				long delayTime = delayQueueJod.getDelayTime();

				// yang: 这里重置了消息的延时时间是为什么? 猜想：为了可靠性，如果ready 消息被取走了，
				// 但是没有被消费会出现丢失，这里预存保证下次延时还能被重试

				// 获取消费超时时间，重新放到延迟任务桶中
				long reDelayTime = System.currentTimeMillis() + delayQueueJod.getTtrTime() * 1000L;
				delayQueueJod.setDelayTime(reDelayTime);
				DelayQueueJobPool.addDelayQueueJod(delayQueueJod);
				ScoredSortedItem item = new ScoredSortedItem(delayQueueJod.getId(), reDelayTime);
				DelayBucket.addToBucket(getDelayBucketKey(delayQueueJod.getId()), item);
				// 返回的时候设置回
				delayQueueJod.setDelayTime(delayTime);
				return delayQueueJod;
			}
		}
	}

	/**
	 * 删除延迟队列任务
	 * 
	 * @param delayQueueJodId
	 */
	public static void delete(long delayQueueJodId) {
		DelayQueueJobPool.deleteDelayQueueJod(delayQueueJodId);
	}

	/**
	 * 完成消息，这个需要每个消息都有个完成的动作，否则消息会一直堆积
	 */
	public static void finish(long delayQueueJodId) {
		DelayQueueJob delayQueueJod = DelayQueueJobPool.getDelayQueueJod(delayQueueJodId);
		if (delayQueueJod == null) {
			return;
		}
		DelayQueueJobPool.deleteDelayQueueJod(delayQueueJodId);
		ScoredSortedItem item = new ScoredSortedItem(delayQueueJod.getId(), delayQueueJod.getDelayTime());
		DelayBucket.deleteFormBucket(getDelayBucketKey(delayQueueJod.getId()), item);
	}

	/**
	 * 查询delay job
	 */
	public static DelayQueueJob get(long delayQueueJodId) {
		return DelayQueueJobPool.getDelayQueueJod(delayQueueJodId);
	}
}
