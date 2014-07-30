package me.dowen.syncDemo;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CountDownLatchDemo {

	public static void main(String[] args) {
		// 早上起来要做以下几件事
		// 1 上卫生间
		// 2 看新闻
		// 3 洗衣服
		// 4 煮鸡蛋（有煮蛋器）
		// 5 做早饭
		// 6 吃饭
		// 7 上班
		// 其中： 1、2可以同时进行；3、4、5可以同时进行但3、5不可同时进行；6、7只能顺序进行
		final Random r = new Random(); // 计划赶不上变化，不一定哪件事会拖点时间。
		ExecutorService run = Executors.newCachedThreadPool(); // 啊，事都堆在一起了。
		final Object lockMe = new Object(); // 线程锁，“我”。有些事情只能自己动手，不能干另的需要手动干的事了。
		CountDownLatch toiletDoneSignal = new CountDownLatch(2); // 两件事要做，呃，厕所里...
		Daily toilet = new Daily(null, toiletDoneSignal) {
			@Override
			protected void doRun() {
				System.out.println("起床上厕所...");
				try {
					Thread.sleep(3000 + r.nextInt(1000)); // 三秒神速。。。意思到了就行吧！
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("上完厕所，神清气爽！");
			}
		};
		Daily news = new Daily(null, toiletDoneSignal) {
			@Override
			protected void doRun() {
				System.out.println("拿出平板看新闻...");
				try {
					Thread.sleep(2000 + r.nextInt(500));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("嗯，今天没有飞机掉下来，和平和平！");
			}
		};
		CountDownLatch beforeBreakfastDoneSingal = new CountDownLatch(3); // 吃饭之前有3件事要做，起码先做饭吧。
		Daily cook = new Daily(toiletDoneSignal, beforeBreakfastDoneSingal) {
			@Override
			protected void doRun() {
				synchronized (lockMe) { // 得看着锅，不能去洗衣服了
					System.out.println("今天煮白水面，可怜人啊！");
					try {
						Thread.sleep(1500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.out.println("煮好了，可怜吧吧的盛出来。");
				}
			}
		};
		Daily wash = new Daily(toiletDoneSignal, beforeBreakfastDoneSingal) {
			@Override
			protected void doRun() {
				synchronized (lockMe) {
					System.out.println("洗刷刷洗刷刷洗...");
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.out.println("嗯，洗的很干净！");
				}
			}
		};
		Daily eggCook = new Daily(toiletDoneSignal, beforeBreakfastDoneSingal) {
			@Override
			protected void doRun() {
				System.out.println("嘿嘿，我有小熊煮蛋器，每天都有煮蛋吃！");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("鸡蛋煮好了，凉着！");
			}
		};
		CountDownLatch breakfastDoneSignal = new CountDownLatch(1);
		Daily breakfast = new Daily(beforeBreakfastDoneSingal, breakfastDoneSignal) {
			@Override
			protected void doRun() {
				System.out.println("能量填充中！");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("多谢款待！");
			}
		};
		CountDownLatch toWorkDoneSignal = new CountDownLatch(1);
		Daily toWork = new Daily(breakfastDoneSignal, toWorkDoneSignal) {
			@Override
			protected void doRun() {
				System.out.println("上班赶路，嘿咻嘿咻！！！");
				try {
					Thread.sleep(1500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		// 开始干吧！别管顺序，所有事情都丢进去，我安排好了线程同步！
		run.execute(toWork);
		run.execute(breakfast);
		run.execute(eggCook);
		run.execute(wash);
		run.execute(cook);
		run.execute(toilet);
		run.execute(news);
		run.shutdown();
		try {
			toWorkDoneSignal.await();
			System.out.println("到了公司，写了这个代码...");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	static abstract class Daily implements Runnable {

		private final CountDownLatch startSignal;
		private final CountDownLatch doneSignal;

		public Daily(CountDownLatch startSignal, CountDownLatch doneSignal) {
			super();
			this.startSignal = startSignal;
			this.doneSignal = doneSignal;
		}

		@Override
		public void run() {
			try {
				if (this.startSignal != null)
					this.startSignal.await();
				this.doRun();
				this.doneSignal.countDown();
			} catch (InterruptedException e) {
			}
		}

		protected abstract void doRun();

	}

}
