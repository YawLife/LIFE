package life.http;

import life.Account;
import life.Block;
import life.LifeCoin;
import life.LifeException;
import life.db.DbIterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetAccountBlocks extends APIServlet.APIRequestHandler {

    static final GetAccountBlocks instance = new GetAccountBlocks();

    private GetAccountBlocks() {
        super(new APITag[] {APITag.ACCOUNTS}, "account", "timestamp", "firstIndex", "lastIndex", "includeTransactions");
    }

    @Override
    JSONStreamAware processRequest(HttpServletRequest req) throws LifeException {

        Account account = ParameterParser.getAccount(req);
        int timestamp = ParameterParser.getTimestamp(req);
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);

        boolean includeTransactions = "true".equalsIgnoreCase(req.getParameter("includeTransactions"));

        JSONArray blocks = new JSONArray();
        try (DbIterator<? extends Block> iterator = LifeCoin.getBlockchain().getBlocks(account, timestamp, firstIndex, lastIndex)) {
            while (iterator.hasNext()) {
                Block block = iterator.next();
                blocks.add(JSONData.block(block, includeTransactions));
            }
        }

        JSONObject response = new JSONObject();
        response.put("blocks", blocks);

        return response;
    }

}
