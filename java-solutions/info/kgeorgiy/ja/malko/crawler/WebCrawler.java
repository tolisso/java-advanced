package info.kgeorgiy.ja.malko.crawler;

import info.kgeorgiy.java.advanced.crawler.AdvancedCrawler;
import info.kgeorgiy.java.advanced.crawler.Crawler;
import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.ReplayDownloader;
import info.kgeorgiy.java.advanced.crawler.Result;
import info.kgeorgiy.java.advanced.crawler.URLUtils;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Phaser;

public class WebCrawler implements AdvancedCrawler {

    private final Downloader downloader;
    private final int perHost;
    private final Map<String, HostQueue> hostQueueMap = new ConcurrentHashMap<>();

    private final ExecutorService downloaderPool;
    private final ExecutorService extractionPool;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.perHost = perHost;

        downloaderPool = Executors.newFixedThreadPool(downloaders);
        extractionPool = Executors.newFixedThreadPool(extractors);
    }

    private class HostQueue {
        private Queue<Runnable> queue = new ArrayDeque<>();
        int running = 0;

        private synchronized void tryRunTask() {
            if (queue.isEmpty() || running >= perHost) {
                return;
            }
            running++;
            Runnable task = queue.poll();
            downloaderPool.submit(() -> {
                task.run();
                decrement();
            });
        }

        private synchronized void decrement() {
            running--;
            tryRunTask();
        }

        private synchronized void add(Runnable r) {
            queue.add(r);
            tryRunTask();
        }
    }

    private void downloadOnPool(String url,
        Phaser phaser, Set<String> downloaded, Map<String, IOException> errors,
        Queue<String> levelLinks, Set<String> hosts) {
        try {
            String host = URLUtils.getHost(url);
            if (hosts != null) {
                if (!hosts.contains(host)) {
                    return;
                }
            }
            HostQueue hostQueue = hostQueueMap.computeIfAbsent(host, s -> new HostQueue());
            phaser.register();

            hostQueue.add(() -> {
                try {
                    if (downloaderPool.isShutdown()) {
                        return;
                    }
                    synchronized (downloaded) {
                        if (downloaded.contains(url)) {
                            return;
                        }
                        downloaded.add(url);
                    }

                    Document doc = downloader.download(url);

                    extractOnPool(doc, url, phaser, errors, levelLinks);
                } catch (IOException e) {
                    errors.putIfAbsent(url, e);
                } finally {
                    phaser.arriveAndDeregister();
                }
            });
        } catch (IOException exc) {
            errors.putIfAbsent(url, exc);
        }
    }

    private void extractOnPool(Document doc, String url,
        Phaser phaser, Map<String, IOException> errors,
        Queue<String> levelLinks) {

        phaser.register();
        extractionPool.submit(() -> {
            try {
                if (extractionPool.isShutdown()) {
                    return;
                }
                List<String> links = doc.extractLinks();
                levelLinks.addAll(links);

            } catch (IOException e) {
                errors.putIfAbsent(url, e);
            } finally {
                phaser.arriveAndDeregister();
            }
        });
    }

    @Override
    public Result download(final String url, final int depth, List<String> hostsList) {
        final Phaser phaser = new Phaser();
        final Set<String> downloaded = new HashSet<>();
        final Map<String, IOException> errors = new ConcurrentHashMap<>();
        final Set<String> hosts = hostsList == null ? null : ConcurrentHashMap.newKeySet();
        if (hosts != null) {
            hosts.addAll(hostsList);
        }
        Queue<String> levelLinks = new LinkedBlockingQueue<>();

        phaser.register();
        levelLinks.add(url);

        for (int i = 0; i < depth; i++) {
            Queue<String> newLevelLinks = new LinkedBlockingQueue<>();
            levelLinks
                .parallelStream()
                .forEach(s -> downloadOnPool(s, phaser, downloaded,
                    errors, newLevelLinks, hosts));
            levelLinks = newLevelLinks;

            phaser.arriveAndAwaitAdvance();
        }

        downloaded.removeAll(errors.keySet());
        return new Result(new ArrayList<>(downloaded), errors);
    }

    @Override
    public Result download(String url, int depth) {
        return download(url, depth, null);
    }

    @Override
    public void close() {
        PoolShutdowner.shutdown(downloaderPool);
        PoolShutdowner.shutdown(extractionPool);
    }

    public static void main(String[] args) throws IOException {
        WebCrawler crawler = new WebCrawler(new ReplayDownloader(args[0], 0, 0),
            10, 10, 10);
        System.out.println(crawler.download(args[0], 2).getDownloaded());
        crawler.close();
    }
}
