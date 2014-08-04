package me.dowen.syncDemo;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CyclicBarrierDemo {

	public static void main(String[] args) {
		final Random random = new Random(); // 每个人不一定什么时候到
		final CountDownLatch countDown = new CountDownLatch(18); // 18个人
		final CountDownLatch firstSignal = new CountDownLatch(1); // 第一桌
		// 每4人一桌
		final CyclicBarrier barrier = new CyclicBarrier(4, new Runnable() {
			@Override
			public void run() {
				if (firstSignal.getCount() > 0) {
					System.out.println("老板，先开一桌！");
				} else {
					System.out.println("老板，再开一桌！");
				}
				firstSignal.countDown();
			}
		});
		ExecutorService pool = Executors.newCachedThreadPool();
		for (int i = 0; i < 18; i++) {
			final int idx = i + 1;
			pool.execute(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(random.nextInt(3000));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.out.println("第" + idx + "人到了");
					countDown.countDown();
					try {
						barrier.await(); // 等这桌人满或所有人到齐。
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (BrokenBarrierException e) { // 最后2人会产生此异常。
						// e.printStackTrace();
						System.out.println("你来的可真晚，" + idx); // 来的晚要被抱怨
					}
				}
			});
		}
		pool.shutdown();
		try {
			countDown.await(); // 等所有人到齐
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (barrier.getNumberWaiting() != 0) {
			System.out.println("老板，给最后" + barrier.getNumberWaiting() + "个人单开一桌！");
			barrier.reset();
		}
		System.out.println("老板，我们人都到齐了，上菜吧");
	}

}
