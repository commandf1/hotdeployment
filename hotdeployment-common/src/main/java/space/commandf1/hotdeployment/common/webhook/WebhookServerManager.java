package space.commandf1.hotdeployment.common.webhook;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.SneakyThrows;
import lombok.val;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class WebhookServerManager {
	private static final WebhookServerManager INSTANCE = new WebhookServerManager();

	public static WebhookServerManager getInstance() {
		return INSTANCE;
	}

	private final Map<Integer, HttpServer> portToServer = new ConcurrentHashMap<>();
	private final Map<Integer, Integer> portRefCount = new ConcurrentHashMap<>();

	@SneakyThrows
	public void registerHandler(int port, String path, HttpHandler handler) {
		val server = portToServer.computeIfAbsent(port, p -> {
			try {
				val s = HttpServer.create(new InetSocketAddress(p), 0);
				s.setExecutor(Executors.newCachedThreadPool());
				s.start();
				return s;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		
		try {
			server.createContext(path, handler);
			portRefCount.merge(port, 1, Integer::sum);
		} catch (IllegalArgumentException e) {
			portRefCount.merge(port, 1, Integer::sum);
		}
	}

	public void unregisterHandler(int port, String path) {
		val server = portToServer.get(port);
		if (server == null) return;
		server.removeContext(path);
		portRefCount.computeIfPresent(port, (p, count) -> {
			int left = count - 1;
			if (left <= 0) {
				server.stop(0);
				portToServer.remove(p);
				return null;
			}
			return left;
		});
	}
}