package life.http;

import life.Account;
import life.LifeException;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetGuaranteedBalance extends APIServlet.APIRequestHandler {

    static final GetGuaranteedBalance instance = new GetGuaranteedBalance();

    private GetGuaranteedBalance() {
        super(new APITag[] {APITag.ACCOUNTS, APITag.FORGING}, "account", "numberOfConfirmations");
    }

    @Override
    JSONStreamAware processRequest(HttpServletRequest req) throws LifeException {

        Account account = ParameterParser.getAccount(req);
        int numberOfConfirmations = ParameterParser.getNumberOfConfirmations(req);

        JSONObject response = new JSONObject();
        if (account == null) {
            response.put("guaranteedBalanceNQT", "0");
        } else {
            response.put("guaranteedBalanceNQT", String.valueOf(account.getGuaranteedBalanceNQT(numberOfConfirmations)));
        }

        return response;
    }

}
