package life.http;

import life.LifeException;
import life.Order;
import life.db.DbIterator;
import life.util.Convert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetAccountCurrentAskOrders extends APIServlet.APIRequestHandler {

    static final GetAccountCurrentAskOrders instance = new GetAccountCurrentAskOrders();

    private GetAccountCurrentAskOrders() {
        super(new APITag[] {APITag.ACCOUNTS, APITag.AE}, "account", "asset", "firstIndex", "lastIndex");
    }

    @Override
    JSONStreamAware processRequest(HttpServletRequest req) throws LifeException {

        long accountId = ParameterParser.getAccount(req).getId();
        long assetId = 0;
        try {
            assetId = Convert.parseUnsignedLong(req.getParameter("asset"));
        } catch (RuntimeException e) {
            // ignore
        }
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);

        DbIterator<Order.Ask> askOrders;
        if (assetId == 0) {
            askOrders = Order.Ask.getAskOrdersByAccount(accountId, firstIndex, lastIndex);
        } else {
            askOrders = Order.Ask.getAskOrdersByAccountAsset(accountId, assetId, firstIndex, lastIndex);
        }
        JSONArray orders = new JSONArray();
        try {
            while (askOrders.hasNext()) {
                orders.add(JSONData.askOrder(askOrders.next()));
            }
        } finally {
            askOrders.close();
        }
        JSONObject response = new JSONObject();
        response.put("askOrders", orders);
        return response;
    }

}
