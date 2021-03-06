import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsMessageContext;

import java.util.*;

class Main {
    private final Set<String> users = new LinkedHashSet<>();
    private final List<ComputerCraftChannel> channels = new ArrayList<>();

    public static void main(String[] args) {
        new Main().init();
    }

    public void init() {
        Javalin app = Javalin.create().start(8080);//
        app.ws("/websocket/:id", ws -> ws.onMessage(this::handleMessage));
        app.ws("/subscribe/:id", ws -> {
            ws.onConnect(this::handleSubscribe);
            ws.onMessage(this::handleSubMessage);
        });
    }

    public void handleSubMessage(WsMessageContext ctx) {
        for (ComputerCraftChannel channel : channels) {
            if (channel.getId().equals(ctx.pathParam("id"))) {
                String message = ctx.message();
                System.out.println(message);
                channel.getCtx().send(message);
            }
        }
    }

    public void handleSubscribe(WsConnectContext ctx) {
        System.out.println("sub: " + ctx.pathParam("id"));
        for (ComputerCraftChannel channel : channels) {
            if (channel.getId().equals(ctx.pathParam("id"))) {
                channel.addObserver(new ViewerSocket(ctx));
            }
        }
    }

    public void handleMessage(WsMessageContext ctx) {
        String id = ctx.pathParam("id");
        boolean added = users.add(id);
        if (added) {
            ComputerCraftChannel computerCraftChannel = new ComputerCraftChannel();
            computerCraftChannel.setId(id);
            computerCraftChannel.setMessage(ctx.message());
            computerCraftChannel.setCtx(ctx);
            channels.add(computerCraftChannel);
        } else {
            for (ComputerCraftChannel channel : channels) {
                if (channel.getId().equals(id)) {
                    channel.setMessage(ctx.message());
                }
            }
        }
    }

}