package com.yangwenjie.delayqueue.core;

import org.redisson.api.RMap;

import com.yangwenjie.delayqueue.utils.RedissonUtils;

/**
 * 延迟任务池 Map结构<br/>
 * key 为任务的id，value为整个的消息体
 * 
 * @author Yang WenJie
 * @date 2018/1/27 上午1:35
 */
public class DelayQueueJobPool {

	private static final String DELAY_QUEUE_JOB_POOL = "delayQueueJobPool";

	/**
	 * 查询 DelayQueueJod
	 * 
	 * @param delayQueueJodId
	 * @return
	 */
	public static DelayQueueJob getDelayQueueJod(long delayQueueJodId) {
		RMap<Long, DelayQueueJob> rMap = RedissonUtils.getMap(DELAY_QUEUE_JOB_POOL);
		return rMap.get(delayQueueJodId);
	}

	/**
	 * 添加 DelayQueueJod
	 * 
	 * @param delayQueueJob
	 */
	public static void addDelayQueueJod(DelayQueueJob delayQueueJob) {
		RMap<Long, DelayQueueJob> rMap = RedissonUtils.getMap(DELAY_QUEUE_JOB_POOL);
		rMap.put(delayQueueJob.getId(), delayQueueJob);
	}

	/**
	 * 删除 DelayQueueJod
	 * 
	 * @param delayQueueJodId
	 */
	public static void deleteDelayQueueJod(long delayQueueJodId) {
		RMap<Long, DelayQueueJob> rMap = RedissonUtils.getMap(DELAY_QUEUE_JOB_POOL);
		rMap.remove(delayQueueJodId);
	}
}
